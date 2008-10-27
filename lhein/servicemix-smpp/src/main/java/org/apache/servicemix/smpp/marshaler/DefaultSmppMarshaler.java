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

import ie.omk.smpp.Address;
import ie.omk.smpp.Connection;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SubmitSM;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Marshals SMPP messages into and out of NMS messages
 * 
 * @author Rohit Joshi
 */
public class DefaultSmppMarshaler implements SmppMarshalerSupport {
    
    private static final transient Log LOG = LogFactory.getLog(DefaultSmppMarshaler.class);
    
    private static final String TAG_MESSAGE     = "message";
    private static final String TAG_SOURCE      = "source";
    private static final String TAG_DESTINATION = "destination";
    private static final String TAG_TEXT        = "text";
    private static final String TAG_TON         = "ton";
    private static final String TAG_NPI         = "npi";
    
    private static final String TAG_MESSAGE_OPEN = "<" + TAG_MESSAGE + ">";
    private static final String TAG_MESSAGE_CLOSE = "</" + TAG_MESSAGE + ">";
    private static final String TAG_SOURCE_OPEN = "<" + TAG_SOURCE + ">";
    private static final String TAG_SOURCE_CLOSE = "</" + TAG_SOURCE + ">";
    private static final String TAG_DESTINATION_OPEN = "<" + TAG_DESTINATION + ">";
    private static final String TAG_DESTINATION_CLOSE = "</" + TAG_DESTINATION + ">";
    private static final String TAG_TEXT_OPEN = "<" + TAG_TEXT + ">";
    private static final String TAG_TEXT_CLOSE = "</" + TAG_TEXT + ">";
    private static final String TAG_TON_OPEN = "<" + TAG_TON + ">";
    private static final String TAG_TON_CLOSE = "</" + TAG_TON + ">";
    private static final String TAG_NPI_OPEN = "<" + TAG_NPI + ">";
    private static final String TAG_NPI_CLOSE = "</" + TAG_NPI + ">";

    private SourceTransformer transformer = new SourceTransformer();
    
    /*
     * (non-Javadoc)
     * @see org.apache.servicemix.smpp.marshaler.SmppMarshalerSupport#toNMS(javax.jbi.messaging.NormalizedMessage, ie.omk.smpp.message.SMPPPacket)
     */
    public void toNMS(NormalizedMessage normalizedMessage, SMPPPacket packet) throws MessagingException {
        
        String text = packet.getMessageText();
        String sourceAddr = packet.getSource().getAddress();
        String destinationAddr = packet.getDestination().getAddress();
        
        int npi = packet.getSource().getNPI();
        int ton = packet.getSource().getTON();
        
        if (text != null) {
            StringBuffer data = new StringBuffer(); 

            // build the message content
            data.append(TAG_MESSAGE_OPEN);
            
            data.append(TAG_SOURCE_OPEN);
            data.append(sourceAddr);
            data.append(TAG_SOURCE_CLOSE);
            
            data.append(TAG_DESTINATION_OPEN);
            data.append(destinationAddr);
            data.append(TAG_DESTINATION_CLOSE);
            
            data.append(TAG_TEXT_OPEN);
            data.append(text);
            data.append(TAG_TEXT_CLOSE);
            
            data.append(TAG_NPI_OPEN);
            data.append(npi);
            data.append(TAG_NPI_CLOSE);
            
            data.append(TAG_TON_OPEN);
            data.append(ton);
            data.append(TAG_TON_CLOSE);
            
            data.append(TAG_MESSAGE_CLOSE);
            
            // put the content to message body
            normalizedMessage.setContent(new StringSource(data.toString()));
        } else {
            LOG.debug("Received message without text content. Skipped.\nPacket: " + packet.toString());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.servicemix.smpp.marshaler.SmppMarshalerSupport#fromNMS(ie.omk.smpp.Connection, javax.jbi.messaging.MessageExchange, javax.jbi.messaging.NormalizedMessage)
     */
    public SubmitSM fromNMS(Connection conn, MessageExchange exchange, NormalizedMessage normalizedMessage)
        throws TransformerException {
        
        SubmitSM sm = null;
        
        String source = null;
        String destination = null;
        String text = "";
        String ton = null;
        String npi = null;

        try {
            // convert message content to DOM document
            Document doc = transformer.toDOMDocument(normalizedMessage);

            // normalize text representation
            doc.getDocumentElement().normalize();

            NodeList node = doc.getElementsByTagName(TAG_SOURCE);
            
            if (node != null && node.getLength() > 0) {
                source = node.item(0).getChildNodes().item(0).getNodeValue();
                LOG.debug(TAG_SOURCE + ": " + source);
            }
            
            node = doc.getElementsByTagName(TAG_DESTINATION);
            if (node != null && node.getLength() > 0) {
                destination = node.item(0).getChildNodes().item(0).getNodeValue();
                LOG.debug(TAG_DESTINATION + ": " + destination);
            }
            
            node = doc.getElementsByTagName(TAG_TEXT);
            if (node != null && node.getLength() > 0) {
                text = node.item(0).getChildNodes().item(0).getNodeValue();
                LOG.debug(TAG_TEXT + ": " + text);
            }
            
            node = doc.getElementsByTagName(TAG_TON);
            if (node != null && node.getLength() > 0) {
                ton = node.item(0).getChildNodes().item(0).getNodeValue();
                LOG.debug(TAG_TON + ": " + ton);
            }
            
            node = doc.getElementsByTagName(TAG_NPI);
            if (node != null && node.getLength() > 0) {
                npi = node.item(0).getChildNodes().item(0).getNodeValue();
                LOG.debug(TAG_NPI + ": " + npi);
            }
            
            // check for mandatory attribute "destination"
            if (destination == null) {
                throw new TransformerException("Invalid message content. Missing tag: " + TAG_DESTINATION);
            }

            // check for mandatory attribute "ton"            
            if (ton == null) {
                throw new TransformerException("Invalid message content. Missing tag: " + TAG_TON);
            }

            // check for mandatory attribute "npi"            
            if (npi == null) {
                throw new TransformerException("Invalid message content. Missing tag: " + TAG_NPI);
            }
            
            try {
                // create the submit sm object
                sm = (SubmitSM)conn.newInstance(SMPPPacket.SUBMIT_SM);
                
                // create the address
                Address addr = new Address();
                // configure the address
                addr.setAddress(destination);
                addr.setNPI(Integer.parseInt(npi));
                addr.setTON(Integer.parseInt(ton));
                // configure the submit sm to use the address
                sm.setDestination(addr);
                // set the short message text
                sm.setMessageText(text);
            } catch (Exception ex) {
                throw new TransformerException(ex);
            }
        } catch (Exception e) {
            throw new TransformerException(e);
        }

        return sm;
    }
}
