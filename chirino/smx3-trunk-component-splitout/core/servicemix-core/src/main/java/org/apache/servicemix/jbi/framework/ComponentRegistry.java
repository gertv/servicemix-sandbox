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
package org.apache.servicemix.jbi.framework;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.component.Component;

/**
 * Registry for Components
 *
 * @version $Revision$
 */
public class ComponentRegistry {
    
    private Map<ComponentNameSpace, ComponentMBeanImpl> idMap = new LinkedHashMap<ComponentNameSpace, ComponentMBeanImpl>();
    private boolean runningStateInitialized;
    private Registry registry;
    
    
    protected ComponentRegistry(Registry reg) {
        this.registry = reg;
    }
    /**
     * Register a Component
     * @param name
     * @param description
     * @param component
     * @param dc
     * @param binding
     * @param service
     * @return an associated ComponentConnector or null if it already exists
     */
    public synchronized ComponentMBeanImpl registerComponent(
                    ComponentNameSpace name, 
                    String description, 
                    Component component,
                    boolean binding, 
                    boolean service,
                    String[] sharedLibraries) {
        ComponentMBeanImpl result = null;
        if (!idMap.containsKey(name)) {
            result = new ComponentMBeanImpl(registry.getContainer(), name, description, component, binding, service, sharedLibraries);
            idMap.put(name, result);
        }
        return result;
    }
    
    /**
     * start components
     * @throws JBIException
     */
    public synchronized void start() throws JBIException {
        if (!setInitialRunningStateFromStart()) {
            for (Iterator<ComponentMBeanImpl> i = getComponents().iterator(); i.hasNext();) {
                ComponentMBeanImpl lcc = i.next();
                lcc.doStart();
            }
        }
    }

    /**
     * stop components
     * @throws JBIException 
     * 
     * @throws JBIException
     */
    public synchronized void stop() throws JBIException  {
        for (Iterator<ComponentMBeanImpl> i = getReverseComponents().iterator(); i.hasNext();) {
            ComponentMBeanImpl lcc = i.next();
            lcc.doStop();
        }
        runningStateInitialized = false;
    }
    
    /**
     * shutdown all Components
     * 
     * @throws JBIException
     */
    public synchronized void shutDown() throws JBIException {
        for (Iterator<ComponentMBeanImpl> i = getReverseComponents().iterator(); i.hasNext();) {
            ComponentMBeanImpl lcc = i.next();
            lcc.persistRunningState();
            lcc.doShutDown();
        }
    }
    
    private Collection<ComponentMBeanImpl> getReverseComponents() {
        synchronized (idMap) {
            List<ComponentMBeanImpl> l = new ArrayList<ComponentMBeanImpl>(idMap.values());
            Collections.reverse(l);
            return l;
        }
    }

    
    /**
     * Deregister a Component
     * @param component
     * @return the deregistered component
     */
    public synchronized void deregisterComponent(ComponentMBeanImpl component) {
        idMap.remove(component.getComponentNameSpace());
    }
    
    /**
     * Get a registered ComponentConnector from it's id
     * @param id
     * @return the ComponentConnector or null
     */
    public ComponentMBeanImpl getComponent(ComponentNameSpace id) {
        synchronized (idMap) {
            return idMap.get(id);
        }
    }
    
    /**
     * 
     * @return Collection of ComponentConnectors held by the registry
     */
    public Collection<ComponentMBeanImpl> getComponents() {
        synchronized (idMap) {
            return new ArrayList<ComponentMBeanImpl>(idMap.values());
        }
    }

    private boolean setInitialRunningStateFromStart() throws JBIException {
        boolean result = !runningStateInitialized;
        if (!runningStateInitialized) {
            runningStateInitialized = true;
            for (Iterator<ComponentMBeanImpl> i = getComponents().iterator(); i.hasNext();) {
                ComponentMBeanImpl lcc = i.next();
                if (!lcc.isPojo() && !registry.isContainerEmbedded()) {
                    lcc.setInitialRunningState();
                } else {
                    lcc.doStart();
                }
            }
        }
        return result;
    }
}