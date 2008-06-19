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
package org.apache.servicemix.components.xslt;

import javax.jbi.messaging.InOut;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.TestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.w3c.dom.Node;

/**
 * @author rbuckland
 * @version $Revision$
 */
public class XsltParameterTest extends TestSupport {

    public void testSendMessagesToJmsThenReceiveItTransformItThenRepublishAndReceive() throws Exception {
       QName service = new QName("http://servicemix.org/cheese/", "transformer");

        InOut exchange = client.createInOutExchange();
        exchange.setService(service);

        NormalizedMessage message = exchange.getInMessage();
        message.setContent(new StringSource(createMessageXmlText(777888)));
        client.sendSync(exchange);

        NormalizedMessage outMessage = exchange.getOutMessage();

        Source content = outMessage.getContent();
        Node node = transformer.toDOMNode(content);

        String value = textValueOfXPath(node, "//param");
        String integer = textValueOfXPath(node, "//integer");

        assertTrue(value.equals("cheeseyCheese"));
        assertTrue(integer.equals("4002"));

    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/xslt/servicemix-parameter-test.xml");
    }
}
