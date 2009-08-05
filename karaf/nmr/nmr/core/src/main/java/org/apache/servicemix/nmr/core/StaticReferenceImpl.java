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
package org.apache.servicemix.nmr.core;

import java.util.List;

import org.w3c.dom.Document;

import org.apache.servicemix.nmr.api.EndpointRegistry;
import org.apache.servicemix.nmr.api.internal.InternalEndpoint;
import org.apache.servicemix.nmr.api.internal.InternalReference;

/**
 * @version $Revision: $
 * @since 4.0
 */
public class StaticReferenceImpl implements InternalReference {

    private final List<InternalEndpoint> endpoints;

    public StaticReferenceImpl(List<InternalEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    /**
     * Get the list of matching endpoints
     *
     * @return the list of matching endpoints
     */
    public List<InternalEndpoint> getEndpoints() {
        return endpoints;
    }

    /**
     * Obtains an xml document describing this endpoint reference.
     *
     * @return
     */
    public Document toXml() {
        // TODO: implement
        return null;
    }

    /**
     * Choose an Endpoint to be used as the target for an exchange
     *
     * @return an endpoint that will be used as the physical target
     */
    public Iterable<InternalEndpoint> choose(EndpointRegistry registry) {
        return endpoints;
    }

}
