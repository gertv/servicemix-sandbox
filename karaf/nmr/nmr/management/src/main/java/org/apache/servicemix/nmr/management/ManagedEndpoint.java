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
package org.apache.servicemix.nmr.management;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicemix.nmr.api.Endpoint;
import org.apache.servicemix.nmr.api.internal.InternalEndpoint;
import org.fusesource.commons.management.ManagementStrategy;
import org.fusesource.commons.management.Statistic;
import org.fusesource.commons.management.Statistic.UpdateMode;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 */
@ManagedResource(description = "Managed Endpoint", currencyTimeLimit = 15)
public class ManagedEndpoint {

    protected final InternalEndpoint endpoint;
    protected final Map<String,?> properties;
    protected final Statistic inboundExchanges;
    protected final Statistic outboundExchanges;
    protected final Statistic inboundExchangeRate;
    protected final Statistic outboundExchangeRate;
    protected final ManagementStrategy managementStrategy;

    public ManagedEndpoint(InternalEndpoint endpoint, 
                           Map<String,?> properties, 
                           ManagementStrategy managementStrategy) {
        this.endpoint = endpoint;
        this.properties = new HashMap<String,Object>(properties);
        this.managementStrategy = managementStrategy;
        this.inboundExchanges = managementStrategy.createStatistic("inboundExchanges", 
                                                                   this, 
                                                                   UpdateMode.VALUE); 
        this.inboundExchangeRate = managementStrategy.createStatistic("inboundExchangeRate", 
                                                                      this, 
                                                                      UpdateMode.COUNTER);
        this.outboundExchanges = managementStrategy.createStatistic("outboundExchanges", 
                                                                    this, 
                                                                    UpdateMode.VALUE);        
        this.outboundExchangeRate = managementStrategy.createStatistic("outboundExchangeRate", 
                                                                       this, 
                                                                       UpdateMode.COUNTER);
    }

    public InternalEndpoint getEndpoint() {
        return endpoint;
    }

    void incrementInbound() {
        inboundExchanges.increment();
        inboundExchangeRate.increment();
    }

    void incrementOutbound() {
        outboundExchanges.increment();
        outboundExchangeRate.increment();
    }

    /**
     * Retrieve the name of the endpoint
     *
     * @return the name of the endpoint
     */
    @ManagedAttribute(description = "Name of the endpoint")
    public String getName() {
        return (String) properties.get(Endpoint.NAME);
    }

    /**
     * Retrieve the properties of the endpoint
     *
     * @return the properties of the endpoint
     */
    @ManagedAttribute(description = "Properties associated to this endpoint")
    public Map<String, ?> getProperties() {
        return properties;
    }

    /**
     * Get the Inbound MessageExchange count
     *
     * @return inbound count
     */
    @ManagedAttribute(description = "Number of exchanges received")
    public long getInboundExchangeCount() {
        return inboundExchanges.getUpdateCount();
    }

    /**
     * Get the Inbound MessageExchange rate (number/sec)
     *
     * @return the inbound exchange rate
     */
    @ManagedAttribute(description = "Exchanges received per second")
    public double getInboundExchangeRate() {
        return getRate(inboundExchangeRate);
    }

    /**
     * Get the Outbound MessageExchange count
     *
     * @return outbound count
     */
    @ManagedAttribute(description = "Number of exchanges sent")
    public long getOutboundExchangeCount() {
        return outboundExchanges.getUpdateCount();
    }

    /**
     * Get the Outbound MessageExchange rate (number/sec)
     *
     * @return the outbound exchange rate
     */
    @ManagedAttribute(description = "Exchanges sent per second")
    public double getOutboundExchangeRate() {
        return getRate(outboundExchangeRate);
    }

    /**
     * reset the Stats
     */
    @ManagedOperation
    public void reset() {
        inboundExchanges.reset();
        outboundExchanges.reset();
        inboundExchangeRate.reset();
        outboundExchangeRate.reset();
    }
    
    /**
     * Get the update rate for the given statistic
     * @param stat the statistic
     * @return
     */
    private synchronized double getRate(Statistic stat) {
        if (stat.getUpdateCount() == 0) {
            return 0;
        }
        double d = stat.getValue();
        return d / stat.getUpdateCount();
    }

}
