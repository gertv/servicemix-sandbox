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
package org.apache.servicemix.jbi.runtime.impl;

import java.net.URI;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.nmr.api.Status;
import org.apache.servicemix.nmr.api.Pattern;
import org.apache.servicemix.nmr.core.MessageImpl;

/**
 * MessageExchange wrapper on top of an Exchange.
 *
 * @see Exchange
 */
public class MessageExchangeImpl implements MessageExchange  {

    public static final String INTERFACE_NAME_PROP = "javax.jbi.InterfaceName";
    public static final String SERVICE_NAME_PROP = "javax.jbi.ServiceName";
    public static final String SERVICE_ENDPOINT_PROP = "javax.jbi.ServiceEndpoint";

    public static final String IN = "in";
    public static final String OUT = "out";
    public static final String FAULT = "fault";

    private static final Map<Pattern, URI> MEP_URIS;

    static {
        MEP_URIS = new HashMap<Pattern, URI>();
        for (Pattern mep : Pattern.values()) {
            String uri = mep.getWsdlUri();
            String name = uri.substring(uri.lastIndexOf('/') + 1);
            MEP_URIS.put(mep, URI.create("http://www.w3.org/2004/08/wsdl/" + name));
        }
    }

    private final Exchange exchange;
    private ExchangeStatus previousStatus;

    public MessageExchangeImpl(Exchange exchange) {
        this.exchange = exchange;
    }

    public Exchange getInternalExchange() {
            return exchange;
    }

    public String getExchangeId() {
        return exchange.getId();
    }

    public URI getPattern() {
        return MEP_URIS.get(exchange.getPattern());
    }

    public MessageExchange.Role getRole() {
        return exchange.getRole() == org.apache.servicemix.nmr.api.Role.Consumer ? MessageExchange.Role.CONSUMER : MessageExchange.Role.PROVIDER;
    }

    public ExchangeStatus getStatus() {
        if (exchange.getStatus() == Status.Active) {
            return ExchangeStatus.ACTIVE;
        } else if (exchange.getStatus() == Status.Done) {
            return ExchangeStatus.DONE;
        } else if (exchange.getStatus() == Status.Error) {
            return ExchangeStatus.ERROR;
        } else {
            throw new IllegalStateException();
        }
    }

    public void setStatus(ExchangeStatus status) {
        if (status == ExchangeStatus.ACTIVE) {
            exchange.setStatus(Status.Active);
        } else if (status == ExchangeStatus.DONE) {
            exchange.setStatus(Status.Done);
        } else if (status == ExchangeStatus.ERROR) {
            exchange.setStatus(Status.Error);
        } else {
            throw new IllegalStateException();
        }

    }

    public QName getOperation() {
        return exchange.getOperation();
    }

    public void setOperation(QName qName) {
        exchange.setOperation(qName);
    }

    public Object getProperty(String s) {
        return exchange.getProperty(s);
    }

    public void setProperty(String s, Object o) {
        exchange.setProperty(s, o);
    }

    public Set getPropertyNames() {
        return exchange.getProperties().keySet();
    }

    public NormalizedMessage createMessage() throws MessagingException {
        return new NormalizedMessageImpl(new MessageImpl());
    }

    public NormalizedMessage getMessage(String name) {
        if (IN.equalsIgnoreCase(name)) {
            return getInMessage();
        } else if (OUT.equalsIgnoreCase(name)) {
            return getOutMessage();
        } else if (FAULT.equalsIgnoreCase(name)) {
            return getFault();
        } else {
            throw new IllegalStateException();
        }
    }

    public void setMessage(NormalizedMessage msg, String name) throws MessagingException {
        if (IN.equalsIgnoreCase(name)) {
            setInMessage(msg);
        } else if (OUT.equalsIgnoreCase(name)) {
            setOutMessage(msg);
        } else if (FAULT.equalsIgnoreCase(name)) {
            setFault((Fault) msg);
        } else {
            throw new IllegalStateException();
        }
    }

    public NormalizedMessage getInMessage() {
        Message msg = exchange.getIn(false);
        if (msg == null) {
            return null;
        } else {
            return new NormalizedMessageImpl(msg);
        }
    }

    public void setInMessage(NormalizedMessage message) throws MessagingException {
        if (exchange.getIn(false) != null) {
            throw new MessagingException("In message already set");
        }
        NormalizedMessageImpl msg = (NormalizedMessageImpl) message;
        exchange.setIn(msg.getInternalMessage());
    }

    public NormalizedMessage getOutMessage() {
        Message msg = exchange.getOut(false);
        if (msg == null) {
            return null;
        } else {
            return new NormalizedMessageImpl(msg);
        }
    }

    public void setOutMessage(NormalizedMessage message) throws MessagingException {
        if (exchange.getOut(false) != null) {
            throw new MessagingException("Out message already set");
        }
        NormalizedMessageImpl msg = (NormalizedMessageImpl) message;
        exchange.setOut(msg.getInternalMessage());
    }

    public Fault createFault() throws MessagingException {
        return new FaultImpl(new MessageImpl());
    }

    public Fault getFault() {
        Message msg = exchange.getFault(false);
        if (msg == null) {
            return null;
        } else {
            return new FaultImpl(msg);
        }
    }

    public void setFault(Fault message) throws MessagingException {
        if (exchange.getFault(false) != null) {
            throw new MessagingException("Fault message already set");
        }
        FaultImpl msg = (FaultImpl) message;
        exchange.setFault(msg.getInternalMessage());
    }

    public Exception getError() {
        return exchange.getError();
    }

    public void setError(Exception e) {
        exchange.setError(e);
    }

    public void setEndpoint(ServiceEndpoint endpoint) {
        exchange.setProperty(SERVICE_ENDPOINT_PROP, endpoint);
    }

    public void setService(QName service) {
        exchange.setProperty(SERVICE_NAME_PROP, service);
    }

    public void setInterfaceName(QName interfaceName) {
        exchange.setProperty(INTERFACE_NAME_PROP, interfaceName);
    }

    public ServiceEndpoint getEndpoint() {
        return getEndpoint(exchange);
    }

    public QName getInterfaceName() {
        return getInterfaceName(exchange);
    }

    public QName getService() {
        return getService(exchange);
    }

    public boolean isTransacted() {
        return exchange.getProperty(JTA_TRANSACTION_PROPERTY_NAME) != null;
    }

    void beforeReceived() throws MessagingException {
        previousStatus = getStatus();
    }

    void afterSend() throws MessagingException {
        if (previousStatus == ExchangeStatus.DONE || previousStatus == ExchangeStatus.ERROR) {
            throw new MessagingException("Can not send a terminated exchange");
        }
    }

    public static ServiceEndpoint getEndpoint(Exchange exchange) {
        return exchange.getProperty(SERVICE_ENDPOINT_PROP, ServiceEndpoint.class);
    }

    public static QName getInterfaceName(Exchange exchange) {
        return exchange.getProperty(INTERFACE_NAME_PROP, QName.class);
    }

    public static QName getService(Exchange exchange) {
        return exchange.getProperty(SERVICE_NAME_PROP, QName.class);
    }
}
