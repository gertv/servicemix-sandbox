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
package org.apache.servicemix.kernel.testing.support;

import java.util.Properties;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.springframework.osgi.test.platform.FelixPlatform;
import org.springframework.osgi.test.platform.OsgiPlatform;
import org.springframework.util.ClassUtils;
import org.apache.felix.main.Main;
import org.apache.felix.framework.Felix;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;

public class SmxKernelPlatform implements OsgiPlatform {

    private static final Log log = LogFactory.getLog(FelixPlatform.class);

    private static final String FELIX_CONF_FILE = "felix.config.properties";

    private static final String FELIX_CONFIG_PROPERTY = "felix.config.properties";

    private static final String FELIX_PROFILE_DIR_PROPERTY = "felix.cache.profiledir";

    private BundleContext context;

    private Felix platform;

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

    public void start() throws Exception {

        platform = new Felix(getConfigurationProperties(), null);
        platform.start();

        Bundle systemBundle = platform;

        // call getBundleContext
        final Method getContext = systemBundle.getClass().getDeclaredMethod("getBundleContext", null);

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
            platform.stop();
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
        props.setProperty(FELIX_PROFILE_DIR_PROPERTY, this.felixStorageDir.getAbsolutePath());
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
        return Main.loadConfigProperties();
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
}
