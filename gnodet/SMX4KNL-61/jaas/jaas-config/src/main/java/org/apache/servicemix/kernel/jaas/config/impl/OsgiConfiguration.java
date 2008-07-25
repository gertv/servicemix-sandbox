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
package org.apache.servicemix.kernel.jaas.config.impl;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.security.auth.login.Configuration;
import javax.security.auth.login.AppConfigurationEntry;

import org.apache.servicemix.kernel.jaas.config.JaasRealm;

public class OsgiConfiguration extends Configuration {

    private List<JaasRealm> realms;

    public void init() {
        realms = new CopyOnWriteArrayList<JaasRealm>();
        Configuration.setConfiguration(this);
    }

    public void close() {
        realms.clear();
        realms = null;
        Configuration.setConfiguration(null);
    }

    public void register(JaasRealm realm, Map<String,?> properties) {
        realms.add(realm);
    }

    public void unregister(JaasRealm realm, Map<String,?> properties) {
        realms.remove(realm);
    }

    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        JaasRealm realm = null;
        for (JaasRealm r : realms) {
            if (r.getName().equals(name)) {
                if (realm == null || r.getRank() > realm.getRank()) {
                    realm = r;
                }
            }
        }
        if (realm != null) {
            return realm.getEntries();
        }
        return null;
    }

    public void refresh() {
        // Nothing to do, as we auto-update the configuration
    }
}
