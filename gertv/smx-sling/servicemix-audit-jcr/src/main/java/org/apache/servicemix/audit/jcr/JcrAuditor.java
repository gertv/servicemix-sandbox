package org.apache.servicemix.audit.jcr;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.jbi.JBIException;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.rmi.client.ClientRepositoryFactory;
import org.apache.servicemix.jbi.audit.AbstractAuditor;

public abstract class JcrAuditor extends AbstractAuditor {
	
	private Repository repository;
	private ThreadLocal<Session> session = new ThreadLocal<Session>();

	@Override
	protected void doStart() throws JBIException {
		super.doStart();
		ClientRepositoryFactory factory = new ClientRepositoryFactory();
		try {
			repository = factory.getRepository("rmi://localhost:1099/jackrabbit");
		} catch (MalformedURLException e) {
			throw new JBIException("Unable to connect to JCR repository", e);
		} catch (ClassCastException e) {
			throw new JBIException("Unable to connect to JCR repository", e);
		} catch (RemoteException e) {
			throw new JBIException("Unable to connect to JCR repository", e);
		} catch (NotBoundException e) {
			throw new JBIException("Unable to connect to JCR repository", e);
		} 
	}
	
	protected Session getSession() throws LoginException, RepositoryException {
		if (session .get() == null) {
			Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
			this.session.set(session);
		}
		return session.get();
	}
	
	public String getDescription() {
		return "ServiceMix JCR Auditor";
	}



}
