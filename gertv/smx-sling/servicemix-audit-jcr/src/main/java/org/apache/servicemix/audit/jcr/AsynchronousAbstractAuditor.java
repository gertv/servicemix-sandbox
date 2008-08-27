package org.apache.servicemix.audit.jcr;

import javax.jbi.JBIException;

import org.apache.servicemix.jbi.audit.AbstractAuditor;
import org.apache.servicemix.jbi.event.ExchangeEvent;

/**
 * 
 * Abstract class for take care of all the serialization and multi-threading stuff
 * 
 * @author vkrejcirik
 * 
 */
public abstract class AsynchronousAbstractAuditor extends AbstractAuditor {

    
    public void doStart() throws JBIException {
        
        
        
        super.doStart();
    }
    
    
    public void exchangeSent(ExchangeEvent event) {      
        onExchangeSent(event);
    }

    public void exchangeAccepted(ExchangeEvent event) {
        onExchangeAccepted(event);
        super.exchangeAccepted(event);
    }
    
    public abstract void onExchangeSent(ExchangeEvent event);

    public abstract void onExchangeAccepted(ExchangeEvent event);

}
