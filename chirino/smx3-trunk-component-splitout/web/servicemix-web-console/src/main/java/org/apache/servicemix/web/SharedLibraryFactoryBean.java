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
package org.apache.servicemix.web;

import org.apache.servicemix.web.filter.Factory;
import org.apache.servicemix.web.model.Registry;
import org.springframework.beans.factory.FactoryBean;

public class SharedLibraryFactoryBean implements FactoryBean {

    private Registry registry;
    
    public Object getObject() throws Exception {
        return new Factory() {
            private String name;
            @SuppressWarnings("unused")
            public void setName(String name) {
                this.name = name;
            }
            public Object getBean() {
                return registry.getSharedLibrary(name);
            }
        };
    }

    public Class getObjectType() {
        return Factory.class;
    }

    public boolean isSingleton() {
        return false;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

}
