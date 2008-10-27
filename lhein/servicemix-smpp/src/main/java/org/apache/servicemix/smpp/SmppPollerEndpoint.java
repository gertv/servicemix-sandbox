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

import ie.omk.smpp.AlreadyBoundException;
import ie.omk.smpp.Connection;
import ie.omk.smpp.NotBoundException;
import ie.omk.smpp.event.ConnectionObserver;
import ie.omk.smpp.event.ReceiverExitEvent;
import ie.omk.smpp.event.SMPPEvent;
import ie.omk.smpp.message.SMPPPacket;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.common.endpoints.ConsumerEndpoint;
import org.apache.servicemix.smpp.marshaler.DefaultSmppMarshaler;
import org.apache.servicemix.smpp.marshaler.SmppMarshalerSupport;

/**
 * A polling component which bind with SMSC and receive SMPP messages and sends
 * the SMPPs into the JBI bus as messages.
 * 
 * @org.apache.xbean.XBean element="poller"
 * @author Rohit Joshi
 */
public class SmppPollerEndpoint extends ConsumerEndpoint implements SmppEndpointType, ConnectionObserver {

    private static final transient Log LOG = LogFactory.getLog(SmppPollerEndpoint.class);

    private static final int SMPP_DEFAULT_PORT = 2775;
    private static final String DEFAULT_RESOURCE = "ServiceMix";
    
    private Connection connection;
    
    private int port = SMPP_DEFAULT_PORT;
    
    private long bindRetryCycle = 60000;
    
    private String host;
    private String user;
    private String password;
    private String resource = DEFAULT_RESOURCE;

    private SmppMarshalerSupport marshaler = new DefaultSmppMarshaler();
    
    /*
     * (non-Javadoc)
     * @see org.apache.servicemix.common.endpoints.PollingEndpoint#start()
     */
    @Override
    public synchronized void start() throws Exception {
        super.start();

        // connect to the smpp host
        connect();
    }

    /*
     * (non-Javadoc)
     * @see org.apache.servicemix.common.endpoints.PollingEndpoint#stop()
     */
    @Override
    public synchronized void stop() throws Exception {
        // disconnect
        disconnect();
        
        super.stop();
    }

    /*
     * (non-Javadoc)
     * @see org.apache.servicemix.common.endpoints.ConsumerEndpoint#validate()
     */
    @Override
    public void validate() throws DeploymentException {
        super.validate();

        // check for valid port number
        if (this.port <=0) {
            LOG.warn("Invalid smpp port specified. Switching to default port: " + SMPP_DEFAULT_PORT);
            this.port = SMPP_DEFAULT_PORT;
        }
        
        // check for valid resource
        if (this.resource == null || this.resource.trim().length()<=0) {
            LOG.warn("Invalid smpp resource specified. Switching to default resource: " + DEFAULT_RESOURCE);
            this.resource = DEFAULT_RESOURCE;
        }
        
        // check for valid host
        if (this.host == null || this.host.trim().length()<=0) {
            throw new IllegalArgumentException("The specified host name is not valid.");
        }
        
        // check for valid user
        if (this.user == null || this.user.trim().length()<=0) {
            throw new IllegalArgumentException("The specified user name is not valid.");
        }
        
        // check for valid password
        if (this.password == null || this.password.trim().length()<=0) {
            throw new IllegalArgumentException("The specified password is not valid.");
        }
    }

    /**
     * connects to the smpp host for polling
     */
    private void connect() {
        try {
            // open the connection
            this.connection = new Connection(this.host, this.port, true);

            // register myself as an observer
            this.connection.addObserver(this);
        } catch (UnknownHostException uhe) {
            LOG.error("Error connecting to host: " + this.host + ":" + this.port, uhe);
            return;
        }

        boolean retry = true;

        while (retry) {
            try {
                // bind
                this.connection.bind(Connection.RECEIVER, this.user, this.password, null);
                // bound
                retry = false;
            } catch (AlreadyBoundException abex) {
                LOG.warn("Already bound.", abex);
                retry = false;
            } catch (Exception ioe) {
                try {
                    logger.warn("Bind failed. Will retry after " + this.bindRetryCycle + " ms");
                    Thread.sleep(this.bindRetryCycle);
                } catch (InterruptedException ie) {
                    // ignore this exception
                }
            }
        }
    }

