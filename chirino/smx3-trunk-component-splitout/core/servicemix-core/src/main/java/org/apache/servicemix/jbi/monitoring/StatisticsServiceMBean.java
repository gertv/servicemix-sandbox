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
package org.apache.servicemix.jbi.monitoring;

public interface StatisticsServiceMBean {

    /**
     * @return the statsInterval
     */
    long getStatsInterval();

    /**
     * @param statsInterval the statsInterval to set
     */
    void setStatsInterval(long statsInterval);

    /**
     * @return the dumpStats
     */
    boolean isDumpStats();

    /**
     * @param dumpStats the dumpStats to set
     */
    void setDumpStats(boolean value);
    
    /**
     * Reset all statistics
     */
    void resetAllStats();
}
