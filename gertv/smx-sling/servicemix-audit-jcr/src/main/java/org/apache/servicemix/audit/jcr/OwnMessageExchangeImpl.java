package org.apache.servicemix.audit.jcr;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.servicemix.jbi.messaging.FaultImpl;
import org.apache.servicemix.jbi.messaging.NormalizedMessageImpl;

/**
 * 
 * Simple MessageExchange implementation for holding values for audit
 * 
 * @author vkrejcirik
 * 
 */
public class OwnMessageExchangeImpl implements Serializable, MessageExchange {

    private static final long serialVersionUID = 1L;

    private NormalizedMessage inMessage;
    private NormalizedMessage outMessage;
    private NormalizedMessage faultMessage;

    private Role role;
    private ExchangeStatus status;
    private String exchangeId;
    private ServiceEndpoint endpoint;
    private QName service;
    private boolean isTransacted;
    private QName interfaceName;
    private QName operation;
    private URI pattern;

    private HashMap<String, Object> properties;

    private OwnMessageExchangeImpl() {
        // private constructor
        properties = new HashMap<String, Object>();
    }

    public static OwnMessageExchangeImpl create(MessageExchange exchange) {

        OwnMessageExchangeImpl result = new OwnMessageExchangeImpl();

        result.exchangeId = exchange.getExchangeId();
        result.endpoint = exchange.getEndpoint();
        result.role = exchange.getRole();
        result.status = exchange.getStatus();
        result.interfaceName = exchange.getInterfaceName();
        result.operation = exchange.getOperation();
        result.isTransacted = exchange.isTransacted();
        result.pattern = exchange.getPattern();

        if (exchange.getMessage("fault") != null)
            result.faultMessage = exchange.getMessage("fault");

        if (exchange.getMessage("in") != null)
            result.inMessage = exchange.getMessage("in");

        if (exchange.getMessage("out") != null)
            result.outMessage = exchange.getMessage("out");

        // properties
        Iterator it = exchange.getPropertyNames().iterator();

        while (it.hasNext()) {
            String key = (String) it.next();
            result.getProperties().put(key, exchange.getProperty(key));
        }

        return result;
    }

    public Role getRole() {
        return this.role;
    }

    public ExchangeStatus getStatus() {
        return this.status;
    }

    public String getExchangeId() {
        return this.exchangeId;
    }

    public ServiceEndpoint getEndpoint() {
        return this.endpoint;
    }

    public NormalizedMessage getInMessage() {
        return this.inMessage;
    }

    public HashMap<String, Object> getProperties() {
        return this.properties;
    }

    public NormalizedMessage getOutMessage() {
        return this.outMessage;
    }

    public NormalizedMessage getFaultMessage() {
        return this.faultMessage;
    }

    public Fault createFault() throws MessagingException {
        return new FaultImpl();
    }

    public NormalizedMessage createMessage() throws MessagingException {
        return new NormalizedMessageImpl();
    }

    public Exception getError() {
        // empty ??
        return null;
    }

    public Fault getFault() {
        // empty ??
        return null;
    }

    public void setError(Exception arg0) {
        // empty ??
    }

    public void setFault(Fault arg0) throws MessagingException {
        setMessage(arg0, "fault");
    }

    public QName getInterfaceName() {
        return this.interfaceName;
    }

    public NormalizedMessage getMessage(String arg0) {

        if ("fault".equals(arg0))
            return this.faultMessage;

        if ("in".equals(arg0))
            return this.inMessage;

        if ("out".equals(arg0))
            return this.outMessage;

        return null;
    }

    public QName getOperation() {
        return this.operation;
    }

    public URI getPattern() {
        return this.pattern;
    }

    public Object getProperty(String arg0) {
        return this.properties.get(arg0);
    }

    public Set getPropertyNames() {
        return this.properties.keySet();
    }

    public QName getService() {
        return this.service;
    }

    public boolean isTransacted() {
        return this.isTransacted;
    }

    public void setEndpoint(ServiceEndpoint arg0) {
        this.endpoint = arg0;
    }

    public void setInterfaceName(QName arg0) {
        this.interfaceName = arg0;
    }

    public void setMessage(NormalizedMessage arg0, String arg1) throws MessagingException {

        if (arg1.equals("fault"))
            this.faultMessage = arg0;
        else if (arg1.equals("in"))
            this.inMessage = arg0;
        else if (arg1.equals("out"))
            this.outMessage = arg0;
    }

    public void setOperation(QName arg0) {
        this.operation = arg0;
    }

    public void setProperty(String arg0, Object arg1) {
        this.properties.put(arg0, arg1);
    }

    public void setService(QName arg0) {
        this.service = arg0;
    }

    public void setStatus(ExchangeStatus arg0) throws MessagingException {
        this.status = arg0;
    }
}