    /**
     * disconnects from snmp host
     */
    private void disconnect() {
        
        if (this.connection == null) {
            // seems not to be opened at all
            return;
        }
        
        // check if bound
        if (this.connection.isBound()) {
            try {
                // unbind
                this.connection.unbind();
            } catch (NotBoundException ex) {
                // we were not bound
                LOG.error("Tried to unbind while not bound.", ex);
            } catch (IOException ex) {
                // error while unbinding
                LOG.error("Error while trying to unbind", ex);
            }
        }

        try {
            // remove me from observer list
            this.connection.removeObserver(this);
            // close the connection
            this.connection.closeLink();
        } catch (IOException ex) {
            LOG.error("Error disconnecting from host: " + this.host + ":" + this.port, ex);
        }
    }
    
    /**
     * does a reconnect first closing existing connection and then reestablishing
     * the connection again
     */
    private void reconnect() {
        LOG.warn("Reconnecting to host " + this.host + ":" + this.port + "...");
        
        // close connection
        disconnect();
        
        // and reconnect
        connect();
    }
    
    /*
     * (non-Javadoc)
     * @see ie.omk.smpp.event.ConnectionObserver#update(ie.omk.smpp.Connection,
     * ie.omk.smpp.event.SMPPEvent)
     */
    public void update(Connection conn, SMPPEvent ev) {
        if (ev.getType() == SMPPEvent.RECEIVER_EXIT && ((ReceiverExitEvent)ev).isException()) {
            logger.debug("SMPPEvent.RECEIVER_EXIT received.");
            // reconnect
            reconnect();
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * ie.omk.smpp.event.ConnectionObserver#packetReceived(ie.omk.smpp.Connection
     * , ie.omk.smpp.message.SMPPPacket)
     */
    public void packetReceived(Connection conn, SMPPPacket pack) {
        switch (pack.getCommandId()) {
        case SMPPPacket.DELIVER_SM:
            try {
                InOnly exchange = getExchangeFactory().createInOnlyExchange();
                NormalizedMessage in = exchange.createMessage();
                exchange.setInMessage(in);
                marshaler.toNMS(in, pack);
                getChannel().send(exchange);
            } catch (Exception e) {
                LOG.error("Unable to send the received packet to nmr.", e);
            }
            break;

        case SMPPPacket.BIND_TRANSCEIVER_RESP:
            if (pack.getCommandStatus() != 0) {
                LOG.error("Error binding: " + pack.getCommandStatus());
                // binding failed...try to reconnect
                reconnect();
            } else {
                LOG.debug("Bounded");
            }
            break;
        default:  // ignore
            LOG.debug("Received packet with unhandled command id: " + pack.getCommandId());
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.apache.servicemix.common.ExchangeProcessor#process(javax.jbi.messaging
     * .MessageExchange)
     */
    public void process(MessageExchange exchange) throws Exception {
        if (exchange.getStatus() == ExchangeStatus.DONE) {
            // received DONE for a sent message
            return;
        } else if (exchange.getStatus() == ExchangeStatus.ERROR) {
            // received ERROR state for a sent message
            return;
        } else {
            throw new MessagingException("Unsupported exchange received...");
        }
    }

    /**
     * @return Returns the connection.
     */
    public Connection getConnection() {
        return this.connection;
    }

    /**
     * @param connection The connection to set.
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * @return Returns the marshaler.
     */
    public SmppMarshalerSupport getMarshaler() {
        return this.marshaler;
    }

    /**
     * @param marshaler The marshaler to set.
     */
    public void setMarshaler(SmppMarshalerSupport marshaler) {
        this.marshaler = marshaler;
    }

    /**
     * @return Returns the port.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * @param port The port to set.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return Returns the bindRetryCycle.
     */
    public long getBindRetryCycle() {
        return this.bindRetryCycle;
    }

    /**
     * @param bindRetryCycle The bindRetryCycle to set.
     */
    public void setBindRetryCycle(long bindRetryCycle) {
        this.bindRetryCycle = bindRetryCycle;
    }

    /**
     * @return Returns the host.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * @param host The host to set.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return Returns the user.
     */
    public String getUser() {
        return this.user;
    }

    /**
     * @param user The user to set.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return Returns the resource.
     */
    public String getResource() {
        return this.resource;
    }

    /**
     * @param resource The resource to set.
     */
    public void setResource(String resource) {
        this.resource = resource;
    }
}
