/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.platform.testing.support;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.CompoundEnumeration;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.servicemix.platform.main.spi.MainService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.test.platform.FelixPlatform;
import org.springframework.osgi.test.platform.OsgiPlatform;
import org.springframework.util.ClassUtils;

public class SmxPlatform implements OsgiPlatform {

    private static final Log log = LogFactory.getLog(FelixPlatform.class);

    private static final String FELIX_CONF_FILE = "config.properties";

    private static final String FELIX_CONFIG_PROPERTY = "config.properties";

    public final static String	FRAMEWORK_STORAGE = "org.osgi.framework.storage";

    private static final String DELIM_START = "${";
    private static final String DELIM_STOP = "}";

    private BundleContext context;

    private Object platform;

    private File felixStorageDir;

    private Properties configurationProperties = new Properties();

    protected Properties getPlatformProperties() {
        // load Felix configuration
        Properties props = new Properties();
        props.putAll(getFelixConfiguration());
        props.putAll(getLocalConfiguration());
        return props;
    }

    public Properties getConfigurationProperties() {
        // local properties
        configurationProperties.putAll(getPlatformProperties());
        // system properties
        configurationProperties.putAll(System.getProperties());
        return configurationProperties;
    }

    public BundleContext getBundleContext() {
        return context;
    }

    private Set<String> getJars(Class... classes) {
        Set<String> jars = new HashSet<String>();
        for (Class cl : classes) {
            String name = cl.getName().replace('.', '/') + ".class";
            URL url = (cl.getClassLoader() != null ? cl.getClassLoader() : getClass().getClassLoader()).getResource(name);
            String path = url.toString();
            if (path.startsWith("jar:")) {
                path = path.substring(0, path.indexOf('!'));
            } else {
                path = path.substring(0, path.indexOf(name));
            }
            jars.add(path);
        }
        return jars;
    }

    public void start() throws Exception {
        Set<String> jars = getJars(Felix.class);
        ClassLoader classLoader = new GuardClassLoader(toURLs(jars.toArray(new String[jars.size()])), null);

        BundleActivator activator = new BundleActivator() {
            private ServiceRegistration registration;

            public void start(BundleContext context) {
                registration = context.registerService(MainService.class.getName(), new MainService() {
                    public String[] getArgs() {
                        return new String[0];
                    }
                    public int getExitCode() {
                        return 0;
                    }
                    public void setExitCode(int exitCode) {
                    }
                }, null);
            }

            public void stop(BundleContext context) {
                registration.unregister();
            }
        };
        List<BundleActivator> activations = new ArrayList<BundleActivator>();
        activations.add(activator);

        Properties props = getConfigurationProperties();
        props.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, activations);

        Thread.currentThread().setContextClassLoader(classLoader);
        Class cl = classLoader.loadClass(Felix.class.getName());
        Constructor cns = cl.getConstructor(Map.class);
        platform = cns.newInstance(props);
        platform.getClass().getMethod("start").invoke(platform);

        Bundle systemBundle = (Bundle) platform;

