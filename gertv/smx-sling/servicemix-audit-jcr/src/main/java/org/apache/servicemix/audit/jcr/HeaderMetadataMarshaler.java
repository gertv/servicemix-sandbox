package org.apache.servicemix.audit.jcr;

import javax.jbi.messaging.MessageExchange;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * Marshaler for header and metadata from message exchange
 * 
 * @author vkrejcirik
 * 
 */
public class HeaderMetadataMarshaler implements AuditorMarshaler {

    private static final Log LOG = LogFactory.getLog(HeaderMetadataMarshaler.class);

    public ObjectMessage marschal(MessageExchange exchange, Session session) {

        ObjectMessage message = null;
        OwnMessageExchangeImpl exchangeImpl = OwnMessageExchangeImpl.create(exchange);

        try {
            message = session.createObjectMessage(exchangeImpl);

        } catch (JMSException e) {
            LOG.error("Error while serializing own message exchange implementation.");
        }

        return message;
    }

    public MessageExchange unmarshal(ObjectMessage message) {

        MessageExchange exchange = null;

        try {
            exchange = (MessageExchange) message.getObject();
        } catch (JMSException e) {
            LOG.error("Error while deserializing object message.");
        }

        return exchange;
    }

}
