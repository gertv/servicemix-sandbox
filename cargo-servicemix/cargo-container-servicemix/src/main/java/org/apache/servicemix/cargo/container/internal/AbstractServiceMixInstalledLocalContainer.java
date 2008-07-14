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
package org.apache.servicemix.cargo.container.internal;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.jar.JarFile;

import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.AntContainerExecutorThread;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;

/**
 * Common support for all ServiceMix container versions.
 * 
 * @version $Revision$
 */
public abstract class AbstractServiceMixInstalledLocalContainer 
                extends AbstractInstalledLocalContainer
{
    /**
     * Parsed version of the container.
     */
    private String version;

    /**
     * Capability of the ServiceMix container.
     */
    private ContainerCapability capability = new JBIContainerCabability();

    private AntContainerExecutorThread serviceMixRunner;
    
    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(LocalConfiguration)
     */
    public AbstractServiceMixInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getCapability()
     */
    public ContainerCapability getCapability()
    {
        return this.capability;
    }

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#doStart(Java)
     */
    public void doStart(Java java) throws Exception
    {
        Path classpath = doAction(java);

        // Add settings specific to a given container version
        startUpAdditions(java, classpath);

        serviceMixRunner = new AntContainerExecutorThread(java);
        serviceMixRunner.start();

    }
    
    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#doStop(Java)
     */
    public void doStop(Java java) throws Exception
    {
        ServiceMixJMXUtil.shutdownServer();
    }

    /**
     * Common Ant Java task settings for start and stop actions.
     *
     * @param java the Ant Java object passed by the Cargo underlying container SPI classes
     * @return the classpath set (this is required as strangely there's no way to query the Ant
     *         Java object for the classapth after it's set)
     */
    private Path doAction(Java java)
    {
        java.setDir(new File(getHome()));
        
        // Invoke the main class to start the container
        java.addSysproperty(getAntUtils().createSysProperty("servicemix.home",
            "."));

        java.addSysproperty(getAntUtils().createSysProperty("classworlds.conf",
            "conf/servicemix.conf"));
        
        java.addSysproperty(getAntUtils().createSysProperty("java.endorsed.dirs",
            "lib/endorsed"));
        
        java.setClassname("org.codehaus.classworlds.Launcher");

        Path classpath = java.createClasspath();
        
        FileSet fileSet = new FileSet();
        fileSet.setDir(new File(getHome()));
        fileSet.createInclude().setName("lib/*.jar");
        classpath.addFileset(fileSet);

        return classpath;
    }

    /**
     * Allow specific version implementations to add custom settings to the 
     * Java container that will be started.
     * 
     * @param javaContainer the Ant Java object that will start the container
     * @param classpath the classpath that will be used to start the 
     *        container
     * @throws FileNotFoundException in case the Tools jar cannot be found
     */
    protected abstract void startUpAdditions(Java javaContainer, Path classpath) 
        throws FileNotFoundException;

    /**
     * @param defaultVersion default version to use if we cannot find out the exact version
     * @return the version found
     */
    protected String getVersion(String defaultVersion)
    {
        String version = this.version;
        
        if (version == null)
        {
            try
            {
                File[] listFiles = new File(getHome(), "/lib").listFiles(new FileFilter() {
                
                    public boolean accept(File file) 
                    {
                        return file.getName().startsWith("servicemix-core");
                    }
                
                });
                
                JarFile jarFile = new JarFile(listFiles[0]);
                
                version = jarFile.getManifest().getMainAttributes()
                            .getValue("Implementation-Version");
                
                getLogger().info("Found ServiceMix version [" + version + "]",
                    this.getClass().getName());
            }
            catch (Exception e)
            {
                getLogger().debug("Failed to get ServiceMix version, Error = [" + e.getMessage()
                    + "]. Using generic version [" + defaultVersion + "]", 
                    this.getClass().getName());
                version = defaultVersion;
            }
        }
        this.version = version;
        return version;
    }
    
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException {
        
        
        if (waitForStarting)
        {
            waitForContainerStart();
        }
        
    }
     
    private void waitForContainerStart()
    {
        int retryCount = 60;
        
        Object serverState = null;
        
        while ((serverState == null || !serverState.equals("Started")) || retryCount == 0) 
        {
            try {
                
                Thread.sleep(1000);
                retryCount--;
                
                serverState = ServiceMixJMXUtil.getServerState();
                
                getLogger().debug("state from smx host: " + serverState, 
                                  this.getClass().getName());
                
            } catch (Exception e) {
                //ignore
            }
            
        }
        
        if (serverState == null) 
        {
            throw new RuntimeException("could not connect to server");
        }
        
        //wait for 'all' deployments
        try {
            int oldDeploymentCount = ServiceMixJMXUtil.getDeploymentCount();
            int newDeploymentCount = -1;
            while ((oldDeploymentCount != newDeploymentCount) || newDeploymentCount == -1) {
                oldDeploymentCount = ServiceMixJMXUtil.getDeploymentCount();
                Thread.sleep(5000);
                newDeploymentCount = ServiceMixJMXUtil.getDeploymentCount();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    
}
