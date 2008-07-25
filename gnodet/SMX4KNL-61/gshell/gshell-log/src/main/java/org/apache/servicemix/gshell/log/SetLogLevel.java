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
package org.apache.servicemix.gshell.log;

import java.util.Dictionary;

import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.support.OsgiCommandSupport;
import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.clp.Option;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.Configuration;
import org.osgi.framework.ServiceReference;

/**
 * Set the log level for a given logger
 */
@CommandComponent(id = "log:set", description = "Change the log level")
public class SetLogLevel extends OsgiCommandSupport {

    @Argument(index = 0, required = true, description = "Level (TRACE, DEBUG, INFO, WARN, ERROR or - to unset")
    String level;

    @Argument(index = 1, required = false, description = "Logger name or ROOT (default)")
    String logger;

    static final String CONFIGURATION_PID  = "org.ops4j.pax.logging";
    static final String ROOT_LOGGER_PREFIX = "log4j.rootLogger";
    static final String LOGGER_PREFIX      = "log4j.logger.";
    static final String ROOT_LOGGER        = "ROOT";

    static final String TRACE = "TRACE";
    static final String DEBUG = "DEBUG";
    static final String INFO = "INFO";
    static final String WARN = "WARN";
    static final String ERROR = "ERROR";
    static final String INHERITED = "-";

    protected Object doExecute() throws Exception {
        if (ROOT_LOGGER.equalsIgnoreCase(this.logger)) {
            this.logger = null;
        }
        if (!TRACE.equals(level) &&
                !DEBUG.equals(level) &&
                !INFO.equals(level) &&
                !WARN.equals(level) &&
                !ERROR.equals(level) &&
                !INHERITED.equals(level)) {
            io.err.println("level must be set to TRACE, DEBUG, INFO, WARN or ERROR (or - to unset it)");
            return FAILURE;
        }
        if (INHERITED.equals(level) && logger == null) {
            io.err.println("Can not unset the ROOT logger");
            return FAILURE;
        }

        ConfigurationAdmin cfgAdmin = getConfigAdmin();
        Configuration cfg = cfgAdmin.getConfiguration(CONFIGURATION_PID, null);
        Dictionary props = cfg.getProperties();

        String logger = this.logger;
        String val;
        String prop;
        if (logger == null) {
            prop = ROOT_LOGGER_PREFIX;
        } else {
            prop = LOGGER_PREFIX + logger;
        }
        val = (String) props.get(prop);
        if (INHERITED.equals(level)) {
            if (val != null) {
                val = val.trim();
                int idx = val.indexOf(",");
                if (idx > 0) {
                    val = val.substring(idx);
                } else {
                    val = null;
                }
            }
        } else {
            if (val == null) {
                val = level;
            } else {
                val = val.trim();
                int idx = val.indexOf(",");
                if (idx == 0) {
                    val = level + val;
                } else if (idx > 0) {
                    val = level + val.substring(idx);
                }
            }
        }
        if (val == null) {
            props.remove(prop);
        } else {
            props.put(prop, val);
        }
        cfg.update(props);

        return SUCCESS;
    }

    protected ConfigurationAdmin getConfigAdmin() {
        ServiceReference ref = getBundleContext().getServiceReference(ConfigurationAdmin.class.getName());
        return getService(ConfigurationAdmin.class, ref);
    }

}