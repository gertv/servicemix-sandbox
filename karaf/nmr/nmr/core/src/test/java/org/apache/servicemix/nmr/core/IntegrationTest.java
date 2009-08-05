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

import java.util.Map;

import org.apache.servicemix.nmr.api.*;
import org.apache.servicemix.nmr.api.service.ServiceHelper;
import junit.framework.TestCase;

/**
 * Integration test
 */
public class IntegrationTest extends TestCase {

    private NMR nmr;

    public void setUp() {
        ServiceMix smx = new ServiceMix();
        smx.init();
        nmr = smx;
    }

    public void testSendExchangeToEndpointUsingClient() throws Exception {
        MyEndpoint endpoint = new MyEndpoint();
        nmr.getEndpointRegistry().register(endpoint, ServiceHelper.createMap(Endpoint.NAME, "id"));
        Channel client = nmr.createChannel();
        Exchange e = client.createExchange(Pattern.InOnly);
        e.setTarget(nmr.getEndpointRegistry().lookup(ServiceHelper.createMap(Endpoint.NAME, "id")));
        e.getIn().setBody("Hello");
        boolean res = client.sendSync(e);
        assertTrue(res);
        assertNotNull(endpoint.getExchange());
        assertEquals(Status.Done, e.getStatus());
    }
    
    public void testSendExchangeToWiredEndpointUsingClient() throws Exception {
        MyEndpoint endpoint = new MyEndpoint();
        Map<String, Object> target = ServiceHelper.createMap(Endpoint.NAME, "id");
        Map<String, Object> wire = ServiceHelper.createMap(Endpoint.NAME, "wire");
        //register the endpoint and a wire to the endpoint
        nmr.getEndpointRegistry().register(endpoint, target);
        nmr.getWireRegistry().register(ServiceHelper.createWire(wire, target));
        
        Channel client = nmr.createChannel();
        Exchange e = client.createExchange(Pattern.InOnly);
        e.setTarget(nmr.getEndpointRegistry().lookup(wire));
        e.getIn().setBody("Hello");
        boolean res = client.sendSync(e);
        assertTrue(res);
        assertNotNull(endpoint.getExchange());
        assertEquals(Status.Done, e.getStatus());        
    }

    public static class MyEndpoint implements Endpoint {

        private Channel channel;
        private Exchange exchange;

        public void setChannel(Channel channel) {
            this.channel = channel;
        }

        public void process(Exchange exchange) {
            this.exchange = exchange;
            exchange.setStatus(Status.Done);
            channel.send(exchange);
        }

        public Exchange getExchange() {
            return exchange;
        }
    }
}
