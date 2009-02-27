package org.apache.servicemix.audit.jcr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import javax.xml.transform.TransformerException;

import org.apache.jackrabbit.value.DateValue;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.util.MessageUtil;

/**
 * 
 * Class for processing message exchange based on correlation id
 * 
 * @author vkrejcirik
 * 
 */
public class SlingJcrAuditorStrategy implements JcrAuditorStrategy {

    public static final String RESOURCE_TYPE = "sling:resourceType";

    // esp files for rendering
    public static final String MESSAGE_FLOWS_RESOURCE_TYPE = "servicemix/message_flows";
    public static final String EXCHANGE_RESOURCE_TYPE = "servicemix/exchange";
    public static final String MESSAGE_FLOW_RESOURCE_TYPE = "servicemix/message_flow";
    public static final String NORMALIZED_MESSAGE_RESOURCE_TYPE = "servicemix/normalizedmessage";
    public static final String ENDPOINT_RESOURCE_TYPE = "servicemix/endpoint";
    // content
    public static final String CONTENT_MESSAGE_FLOWS_TYPE = "content/servicemix/message_flows";
    public static final String CONTENT_ENDPOINTS = "content/servicemix/endpoints";
    public static final String CONTENT_EXCHANGES_TYPE = "content/servicemix/exchanges";

    private static final SourceTransformer TRANSFORMER = new SourceTransformer();

    // let's time slice our message exchange archive on an hourly basis
    private static final DateFormat FORMAT = new SimpleDateFormat("yyyyMMddhha", Locale.ENGLISH);

    public void processExchange(MessageExchange messageExchange, Session session) throws ItemExistsException, PathNotFoundException,
            VersionException, ConstraintViolationException, LockException, RepositoryException, MessagingException, TransformerException {

        /*
         * (String) messageExchange .getProperty("org.apache.servicemix.senderEndpoint")
         */

        // get endpoint node for this exchange
        Node endpointNode = getEndpointNode(session, (String) messageExchange.getProperty("org.apache.servicemix.senderEndpoint"));

        String exId = (String) messageExchange.getExchangeId().replaceAll(":", "_");

        // add this exchange to the endpoint node, if not exists yet
        try {
            endpointNode.getNode(exId);
        } catch (PathNotFoundException e) {
            Node newNode = endpointNode.addNode(exId);
            newNode.setProperty("Created", new DateValue(new GregorianCalendar()));
            newNode.setProperty("exchangeId", messageExchange.getExchangeId());
        }

        // construct exchange node
        Node node = getNodeForExchange(messageExchange, session);

        node.setProperty("ExchangeStatus", messageExchange.getStatus().toString());
        node.setProperty("Pattern", messageExchange.getPattern().toString());

        node.setProperty("endpointId", ((String) messageExchange.getProperty("org.apache.servicemix.senderEndpoint")).replaceAll(":", "_")
                .replaceAll("/", "-"));

        if (messageExchange.getEndpoint() != null) {
            node.setProperty("Endpoint", messageExchange.getEndpoint().getEndpointName());
        }

        if (messageExchange.getService() != null) {
            node.setProperty("Service", messageExchange.getService().toString());
        }

        for (Object key : messageExchange.getPropertyNames()) {
            String name = (String) key;

            node.setProperty(name, messageExchange.getProperty(name).toString());
        }

        addNormalizedMessages(node, messageExchange);
        node.setProperty("Updated", new DateValue(new GregorianCalendar()));

    }

    /**
     * 
     * Get node for given Exchange
     * 
     * @param exchange
     * @param session
     * @return Node
     * @throws ItemExistsException
     * @throws PathNotFoundException
     * @throws VersionException
     * @throws ConstraintViolationException
     * @throws LockException
     * @throws RepositoryException
     */
    private Node getNodeForExchange(MessageExchange exchange, Session session) throws ItemExistsException, PathNotFoundException,
            VersionException, ConstraintViolationException, LockException, RepositoryException {

        String id = exchange.getExchangeId().replaceAll(":", "_");
        String corr_id = exchange.getProperty("org.apache.servicemix.correlationId").toString().replaceAll(":", "_");

        // node with date
        // Node parent = getExchangeBaseNode(session);
        Node parent = getMessageFlowBaseNode(session);

        // node with correlation id
        Node parent_corr = getCorrelationIdNode(parent, corr_id, id);

        try {
            return parent_corr.getNode(id);

        } catch (PathNotFoundException e) {
            Node node = parent_corr.addNode(id);
            node.setProperty(RESOURCE_TYPE, EXCHANGE_RESOURCE_TYPE);

            node.setProperty("Created", new DateValue(new GregorianCalendar()));

            node.addMixin("mix:versionable");
            return node;
        }
    }

