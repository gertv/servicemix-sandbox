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
package org.apache.servicemix.smpp.marshaler;

import ie.omk.smpp.Connection;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SubmitSM;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.TransformerException;

/**
 * @author lhein
 */
public interface SmppMarshalerSupport {
    
    /**
     * converts the received smpp packet into a normalized message
     * 
     * @param message   the message to fill the packet into
     * @param packet    the received packet to convert into message
     * @throws MessagingException       on errors
     */
    void toNMS(NormalizedMessage message, SMPPPacket packet) throws MessagingException;
    
    /**
     * converts a normalized message from the nmr into smpp packets
     * 
     * @param connection        the smpp connection
     * @param exchange          the message exchange
     * @param message           the normalized message
     * @return                  a submit sm object
     * @throws TransformerException     on errors in transformation
     */
    SubmitSM fromNMS(Connection connection, MessageExchange exchange, NormalizedMessage message) throws TransformerException;
}