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
package org.apache.servicemix.jbi.deployment;

import javax.xml.namespace.QName;

/**
 * Specifies either an interface name or a service and endpoint name.
 *
 * @version $Revision$
 */
public class Consumes {
    private QName interfaceName;
    private QName serviceName;
    private String endpointName;
    private String linkType = "standard";

    public QName getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(QName interfaceName) {
        this.interfaceName = interfaceName;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public boolean isStandardLink() {
        return linkType != null && linkType.equals("standard");
    }

    public boolean isSoftLink() {
        return linkType != null && linkType.equals("soft");
    }

    public boolean isHardLink() {
        return linkType != null && linkType.equals("hard");
    }
}
