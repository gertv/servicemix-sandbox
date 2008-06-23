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
package org.apache.servicemix.components.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.mock.MockMessageExchange;
import org.apache.servicemix.tck.mock.MockNormalizedMessage;

/**
 * Test cases for {@link DefaultFileMarshaler}
 */
public class DefaultFileMarshalerTest extends TestCase {
    
    private static final String MESSAGE = "<test>l'élève est à l'école</test>";
    private static final SourceTransformer TRANSFORMER = new SourceTransformer();
    private DefaultFileMarshaler marshaler = new DefaultFileMarshaler();
    
    public void testReadExplicitEncoding() throws Exception {
        //create a mock exchange
        MessageExchange exchange = createMockExchange();
        
        //have the marshaler read the message as ISO-8859-1, encoded as ISO-8859-1
        ByteArrayInputStream stream = new ByteArrayInputStream(Charset.forName("ISO-8859-1").encode(MESSAGE).array());
        marshaler.setEncoding("ISO-8859-1");
        marshaler.readMessage(exchange, exchange.getMessage("in"), stream, "/any/thing/will/do");
        
        //make sure that the end result is the same and no exception is thrown
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + MESSAGE, TRANSFORMER.contentToString(exchange.getMessage("in")));
    }
    
    public void testWriteExplicitEncoding() throws Exception {
        //create a mock exchange
        MessageExchange exchange = createMockExchange();
        exchange.getMessage("in").setContent(new StringSource(MESSAGE));
        
        //have the marshaler read the message as ISO-8859-1, encoded as ISO-8859-1
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        marshaler.setEncoding("ISO-8859-1");
        marshaler.writeMessage(exchange, exchange.getMessage("in"), stream, "/any/thing/will/do");
        
        //make sure that the end result is the same and no exception is thrown
        assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + MESSAGE, stream.toString("ISO-8859-1"));
    }    

    private MessageExchange createMockExchange() throws MessagingException {
        MessageExchange exchange = new MockMessageExchange();
        exchange.setMessage(new MockNormalizedMessage(), "in");
        return exchange;
    }

}
