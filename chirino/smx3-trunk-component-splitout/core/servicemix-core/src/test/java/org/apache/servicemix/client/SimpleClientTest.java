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
package org.apache.servicemix.client;

import java.io.StringReader;

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.components.util.OutBinding;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;

/**
 * @version $Revision$
 */
public class SimpleClientTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(SimpleClientTest.class);

    protected JBIContainer container;
    protected OutBinding out;
    protected ServiceMixClient client;

    protected void setUp() throws Exception {
        container = new JBIContainer();
        container.setEmbedded(true);
        container.init();
        container.start();
        out = new OutBinding() {
            protected void process(MessageExchange exchange, NormalizedMessage message) throws MessagingException {
                LOG.info("Received: " + message);
                done(exchange);
            }
        };
        ActivationSpec as = new ActivationSpec("out", out);
        as.setService(new QName("out"));
        container.activateComponent(as);
        client = new DefaultServiceMixClient(container);
    }

    protected void tearDown() throws Exception {
        container.shutDown();
    }

    /**
     * Simple test
     * 
     * @throws Exception
     */
    public void testSimple() throws Exception {
        InOnly exchange = client.createInOnlyExchange();
        NormalizedMessage message = exchange.getInMessage();
        message.setProperty("name", "john");
        message.setContent(new StreamSource(new StringReader("<hello>world</hello>")));
        QName service = new QName("out");
        exchange.setService(service);
        client.sendSync(exchange);
    }
}
