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
package org.apache.servicemix.gshellweb.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.InputStreamReader;
import java.io.PipedOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;

import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.apache.servicemix.gshellweb.client.ShellService;
import org.apache.geronimo.gshell.spring.ProxyIO;
import org.apache.geronimo.gshell.spring.EnvironmentTargetSource;
import org.apache.geronimo.gshell.command.IO;
import org.apache.geronimo.gshell.command.Variables;
import org.apache.geronimo.gshell.shell.Environment;
import org.apache.geronimo.gshell.DefaultEnvironment;
import org.apache.geronimo.gshell.DefaultVariables;

public class ShellServiceImpl extends RemoteServiceServlet implements ShellService {

    private Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();
    private Timer timer = new Timer(true);
    private long timeout = 5 * 60 * 1000;

    public void init() throws ServletException {
        super.init();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                processTimeouts();
            }
        }, 1000, 1000);

    }

    public String login(String username, String password) {
        try {
            Session session = new Session();
            String sessionId = session.toString();
            sessions.put(sessionId, session);
            return sessionId;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
	
	public void logout(String sessionId) {
        sessions.remove(sessionId);
	}
	
	public void setNextLineIn(String sessionId, String in) {
        System.out.println("setNextLineIn [sessionId=" + sessionId + ", in=" + in + "]");
        Session session = useSession(sessionId);
        try {
            EnvironmentTargetSource.setEnvironment(session.environment);
            ProxyIO.setIO(session.io);
            CommandExecutorHolder.getCommandExecutor().execute(in);
            session.outPipe.write(("Command: " + in + "\n").getBytes());
        } catch (Exception e) {
            handleException(sessionId, e);
        } finally {
            ProxyIO.setIO(null);
        }
    }

    public String getNextLineOut(String sessionId) {
        System.out.println("getNextLineOut [sessionId=" + sessionId + "]");
        Session session = useSession(sessionId);
        try {
            return session.outReader.readLine();
        } catch (Exception e) {
            handleException(sessionId, e);
            return null;
        }
    }
	
	public String getNextLineErr(String sessionId) {
        System.out.println("getNextLineErr [sessionId=" + sessionId + "]");
        Session session = useSession(sessionId);
        try {
            return session.errReader.readLine();
        } catch (Exception e) {
            handleException(sessionId, e);
            return null;
        }
	}

    private void handleException(String sessionId, Exception e) {
        System.err.println("Exception in session: " + sessionId + ": " + e);
        e.printStackTrace(System.err);
    }

    private Session useSession(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            throw new RuntimeException("Session does not exist or has been closed");
        }
        session.updateLastUsed();
        return session;
    }

    private void processTimeouts() {
        long time = System.currentTimeMillis();
        for (Map.Entry<String, Session> entry : sessions.entrySet()) {
            if (entry.getValue().getLastUsed() < time - timeout) {
                System.out.println("Session " + entry.getKey() + " has timed out");
                sessions.remove(entry.getKey());
            }
        }
    }

    private static class Session {
        private long lastUsed;
        private InputStream out;
        private InputStream err;
        private BufferedReader outReader;
        private BufferedReader errReader;
        private PipedOutputStream outPipe;
        private PipedOutputStream errPipe;
        private IO io;
        private Environment environment;
        private Variables variables;

        public Session() throws IOException {
            // TODO: bridge the out / err to gshell
            outPipe = new PipedOutputStream();
            errPipe = new PipedOutputStream();

            out = new PipedInputStream(outPipe);
            outReader = new BufferedReader(new InputStreamReader(out));
            err = new PipedInputStream(errPipe);
            errReader = new BufferedReader(new InputStreamReader(err));

            io = new IO(new ByteArrayInputStream(new byte[0]), outPipe, errPipe);
            variables = new DefaultVariables();
            environment = new DefaultEnvironment(io, variables);

            updateLastUsed();
        }
        public long getLastUsed() {
            return lastUsed;
        }
        public void updateLastUsed() {
            lastUsed = System.currentTimeMillis();
        }
    }

}

