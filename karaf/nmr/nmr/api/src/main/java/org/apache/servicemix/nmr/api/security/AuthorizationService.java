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
package org.apache.servicemix.nmr.api.security;

import java.security.Principal;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * The AuthorizationService interface allows the NMR to retrieve the
 * Access Control List for a given combination of endpoint / operation.
 *
 * This is used to secure access to a given endpoint. 
 *  
 */
public interface AuthorizationService {

    /**
     * Retrieve the Access Control List for a given endpoint and operation.
     * The endpoint is identified by its ID (usually a combination of
     * service QName and endpoint in the JBI case).
     * 
     * @param endpoint the endpoint identifier
     * @param operation the operation invoked or null
     * @return a set of GroupPrincipal allowed to invoke the endpoint / operation
     */
    Set<GroupPrincipal> getAcls(String endpoint, QName operation);

}
