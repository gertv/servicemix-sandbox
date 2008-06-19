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
package org.apache.servicemix.jbi.management.task;

import org.apache.servicemix.jbi.framework.AdminCommandsServiceMBean;
import org.apache.tools.ant.Project;

/**
 * ListBindingComponentsTask
 * 
 * @version $Revision:
 */
public class ListBindingComponentsTask extends JbiTask {

    private String sharedLibraryName;

    private String serviceAssemblyName;

    private String bindingComponentName;

    private String state;

    private String xmlOutput;

    /**
     * @return the xmlOutput
     */
    public String isXmlOutput() {
        return xmlOutput;
    }

    /**
     * @param xmlOutput
     *            the xmlOutput to set
     */
    public void setXmlOutput(String xmlOutput) {
        this.xmlOutput = xmlOutput;
    }

    /**
     * 
     * @return shared library name
     */
    public String getSharedLibraryName() {
        return sharedLibraryName;
    }

    /**
     * 
     * @param sharedLibraryName
     */
    public void setSharedLibraryName(String sharedLibraryName) {
        this.sharedLibraryName = sharedLibraryName;
    }

    /**
     * 
     * @return service assembly name
     */
    public String getServiceAssemblyName() {
        return serviceAssemblyName;
    }

    /**
     * 
     * @param serviceAssemblyName
     */
    public void setServiceAssemblyName(String serviceAssemblyName) {
        this.serviceAssemblyName = serviceAssemblyName;
    }

    /**
     * 
     * @return binding component name
     */
    public String getBindingComponentName() {
        return bindingComponentName;
    }

    /**
     * 
     * @param bindingComponentName
     */
    public void setBindingComponentName(String bindingComponentName) {
        this.bindingComponentName = bindingComponentName;
    }

    /**
     * 
     * @return component state
     */
    public String getState() {
        return state;
    }

    /**
     * 
     * @param state
     *            Sets the component state
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * execute the task
     * 
     * @throws BuildException
     */
    public void doExecute(AdminCommandsServiceMBean acs) throws Exception {
        String result = acs.listComponents(true, false, true, getState(), getSharedLibraryName(), getServiceAssemblyName());
        if (xmlOutput != null) {
            getProject().setProperty(xmlOutput, result);
        }
        log(result, Project.MSG_WARN);
    }

}