        // call getBundleContext
        final Method getContext = systemBundle.getClass().getMethod("getBundleContext", null);

        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                getContext.setAccessible(true);
                return null;
            }
        });
        context = (BundleContext) getContext.invoke(systemBundle, null);
    }

    public void stop() throws Exception {
        try {
            platform.getClass().getMethod("stop").invoke(platform);
        }
        finally {
            // remove cache folder
            delete(felixStorageDir);
        }
    }

    public String toString() {
        return getClass().getName();
    }

    File createTempDir(String suffix) {
        if (suffix == null)
            suffix = "osgi";
        File tempFileName;

        try {
            tempFileName = File.createTempFile("org.sfw.osgi", suffix);
        }
        catch (IOException ex) {
            if (log.isWarnEnabled()) {
                log.warn("Could not create temporary directory, returning a temp folder inside the current folder", ex);
            }
            return new File("./tmp-test");
        }

        tempFileName.delete(); // we want it to be a directory...
        File tempFolder = new File(tempFileName.getAbsolutePath());
        tempFolder.mkdir();
        return tempFolder;
    }

    /**
     * Configuration settings for the OSGi test run.
     *
     * @return
     */
    private Properties getLocalConfiguration() {
        Properties props = new Properties();
        felixStorageDir = createTempDir("felix");
        props.setProperty(FRAMEWORK_STORAGE, this.felixStorageDir.getAbsolutePath());
        if (log.isTraceEnabled())
            log.trace("felix storage dir is " + felixStorageDir.getAbsolutePath());

        return props;
    }

    /**
     * Loads Felix config.properties.
     *
     * <strong>Note</strong> the current implementation uses Felix's Main class
     * to resolve placeholders as opposed to loading the properties manually
     * (through JDK's Properties class or Spring's PropertiesFactoryBean).
     *
     * @return
     */
    // TODO: this method should be removed once Felix 1.0.2 is released
    private Properties getFelixConfiguration() {
        String location = "/".concat(ClassUtils.classPackageAsResourcePath(getClass())).concat("/").concat(FELIX_CONF_FILE);
        URL url = getClass().getResource(location);
        if (url == null)
            throw new RuntimeException("cannot find felix configuration properties file:" + location);

        // used with Main
        System.getProperties().setProperty(FELIX_CONFIG_PROPERTY, url.toExternalForm());

        // load config.properties (use Felix's Main for resolving placeholders)
        Properties props = new Properties();
        InputStream is = null;
        try {
            is = url.openConnection().getInputStream();
            props.load(is);
            is.close();
        }
        catch (FileNotFoundException ex) {
            // Ignore file not found.
        }
        catch (Exception ex) {
            System.err.println("Main: Error loading system properties from " + url);
            System.err.println("Main: " + ex);
            try {
                if (is != null) is.close();
            }
            catch (IOException ex2) {
                // Nothing we can do.
            }
            return null;
        }
        // Perform variable substitution for system properties.
        for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            props.setProperty(name, substVars(props.getProperty(name), name, null, props));
        }
        return props;
    }

    /**
     * Delete the given file (can be a simple file or a folder).
     *
     * @param file the file to be deleted
     * @return if the deletion succeded or not
     */
    public static boolean delete(File file) {

        // bail out quickly
        if (file == null)
            return false;

        // recursively delete children file
        boolean success = true;

        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i = 0; i < children.length; i++) {
                success &= delete(new File(file, children[i]));
            }
        }

        // The directory is now empty so delete it
        return (success &= file.delete());
    }

    private static URL[] toURLs(String[] jars) throws MalformedURLException {
        URL[] urls = new URL[jars.length];
        for (int i = 0; i < urls.length; i++) {
            String s = jars[i];
            if (s.startsWith("jar:")) {
                s = s.substring("jar:".length());
            }
            urls[i] = new URL(s);
        }
        return urls;
    }

    public class GuardClassLoader extends URLClassLoader {
        private Set<String> bootDelegationPackages = new HashSet<String>();
        private Set<String> packages = new HashSet<String>();
        private List<ClassLoader> parents = new ArrayList<ClassLoader>();

        public GuardClassLoader(URL[] urls, List<String> additionalPackages) throws MalformedURLException {
            super(urls, SmxPlatform.class.getClassLoader());
            Properties props = getConfigurationProperties();
            String prop = props.getProperty("org.osgi.framework.system.packages");
            String[] ps = prop.split(",");
            for (String p : ps) {
                String[] spack = p.split(";");
                for (String sp : spack) {
                    sp = sp.trim();
                    if (!sp.startsWith("version")) {
                        packages.add(sp);
                    }
                }
            }
            if (additionalPackages != null) {
                packages.addAll(additionalPackages);
            }
            prop = props.getProperty("org.osgi.framework.bootdelegation");
            ps = prop.split(",");
            for (String p : ps) {
                p = p.trim();
                if (p.endsWith("*")) {
                    p = p.substring(0, p.length() - 1);
                }
                bootDelegationPackages.add(p);
            }
            ClassLoader cl = getParent();
            while (cl != null) {
                parents.add(0, cl);
                cl = cl.getParent();
            }
            //System.err.println("Boot packages: " + packages);
        }

        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            //System.err.println("Loading class: " + name);
            Class c = findLoadedClass(name);
            if (c == null) {
                String pkg = name.substring(0, name.lastIndexOf('.'));
                boolean match = name.startsWith("java.") || packages.contains(pkg);
                if (!match) {
                    for (String p : bootDelegationPackages) {
                        if (pkg.startsWith(p)) {
                            match = true;
                            break;
                        }
                    }
                }
                if (match) {
                    for (ClassLoader cl : parents) {
                        try {
                            c = cl.loadClass(name);
                            //System.err.println("Class loaded from: " + cl.getResource(name.replace('.', '/') + ".class"));
                            break;
                        } catch (ClassNotFoundException e) {
                        }
                    }
                    if (c == null) {
                        throw new ClassNotFoundException(name);
                    }
                    //c = getParent().loadClass(name);
                } else  {
                    c = findClass(name);
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }

        public URL getResource(String name) {
            //System.err.println("GetResource: " + name);
            URL url = getParent().getResource(name);
            if (url != null && url.toString().startsWith("file:")) {
                return url;
            }
            url = findResource(name);
            System.err.println("Resource " + name + " found at " + url);
            return url;
            /*
            URL u = getParent().getResource(name);
            if (u != null) {
                String path = u.toString();
                int idx = path.indexOf('!');
                if (idx > 0) {
                    path = path.substring(0, idx);
                    if (!jars.contains(path)) {
                        return null;
                    }
                } else {
                    idx = 0;
                }
            }
            return u;
            */
        }

        public Enumeration<URL> getResources(final String name) throws IOException {
            //System.err.println("GetResources: " + name);
            Enumeration[] tmp = new Enumeration[2];
            final Enumeration<URL> e = getParent().getResources(name);
            tmp[0] = new Enumeration<URL>() {
                URL next = null;
                public boolean hasMoreElements() {
                    while (next == null && e.hasMoreElements()) {
                        next = e.nextElement();
                        String path = next.toString();
                        if (!path.startsWith("file:")) {
                            next = null;
                        }
                    }
                    return next != null;
                }
                public URL nextElement() {
                    return next;
                }
            };
            tmp[1] = findResources(name);
            return new CompoundEnumeration(tmp) {
                public Object nextElement() {
                    Object next = super.nextElement();
                    System.err.println("Resources " + name + " found at " + next);
                    return next;
                }
            };
        }
    }

    /**
     * <p>
     * This method performs property variable substitution on the
     * specified value. If the specified value contains the syntax
     * <tt>${&lt;prop-name&gt;}</tt>, where <tt>&lt;prop-name&gt;</tt>
     * refers to either a configuration property or a system property,
     * then the corresponding property value is substituted for the variable
     * placeholder. Multiple variable placeholders may exist in the
     * specified value as well as nested variable placeholders, which
     * are substituted from inner most to outer most. Configuration
     * properties override system properties.
     * </p>
     *
     * @param val         The string on which to perform property substitution.
     * @param currentKey  The key of the property being evaluated used to
     *                    detect cycles.
     * @param cycleMap    Map of variable references used to detect nested cycles.
     * @param configProps Set of configuration properties.
     * @return The value of the specified string after system property substitution.
     * @throws IllegalArgumentException If there was a syntax error in the
     *                                  property placeholder syntax or a recursive variable reference.
     */
    public static String substVars(String val, String currentKey,
                                    Map<String, String> cycleMap, Properties configProps)
            throws IllegalArgumentException {
        // If there is currently no cycle map, then create
        // one for detecting cycles for this invocation.
        if (cycleMap == null) {
            cycleMap = new HashMap<String, String>();
        }

        // Put the current key in the cycle map.
        cycleMap.put(currentKey, currentKey);

        // Assume we have a value that is something like:
        // "leading ${foo.${bar}} middle ${baz} trailing"

        // Find the first ending '}' variable delimiter, which
        // will correspond to the first deepest nested variable
        // placeholder.
        int stopDelim = val.indexOf(DELIM_STOP);

        // Find the matching starting "${" variable delimiter
        // by looping until we find a start delimiter that is
        // greater than the stop delimiter we have found.
        int startDelim = val.indexOf(DELIM_START);
        while (stopDelim >= 0) {
            int idx = val.indexOf(DELIM_START, startDelim + DELIM_START.length());
            if ((idx < 0) || (idx > stopDelim)) {
                break;
            } else if (idx < stopDelim) {
                startDelim = idx;
            }
        }

        // If we do not have a start or stop delimiter, then just
        // return the existing value.
        if ((startDelim < 0) && (stopDelim < 0)) {
            return val;
        }
        // At this point, we found a stop delimiter without a start,
        // so throw an exception.
        else if (((startDelim < 0) || (startDelim > stopDelim))
                && (stopDelim >= 0)) {
            throw new IllegalArgumentException(
                    "stop delimiter with no start delimiter: "
                            + val);
        }

        // At this point, we have found a variable placeholder so
        // we must perform a variable substitution on it.
        // Using the start and stop delimiter indices, extract
        // the first, deepest nested variable placeholder.
        String variable =
                val.substring(startDelim + DELIM_START.length(), stopDelim);

        // Verify that this is not a recursive variable reference.
        if (cycleMap.get(variable) != null) {
            throw new IllegalArgumentException(
                    "recursive variable reference: " + variable);
        }

        // Get the value of the deepest nested variable placeholder.
        // Try to configuration properties first.
        String substValue = (configProps != null)
                ? configProps.getProperty(variable, null)
                : null;
        if (substValue == null) {
            // Ignore unknown property values.
            substValue = System.getProperty(variable, "");
        }

        // Remove the found variable from the cycle map, since
        // it may appear more than once in the value and we don't
        // want such situations to appear as a recursive reference.
        cycleMap.remove(variable);

        // Append the leading characters, the substituted value of
        // the variable, and the trailing characters to get the new
        // value.
        val = val.substring(0, startDelim)
                + substValue
                + val.substring(stopDelim + DELIM_STOP.length(), val.length());

        // Now perform substitution again, since there could still
        // be substitutions to make.
        val = substVars(val, currentKey, cycleMap, configProps);

        // Return the value.
        return val;
    }
}