    /**
     * 
     * Get node for correlation id
     * 
     * @param parent
     * @param corr_id
     * @param id
     * @return Node
     * @throws RepositoryException
     * @throws ValueFormatException
     * @throws VersionException
     * @throws LockException
     * @throws ConstraintViolationException
     */
    private synchronized Node getCorrelationIdNode(Node parent, String corr_id, String id) throws RepositoryException,
            ValueFormatException, VersionException, LockException, ConstraintViolationException {

        // first exchange of the flow
        if (corr_id == null) {
            Node node = parent.addNode(id);
            node.setProperty(RESOURCE_TYPE, MESSAGE_FLOW_RESOURCE_TYPE);

            node.setProperty("Created", new DateValue(new GregorianCalendar()));
            node.setProperty("CorrelationId", id);
            return node;
        }

        try {
            return parent.getNode(corr_id);

        } catch (PathNotFoundException e) {

            Node node = parent.addNode(corr_id);
            node.setProperty(RESOURCE_TYPE, MESSAGE_FLOW_RESOURCE_TYPE);

            node.setProperty("Created", new DateValue(new GregorianCalendar()));
            node.setProperty("CorrelationId", corr_id);

            return node;
        }
    }

    /**
     * 
     * Get base node for message flow
     * 
     * @param session
     * @return Node
     * @throws RepositoryException
     */
    private Node getMessageFlowBaseNode(Session session) throws RepositoryException {
        Node messageFlows = session.getRootNode().getNode(CONTENT_MESSAGE_FLOWS_TYPE);

        return createOrGet(messageFlows, FORMAT.format(new Date()), MESSAGE_FLOWS_RESOURCE_TYPE);
    }

    /**
     * 
     * Get base node for endpoint
     * 
     * @param session
     * @param endpointName
     * @return Node
     * @throws RepositoryException
     */
    private Node getEndpointNode(Session session, String endpointName) throws RepositoryException {
        Node endpoints = session.getRootNode().getNode(CONTENT_ENDPOINTS);

        String id = endpointName.replaceAll(":", "_").replaceAll("/", "-");

        return createOrGet(endpoints, id, ENDPOINT_RESOURCE_TYPE);
    }

    /**
     * 
     * Create or get child node with path and resource type
     * 
     * @param parent
     * @param path
     * @param resourceType
     * @return Node
     * @throws ValueFormatException
     * @throws VersionException
     * @throws LockException
     * @throws ConstraintViolationException
     * @throws RepositoryException
     */
    private synchronized Node createOrGet(Node parent, String path, String resourceType) throws ValueFormatException, VersionException,
            LockException, ConstraintViolationException, RepositoryException {
        try {
            return parent.getNode(path);

        } catch (PathNotFoundException e) {
            Node node = parent.addNode(path);
            node.setProperty(RESOURCE_TYPE, resourceType);
            node.setProperty("Created", new DateValue(new GregorianCalendar()));
            return node;
        }
    }

    /**
     * 
     * Add in, out and fault messages, if exist
     * 
     * @param node
     * @param exchange
     * @throws ItemExistsException
     * @throws PathNotFoundException
     * @throws VersionException
     * @throws ConstraintViolationException
     * @throws LockException
     * @throws MessagingException
     * @throws RepositoryException
     * @throws TransformerException
     */
    private void addNormalizedMessages(Node node, MessageExchange exchange) throws ItemExistsException, PathNotFoundException,
            VersionException, ConstraintViolationException, LockException, MessagingException, RepositoryException, TransformerException {

        if (exchange.getMessage("in") != null) {
            addNormalizedMessages(node, "In", exchange.getMessage("in"));
        }
        if (exchange.getMessage("out") != null) {
            addNormalizedMessages(node, "Out", exchange.getMessage("out"));
        }
        if (exchange.getMessage("fault") != null) {
            addNormalizedMessages(node, "Fault", exchange.getMessage("fault"));
        }
    }

    /**
     * 
     * Add normalized message to node as property
     * 
     * @param parent
     * @param type
     * @param message
     * @throws ItemExistsException
     * @throws PathNotFoundException
     * @throws VersionException
     * @throws ConstraintViolationException
     * @throws LockException
     * @throws RepositoryException
     * @throws MessagingException
     * @throws TransformerException
     */
    private void addNormalizedMessages(Node parent, String type, NormalizedMessage message) throws ItemExistsException,
            PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException, MessagingException,
            TransformerException {
        if (message != null) {
            Node node;
            try {
                node = parent.getNode(type);
            } catch (PathNotFoundException e) {
                node = parent.addNode(type);
            }
            node.setProperty("Content", getNormalizedMessageContent(message));
            for (Object key : message.getPropertyNames()) {
                String name = (String) key;
                node.setProperty(name, message.getProperty(name).toString());
            }
            node.setProperty(RESOURCE_TYPE, NORMALIZED_MESSAGE_RESOURCE_TYPE);
        }
    }

    /**
     * 
     * Get content from normalized message
     * 
     * @param message
     * @return String
     * @throws MessagingException
     * @throws TransformerException
     */
    private String getNormalizedMessageContent(NormalizedMessage message) throws MessagingException, TransformerException {
        MessageUtil.enableContentRereadability(message);
        return TRANSFORMER.toString(message.getContent());
    }
}
