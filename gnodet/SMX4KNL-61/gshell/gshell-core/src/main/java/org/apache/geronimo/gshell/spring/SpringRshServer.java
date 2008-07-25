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
package org.apache.geronimo.gshell.spring;

import java.net.URI;

import org.apache.geronimo.gshell.remote.server.RshServer;

public class SpringRshServer {

    private RshServer server;
    private String location;
    private boolean start;

    public RshServer getServer() {
        return server;
    }

    public void setServer(RshServer server) {
        this.server = server;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public void start() throws Exception {
        if (start) {
            try {
                server.bind(URI.create(location));
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    public void stop() throws Exception {
        if (start) {
            server.close();
        }
    }
}
