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
package org.apache.servicemix.smpp;

import ie.omk.smpp.Address;
import ie.omk.smpp.Connection;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SubmitSM;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.id.IdGenerator;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.messaging.MessageExchangeFactoryImpl;
import org.apache.servicemix.jbi.messaging.MessageExchangeSupport;
import org.apache.servicemix.smpp.marshaler.DefaultSmppMarshaler;
import org.apache.servicemix.smpp.marshaler.SmppMarshalerSupport;

import junit.framework.TestCase;

/**
 * @author: lhein
 */
public class SmppMarshalerTest extends TestCase {

    private static final String SOURCE = "0123456789";
    private static final String DESTINATION = "9876543210";
    private static final String TEXT = "This is a smpp test...";
    private static final String NPI = "0";
    private static final String TON = "1";
    
    private static final String MSG_VALID = "<message><source>" + SOURCE + "</source><destination>" + DESTINATION + "</destination><text>" + TEXT + "</text><npi>" + NPI + "</npi><ton>" + TON + "</ton></message>";
    private static final String MSG_INVALID = "This will definitely break the test...";
    private static final String MSG_INVALID_DEST = "<message><source>" + SOURCE + "</source><text>" + TEXT + "</text><npi>" + NPI + "</npi><ton>" + TON + "</ton></message>";
    private static final String MSG_INVALID_TON = "<message><source>" + SOURCE + "</source><destination>" + DESTINATION + "</destination><text>" + TEXT + "</text><npi>" + NPI + "</npi></message>";
    private static final String MSG_INVALID_NPI = "<message><source>" + SOURCE + "</source><destination>" + DESTINATION + "</destination><text>" + TEXT + "</text><ton>" + TON + "</ton></message>";
        
    private SmppMarshalerSupport marshaler;
    private SourceTransformer st;
    private MessageExchangeFactory factory;
    private Connection conn;
    
    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception {
        this.marshaler = new DefaultSmppMarshaler();
        this.st = new SourceTransformer();
        this.factory = new MessageExchangeFactoryImpl(new IdGenerator(), new AtomicBoolean(false));
        this.conn = new Connection("localhost", 1977, false);
    }

    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    public void tearDown() throws Exception {
        this.marshaler = null;
        this.st = null;
        this.factory = null;
        this.conn.closeLink();
        this.conn = null;
    }

    private MessageExchange prepareMessageExchange(URI pattern, String inMsgContent) {
        MessageExchange ex = null;
        
        try {
            ex = this.factory.createExchange(pattern);
            NormalizedMessage nmsg = ex.createMessage();
            nmsg.setContent(new StringSource(inMsgContent));
            ex.setMessage(nmsg, "in");
        } catch (Exception e) {
            ex = null;
        }        
        
        return ex;
    }
    
    private SMPPPacket preparePacketValid() {
        SMPPPacket packet = null;
        
        try {
          packet = this.conn.newInstance(SMPPPacket.SUBMIT_SM);
          packet.setMessageText(TEXT);
          packet.setSource(new Address(Integer.parseInt(TON), Integer.parseInt(NPI), SOURCE));
          packet.setDestination(new Address(Integer.parseInt(TON), Integer.parseInt(NPI), DESTINATION));
        } catch (Exception ex) {
          ex.printStackTrace();
          packet = null;
        }        
                
        return packet;
    }
    
    private SMPPPacket preparePacketInvalid() {
        SMPPPacket packet = null;
        
        try {
          packet = this.conn.newInstance(SMPPPacket.SUBMIT_SM);
        } catch (Exception ex) {
          ex.printStackTrace();
          packet = null;
        }        
                
        return packet;
    }
    
    
    
    /**
     * ********************* HERE THE TEST METHODS **************************
     */
    
    public void testFromNMSValid() {
        MessageExchange exch = prepareMessageExchange(MessageExchangeSupport.IN_ONLY, MSG_VALID);
        try {
           SubmitSM sm = marshaler.fromNMS(this.conn, exch, exch.getMessage("in"));
           
           assertEquals("The message text is not the same. Expected: " + TEXT + ", Found: " + sm.getMessageText(), sm.getMessageText(), TEXT);
           assertEquals("The destination is not the same. Expected: " + DESTINATION + ", Found: " + sm.getDestination().getAddress(), sm.getDestination().getAddress(), DESTINATION);
           assertEquals("The NPI is not the same. Expected: " + NPI + ", Found: " + sm.getDestination().getNPI(), "" + sm.getDestination().getNPI(), NPI);
           assertEquals("The TON is not the same. Expected: " + TON + ", Found: " + sm.getDestination().getTON(), "" + sm.getDestination().getTON(), TON);
        } catch (TransformerException ex) {
           fail(ex.getMessage());
        }        
    }
    
