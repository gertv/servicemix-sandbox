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
package org.apache.servicemix.cargo.container;

import org.apache.servicemix.cargo.container.internal.ServiceMixJMXUtil;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.spi.deployer.AbstractInstalledLocalDeployer;

/**
 * Local deployer for ServiceMix3.
 * 
 * @version $Revision$
 */
public class ServiceMix3xInstalledLocalDeployer extends AbstractInstalledLocalDeployer 
{

    /**
     * Hot-Deploy-Directory.
     */
    private String deployDir;

    /**
     * Constructor.
     * 
     * @param container container
     */
    public ServiceMix3xInstalledLocalDeployer(InstalledLocalContainer container) 
    {
        super(container);
        deployDir = container.getHome() + "/hotdeploy";
    }

    /**
     * Deploy a file to ServiceMix.
     * 
     * @param deployable deplyoable.
     */
    public void deploy(Deployable deployable) 
    {

        ServiceMixJMXUtil.deploy(getFileHandler().getURL(deployable.getFile()));
                
    }

}
