package org.apache.servicemix.audit.jcr;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.audit.AbstractAuditor;
import org.apache.servicemix.jbi.audit.AuditorException;
import org.apache.servicemix.jbi.event.ExchangeEvent;

/**
 * 
 * JCR auditor
 * 
 * @author vkrejcirik
 *
 */
public class JcrAuditor extends AbstractAuditor {
	
	private static final Log LOG = LogFactory.getLog(JcrAuditor.class);
	
	private Repository repository;
	private ThreadLocal<Session> session = new ThreadLocal<Session>();
	private JcrAuditorStrategy strategy;
	
	@Override
	protected void doStart() throws JBIException {
		if (repository == null) {
			throw new JBIException("No repository configured, unable to start JCR auditor");
		}
		if (strategy == null) {
			throw new JBIException("No JcrAuditorStrategy configure, unable to start JCR auditor");
		}
		super.doStart();
	}
	
	/**
	 * Open a session with the JCR Repository 
	 */
	protected Session getSession() throws LoginException, RepositoryException {
		if (session .get() == null) {
			Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
			this.session.set(session);
		}
		return session.get();
	}
	
	public void exchangeSent(ExchangeEvent event) {
		try {
			strategy.processExchange(event.getExchange(), getSession());
			getSession().save();
			LOG.info("Successfully stored information about message exchange " + event.getExchange().getExchangeId() + " in the JCR repository");
		} catch (Exception e) {
			LOG.error("Unable to store information about message exchange " + event.getExchange().getExchangeId(), e);
		}
	}
	
	public String getDescription() {
		return "ServiceMix JCR Auditor";
	}
	
	// just some setters and getters
	/**
	 * Configure the JCR Repository to connect to
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}
	
	/**
	 * Configure the {@link JcrAuditorStrategy} to use
	 * @param strategy
	 */
	public void setStrategy(JcrAuditorStrategy strategy) {
		this.strategy = strategy;
	}

	//to be implemented
	@Override
	public int deleteExchangesByIds(String[] arg0) throws AuditorException {
		return 0;
	}

	@Override
	public int getExchangeCount() throws AuditorException {
		return 0;
	}

	@Override
	public String[] getExchangeIdsByRange(int arg0, int arg1)
			throws AuditorException {
		return null;
	}

	@Override
	public MessageExchange[] getExchangesByIds(String[] arg0)
			throws AuditorException {
		return null;
	}
}
