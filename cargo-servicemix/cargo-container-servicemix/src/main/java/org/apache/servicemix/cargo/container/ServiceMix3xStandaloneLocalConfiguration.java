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
package org.apache.servicemix.cargo.container;

import java.util.Collections;
import java.util.Map;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;

/**
 * ServiceMix {@link org.codehaus.cargo.container.configuration.Configuration}
 * implementation.
 * 
 * @version $Revision$
 */
public class ServiceMix3xStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration {
	
	/**
	 * Time in ms between deployment finished checks.
	 */
	private int timeInMsBetweenDeploymentChecks = 7500;
	
	/**
	 * Time in ms between server startup finished checks.
	 */
	private int timeInMsBetweenStartupChecks = 1000;

	private int serverStartupMaxRetryCount = 60;
	
	/**
	 * constructor.
	 * 
	 * @param dir
	 *            directory
	 */
	public ServiceMix3xStandaloneLocalConfiguration(String dir) {
		super(dir);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doConfigure(LocalContainer arg0) throws Exception {

	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigurationCapability getCapability() {
		return new ConfigurationCapability() {

			public boolean supportsProperty(String arg0) {
				return false;
			}

			public Map getProperties() {
				return Collections.EMPTY_MAP;
			}

		};
	}

	public int getTimeInMsBetweenDeploymentChecks() {
		return timeInMsBetweenDeploymentChecks;
	}

	public void setTimeInMsBetweenDeploymentChecks(int timeBetweenDeploymentChecks) {
		this.timeInMsBetweenDeploymentChecks = timeBetweenDeploymentChecks;
	}

	
	public int getTimeInMsBetweenStartupChecks() {
		return timeInMsBetweenStartupChecks;
	}

	public void setTimeInMsBetweenStartupChecks(int timeInMsBetweenStartupChecks) {
		this.timeInMsBetweenStartupChecks = timeInMsBetweenStartupChecks;
	}

	public int getServerStartupMaxRetryCount() {
		return serverStartupMaxRetryCount;
	}

	public void setServerStartupMaxRetryCount(int serverStartupMaxRetryCount) {
		this.serverStartupMaxRetryCount = serverStartupMaxRetryCount;
	}
	
}
