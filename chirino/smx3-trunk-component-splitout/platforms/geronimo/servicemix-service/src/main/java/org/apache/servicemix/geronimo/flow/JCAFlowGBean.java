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
package org.apache.servicemix.geronimo.flow;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.servicemix.jbi.nmr.flow.jca.JCAFlow;

/**
 * Wrapper GBean for ServiceMix JCAFlow Bean
 */
public class JCAFlowGBean extends JCAFlow {

    private final Kernel kernel;

    public JCAFlowGBean(Kernel kernel) {
        this.kernel = kernel;
    }
    
    public static final GBeanInfo GBEAN_INFO;
    
    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("JCAFlow",
                JCAFlowGBean.class, "ServiceMixFlow");
        
        infoFactory.addAttribute("jmsURL", String.class, true);
        infoFactory.addAttribute("broadcastDestinationName", String.class, true);
        infoFactory.setConstructor(new String[] { "kernel" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
