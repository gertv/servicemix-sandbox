package org.apache.servicemix.audit.jcr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

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
	public static final String EXCHANGES_RESOURCE_TYPE = "servicemix/exchanges";
	public static final String EXCHANGE_RESOURCE_TYPE = "servicemix/exchange";
	public static final String MESSAGE_FLOW_RESOURCE_TYPE = "servicemix/message_flow";
	public static final String NORMALIZED_MESSAGE_RESOURCE_TYPE = "servicemix/normalizedmessage";
	public static final String CONTENT_EXCHANGES_TYPE = "content/servicemix/exchanges";
	
	private static final SourceTransformer TRANSFORMER = new SourceTransformer();

	// let's time slice our message exchange archive on an hourly basis
	private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd hha");

	public void processExchange(MessageExchange messageExchange, Session session)
			throws ItemExistsException, PathNotFoundException,
			VersionException, ConstraintViolationException, LockException,
			RepositoryException, MessagingException, TransformerException {

		Node node = getNodeForExchange(messageExchange, session);

		node.setProperty("ExchangeStatus", messageExchange.getStatus()
				.toString());
		node.setProperty("Pattern", messageExchange.getPattern().toString());

		if (messageExchange.getEndpoint() != null) {
			node.setProperty("Endpoint", messageExchange.getEndpoint()
					.getEndpointName());
		}

		if (messageExchange.getService() != null) {
			node
					.setProperty("Service", messageExchange.getService()
							.toString());
		}

		for (Object key : messageExchange.getPropertyNames()) {
			String name = (String) key;

			node
					.setProperty(name, messageExchange.getProperty(name)
							.toString());
		}

		addNormalizedMessages(node, messageExchange);
		node.setProperty("Updated", new DateValue(new GregorianCalendar()));

	}

	private Node getNodeForExchange(MessageExchange exchange, Session session)
			throws ItemExistsException, PathNotFoundException,
			VersionException, ConstraintViolationException, LockException,
			RepositoryException {

		String id = exchange.getExchangeId().replaceAll(":", "_");
		String corr_id = exchange.getProperty(
				"org.apache.servicemix.correlationId").toString().replaceAll(":", "_");

		// node with date
		Node parent = getExchangeBaseNode(session);

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

	private synchronized Node getCorrelationIdNode(Node parent, String corr_id,
			String id) throws RepositoryException, ValueFormatException,
			VersionException, LockException, ConstraintViolationException {

		//first exchange of the flow
		if (corr_id == null) {
			Node node = parent.addNode(id);
			node.setProperty(RESOURCE_TYPE, EXCHANGES_RESOURCE_TYPE);
			
			node.setProperty("Created", new DateValue(new GregorianCalendar()));
			node.setProperty("CorrelationId", corr_id);
			return node;
		}

		try {
			return parent.getNode(corr_id);
		
		} catch (PathNotFoundException e) {
			
			Node node = parent.addNode(id);
			node.setProperty(RESOURCE_TYPE, EXCHANGES_RESOURCE_TYPE);
			
			node.setProperty("Created", new DateValue(new GregorianCalendar()));
			node.setProperty("CorrelationId", corr_id);
			
			return node;
		}
	}

	/**
	 * 
	 * Get base node for message exchange
	 * 
	 * @param session
	 * @return Node
	 * @throws RepositoryException
	 */
	private Node getExchangeBaseNode(Session session)
			throws RepositoryException {
		Node exchanges = session.getRootNode().getNode(CONTENT_EXCHANGES_TYPE);

		return createOrGet(exchanges, FORMAT.format(new Date()));
	}

	/**
	 * 
	 * Create or get node with the path
	 * 
	 * @param exchanges
	 * @param path
	 * @return Node
	 * @throws ValueFormatException
	 * @throws VersionException
	 * @throws LockException
	 * @throws ConstraintViolationException
	 * @throws RepositoryException
	 */
	private synchronized Node createOrGet(Node exchanges, String path)
			throws ValueFormatException, VersionException, LockException,
			ConstraintViolationException, RepositoryException {
		try {
			return exchanges.getNode(path);

		} catch (PathNotFoundException e) {
			Node node = exchanges.addNode(path);
			node.setProperty(RESOURCE_TYPE, MESSAGE_FLOW_RESOURCE_TYPE);

			node.setProperty("Created", new DateValue(new GregorianCalendar()));
			return node;
		}
	}

	private void addNormalizedMessages(Node node, MessageExchange exchange)
			throws ItemExistsException, PathNotFoundException,
			VersionException, ConstraintViolationException, LockException,
			MessagingException, RepositoryException, TransformerException {
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

	private void addNormalizedMessages(Node parent, String type,
			NormalizedMessage message) throws ItemExistsException,
			PathNotFoundException, VersionException,
			ConstraintViolationException, LockException, RepositoryException,
			MessagingException, TransformerException {
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

	private String getNormalizedMessageContent(NormalizedMessage message)
			throws MessagingException, TransformerException {
		MessageUtil.enableContentRereadability(message);
		return TRANSFORMER.toString(message.getContent());
	}
}
