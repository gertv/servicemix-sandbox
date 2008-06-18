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
import org.apache.servicemix.jbi.audit.AuditorException;
import org.apache.servicemix.jbi.event.ExchangeEvent;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.util.MessageUtil;

public class SlingAuditor extends JcrAuditor {
	
	//let's time slice our message exchange archive on an hourly basis
	private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd hha");
	
	private static final String RESOURCE_TYPE = "sling:resourceType";
	private static final String EXCHANGES_RESOURCE_TYPE = "servicemix/exchanges";
	
	private static final SourceTransformer TRANSFORMER = new SourceTransformer();
	
	public void exchangeSent(ExchangeEvent event) {
		try {
			Node node = getNodeForExchange(event.getExchange());
			node.setProperty("ExchangeStatus", event.getExchange().getStatus().toString());
			node.setProperty("Pattern", event.getExchange().getPattern().toString());
			if (event.getExchange().getEndpoint() !=  null) {
				node.setProperty("Endpoint", event.getExchange().getEndpoint().getEndpointName());
			}
			if (event.getExchange().getService() != null) {
				node.setProperty("Service", event.getExchange().getService().toString());
			}
			for (Object key: event.getExchange().getPropertyNames()) {
				String name = (String) key;
				node.setProperty(name, event.getExchange().getProperty(name).toString());
			}
			addNormalizedMessages(node, event.getExchange());
			node.setProperty("Updated", new DateValue(new GregorianCalendar()));
			getSession().save();
			System.out.println("Event archived in JCR");
		} catch (ItemExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PathNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (VersionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConstraintViolationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LockException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block-
			e.printStackTrace();
		}
	}

	private void addNormalizedMessages(Node node, MessageExchange exchange) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, MessagingException, RepositoryException, TransformerException {
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
			NormalizedMessage message) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException, MessagingException, TransformerException {
		if (message != null) {
			Node node; 
			try {
				node = parent.getNode(type);
			} catch (PathNotFoundException e) {
				node = parent.addNode(type);
			}
			node.setProperty("Content", getNormalizedMessageContent(message));
			for (Object key: message.getPropertyNames()) {
				String name = (String) key;
				node.setProperty(name, message.getProperty(name).toString());
			}
			node.setProperty("sling:resourceType", "servicemix/normalizedmessage");
		}
	}

	private String getNormalizedMessageContent(NormalizedMessage message) throws MessagingException, TransformerException {
		MessageUtil.enableContentRereadability(message);
		return TRANSFORMER.toString(message.getContent());
	}

	private Node getNodeForExchange(MessageExchange exchange)
			throws ItemExistsException, PathNotFoundException,
			VersionException, ConstraintViolationException, LockException,
			RepositoryException {
		Session session = getSession();
		String id = exchange.getExchangeId().replaceAll(":", "_");
		Node parent = getBaseNode(session);
		try {
			return parent.getNode(id);
		} catch (PathNotFoundException e) {
			Node node = parent.addNode(id);
			node.setProperty("sling:resourceType", "servicemix/exchange");
			node.setProperty("Created", new DateValue(new GregorianCalendar()));
			node.addMixin("mix:versionable");
			return node;
		}
	}

	private Node getBaseNode(Session session) throws RepositoryException {
		Node exchanges = session.getRootNode().getNode("content/servicemix/exchanges");
		return createOrGet(exchanges, FORMAT.format(new Date()));
	}

	private synchronized Node createOrGet(Node exchanges, String path) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		try {
			return exchanges.getNode(path);
		} catch (PathNotFoundException e) {
			Node node = exchanges.addNode(path);
			node.setProperty(RESOURCE_TYPE, EXCHANGES_RESOURCE_TYPE);
			node.setProperty("Created", new DateValue(new GregorianCalendar()));
			return node;
		}
	}

	public String getDescription() {
		return "ServiceMix Sling Auditor";
	}

	@Override
	public int deleteExchangesByIds(String[] arg0) throws AuditorException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getExchangeCount() throws AuditorException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String[] getExchangeIdsByRange(int arg0, int arg1)
			throws AuditorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageExchange[] getExchangesByIds(String[] arg0)
			throws AuditorException {
		// TODO Auto-generated method stub
		return null;
	}

}