    public void testFromNMSNullConnection() {
        MessageExchange exch = prepareMessageExchange(MessageExchangeSupport.IN_ONLY, MSG_VALID);
        try {
           marshaler.fromNMS(null, exch, exch.getMessage("in"));
           fail("Seems we processed a message with null connection...");
        } catch (TransformerException ex) {
           // all fine
        }        
    }
    
    public void testFromNMSNullExchange() {
        try {
           marshaler.fromNMS(this.conn, null, null);
           fail("Seems we processed a message with null exchange...");
        } catch (TransformerException ex) {
           // all fine
        }        
    }
        
    public void testFromNMSInvalid() {
        MessageExchange exch = prepareMessageExchange(MessageExchangeSupport.IN_ONLY, MSG_INVALID);
        try {
           marshaler.fromNMS(this.conn, exch, exch.getMessage("in"));
           fail("Seems that we processed a invalid message...");
        } catch (TransformerException ex) {
           // all fine
        }        
    }
    
    public void testFromNMSInvalidDest() {
        MessageExchange exch = prepareMessageExchange(MessageExchangeSupport.IN_ONLY, MSG_INVALID_DEST);
        try {
           marshaler.fromNMS(this.conn, exch, exch.getMessage("in"));
           fail("Seems that we processed a invalid message...");
        } catch (TransformerException ex) {
           // all fine
        }        
    }
    
    public void testFromNMSInvalidTon() {
        MessageExchange exch = prepareMessageExchange(MessageExchangeSupport.IN_ONLY, MSG_INVALID_TON);
        try {
           marshaler.fromNMS(this.conn, exch, exch.getMessage("in"));
           fail("Seems that we processed a invalid message...");
        } catch (TransformerException ex) {
           // all fine
        }        
    }
    
    public void testFromNMSInvalidNpi() {
        MessageExchange exch = prepareMessageExchange(MessageExchangeSupport.IN_ONLY, MSG_INVALID_NPI);
        try {
           marshaler.fromNMS(this.conn, exch, exch.getMessage("in"));
           fail("Seems that we processed a invalid message...");
        } catch (TransformerException ex) {
           // all fine
        }        
    }
    
    public void testToNMSValid() {
        SMPPPacket packet = preparePacketValid();
        
        try {
           MessageExchange ex = this.factory.createExchange(MessageExchangeSupport.IN_ONLY);
           NormalizedMessage nmsg = ex.createMessage();
           ex.setMessage(nmsg, "in");
           marshaler.toNMS(nmsg, packet);
           assertEquals("Message not correct. Expected: " + MSG_VALID + ", Found: " + st.contentToString(nmsg), MSG_VALID, st.contentToString(nmsg));
        } catch (Exception ex) {
           ex.printStackTrace();
           fail(ex.getMessage());
        }        
    }
    
    public void testToNMSInvalid() {
        SMPPPacket packet = preparePacketInvalid();
        
        try {
           MessageExchange ex = this.factory.createExchange(MessageExchangeSupport.IN_ONLY);
           NormalizedMessage nmsg = ex.createMessage();
           ex.setMessage(nmsg, "in");
           marshaler.toNMS(nmsg, packet);
           fail("Seems we processed a invalid smpp packet...");
        } catch (Exception ex) {
           // all fine
        }        
    }
    
    public void testToNMSNullPacket() {
        SMPPPacket packet = null;
        
        try {
           MessageExchange ex = this.factory.createExchange(MessageExchangeSupport.IN_ONLY);
           NormalizedMessage nmsg = ex.createMessage();
           ex.setMessage(nmsg, "in");
           marshaler.toNMS(nmsg, packet);
           fail("Seems we processed a null smpp packet...");
        } catch (Exception ex) {
           // all fine
        }        
    }
    
    public void testToNMSNullMsg() {
        SMPPPacket packet = preparePacketInvalid();;
        
        try {
           marshaler.toNMS(null, packet);
           fail("Seems we processed a smpp packet with a null normalized message...");
        } catch (Exception ex) {
           // all fine
        }        
    }
}
