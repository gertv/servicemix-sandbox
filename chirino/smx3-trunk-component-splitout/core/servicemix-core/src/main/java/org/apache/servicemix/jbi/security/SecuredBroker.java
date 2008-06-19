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
package org.apache.servicemix.jbi.security;

import java.security.Principal;
import java.util.Set;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.nmr.DefaultBroker;
import org.apache.servicemix.jbi.security.acl.AuthorizationMap;

/**
 * 
 * @author gnodet
 * @org.apache.xbean.XBean
 */
public class SecuredBroker extends DefaultBroker {

    private static final Log LOG = LogFactory.getLog(SecuredBroker.class);
    private AuthorizationMap authorizationMap;
    
    public SecuredBroker() {
    }
    
    public SecuredBroker(AuthorizationMap authorizationMap) {
        this.authorizationMap = authorizationMap;
    }

    /**
     * @return the authorizationMap
     */
    public AuthorizationMap getAuthorizationMap() {
        return authorizationMap;
    }

    /**
     * @param authorizationMap the authorizationMap to set
     */
    public void setAuthorizationMap(AuthorizationMap authorizationMap) {
        this.authorizationMap = authorizationMap;
    }

    public void sendExchangePacket(MessageExchange me) throws JBIException {
        LOG.debug("send exchange with secure broker");
        MessageExchangeImpl exchange = (MessageExchangeImpl) me;
        if (exchange.getRole() == Role.PROVIDER && exchange.getDestinationId() == null) {
            resolveAddress(exchange);
            ServiceEndpoint se = exchange.getEndpoint();
            if (se != null) {
                LOG.debug("service name :" + se.getServiceName());
                LOG.debug("operation name :" + me.getOperation());
                Set<Principal> acls = authorizationMap.getAcls(se, me.getOperation());
                if (!acls.contains(GroupPrincipal.ANY)) { 
                    Subject subject = exchange.getMessage("in").getSecuritySubject();
                    if (subject == null) {
                        throw new SecurityException("User not authenticated");
                    }
                    LOG.debug("authorization for " + subject);
                    acls.retainAll(subject.getPrincipals());
                    if (acls.size() == 0) {
                        throw new SecurityException("Endpoint is not authorized for this user");
                    }
                }
            }
        }
        super.sendExchangePacket(me);
    }

}
