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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * 
 * @version $Revision$
 */
public final class ServiceMixJMXUtil {

    private static String containerName = "org.apache.servicemix:ContainerName=ServiceMix,Type=JBIContainer";
    
    private static MBeanServerConnection getServerConnection() throws MalformedURLException, IOException 
    {
        
        String jndiPath = "jmxrmi";
        String urlstring = "service:jmx:rmi:///jndi/rmi://localhost:1099/" + jndiPath;
        JMXServiceURL url = new JMXServiceURL(urlstring) ;
         
        String username ="smx";
        String password="smx";
        Map map = new HashMap();
        map.put("jmx.remote.credentials", new String[] {username, password});
         
        JMXConnector connector = JMXConnectorFactory.connect(url, map);
         
        MBeanServerConnection serverConnection = connector.getMBeanServerConnection();
        return serverConnection;
        
    }
    
    public static String getServerState() throws Exception {
        return (String)getServerConnection().getAttribute(new ObjectName(containerName), "currentState");
    }
    
    public static void shutdownServer() throws Exception {
        getServerConnection().invoke(new ObjectName(containerName), "shutDown", new Object[]{} , new String[]{});
    }
     
    public static int getDeploymentCount() throws Exception {
        
        int count = 0;
        
        MBeanServerConnection serverConnection = getServerConnection();
        
        Set queryMBeans = serverConnection.queryMBeans(new ObjectName("org.apache.servicemix:ContainerName=ServiceMix,Type=SystemService,*"), null);
        count += queryMBeans.size();
        
        queryMBeans = serverConnection.queryMBeans(new ObjectName("org.apache.servicemix:ContainerName=ServiceMix,Type=SharedLibrary,*"), null);
        count += queryMBeans.size();
        
        queryMBeans = serverConnection.queryMBeans(new ObjectName("org.apache.servicemix:ContainerName=ServiceMix,Type=ServiceAssembly,*"), null);
        count += queryMBeans.size();
        
        queryMBeans = serverConnection.queryMBeans(new ObjectName("org.apache.servicemix:ContainerName=ServiceMix,Type=ServiceUnit,*"), null);
        count += queryMBeans.size();
        
        queryMBeans = serverConnection.queryMBeans(new ObjectName("org.apache.servicemix:ContainerName=ServiceMix,Type=Component,*"), null);
        count += queryMBeans.size();
        
        return count;
        
    }
    
    public static void deploy(String filename) {
        
        try {
            
            getServerConnection().invoke(new ObjectName("org.apache.servicemix:ContainerName=ServiceMix,Type=SystemService,Name=AdminCommandsService"), "installArchive", new Object[]{filename}, new String[]{String.class.getName()});
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    
}
