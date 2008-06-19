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
package org.apache.servicemix.web.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.management.ObjectName;
import javax.xml.namespace.QName;

import org.apache.servicemix.jbi.framework.EndpointMBean;

public class Endpoint {

    private Registry registry;
    
    private EndpointMBean mbean;

    private ObjectName objectName;
    
    private boolean showWsdl;

    public Endpoint(Registry registry, EndpointMBean mbean, ObjectName objectName) {
        this.registry = registry;
        this.mbean = mbean;
        this.objectName = objectName;
    }

    public String getName() {
        return mbean.getServiceName() + ":" + mbean.getEndpointName();
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public String getType() throws Exception {
        return objectName.getKeyProperty("SubType");
    }

    public Component getComponent() throws Exception {
        return registry.getComponent(mbean.getComponentName());
    }

    public boolean equals(Object o) {
        if (o instanceof Endpoint) {
            return ((Endpoint) o).objectName.equals(objectName);
        } else {
            return false;
        }
    }
    
    public int hashCode() {
        return objectName.hashCode();
    }

    public boolean isShowWsdl() {
        return showWsdl;
    }

    public void setShowWsdl(boolean showWsdl) {
        this.showWsdl = showWsdl;
    }
    
    public String getWsdl() {
        return mbean.loadWSDL().replace("<", "&lt;");
    }
    
    public List<QName> getInterfaces() {
        QName[] names = mbean.getInterfaces();
        if (names != null) {
            return Arrays.asList(names);
        } else {
            return Collections.emptyList(); 
        }
    }

}