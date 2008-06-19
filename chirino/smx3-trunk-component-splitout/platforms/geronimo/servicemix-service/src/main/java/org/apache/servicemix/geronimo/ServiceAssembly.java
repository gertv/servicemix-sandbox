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
package org.apache.servicemix.geronimo;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.jbi.component.ServiceUnitManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.servicemix.jbi.deployment.Descriptor;
import org.apache.servicemix.jbi.deployment.DescriptorFactory;

public class ServiceAssembly implements GBeanLifecycle {

    private static final Log log = LogFactory.getLog(ServiceAssembly.class);
    
    private String name;
    private Container container;
    private URI rootDir;
    private List<ServiceUnitReference> serviceUnitReferences;
    private Kernel kernel;
    
	public ServiceAssembly(String name, 
			               Container container,
			               URL configurationBaseUrl,
                           List<ServiceUnitReference> serviceUnitReferences,
                           Kernel kernel) throws Exception {
		this.name = name;
		this.container = container;
        this.serviceUnitReferences = serviceUnitReferences;
        this.kernel = kernel;
        //TODO is there a simpler way to do this?
        if (configurationBaseUrl.getProtocol().equalsIgnoreCase("file")) {
            this.rootDir = new URI("file", configurationBaseUrl.getPath(), null);
        } else {
            this.rootDir = URI.create(configurationBaseUrl.toString());
        }
        log.info("Created JBI service assembly: " + name);
	}
	
    public void doStart() throws Exception {
        log.info("doStart called for JBI service assembly: " + name);
        container.register(this);
    }

    public void doStop() throws Exception {
        log.info("doStop called for JBI service assembly: " + name);
        container.unregister(this);
    }

    public void doFail() {
        log.info("doFail called for JBI service assembly: " + name);
    }
	
    public URI getRootDir() {
        return rootDir;
    }
	
    public String getName() {
        return name;
    }
	
    public void undeploySus() {
        for (ServiceUnitReference reference: serviceUnitReferences) {
            undeploy(reference);
        }       
    }
    
    private void undeploy(ServiceUnitReference reference) {
       try {
            Component componentGBean = getComponentGBean(
                    reference.getAssocialtedServiceUnitName().getArtifactId(), 
                    reference.getAssocialtedServiceUnitName());
            ServiceUnitManager serviceUnitManager = 
                componentGBean.getComponent().getServiceUnitManager();
            serviceUnitManager.undeploy(reference.getServiceUnitName(), 
                    reference.getServiceUnitPath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private org.apache.servicemix.geronimo.Component getComponentGBean(
            String compName, Artifact artifactName)
            throws GBeanNotFoundException {
        Properties props = new Properties();
        props.put("jbiType", "JBIComponent");
        props.put("name", compName);
        org.apache.servicemix.geronimo.Component serviceUnit = 
                (org.apache.servicemix.geronimo.Component) 
                kernel.getGBean(new AbstractName(artifactName, props));
        return serviceUnit;
    }

    public Descriptor getDescriptor() throws Exception {
        return DescriptorFactory.buildDescriptor(new File(new File(rootDir), "install"));
	}

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("JBIServiceAssembly", ServiceAssembly.class, "JBIServiceAssembly");
        infoFactory.addAttribute("name", String.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("serviceUnitReferences", List.class, true);
        infoFactory.addReference("container", Container.class);
        infoFactory.addAttribute("configurationBaseUrl", URL.class, true);
        infoFactory.setConstructor(new String[] {"name", "container", "configurationBaseUrl", "serviceUnitReferences" , "kernel"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
