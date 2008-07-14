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
package org.apache.servicemix.samples.bridge;

import java.io.FileInputStream;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import junit.framework.TestCase;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Simple Bridge Test.
 * Send a HTML post request to servicemix and waits 
 * for the "translated" jms message.
 */
public class BridgeTest extends TestCase {

	private static String url = "http://localhost:8192/bridge/";
	
	private static boolean recieved = false;
	
	public void testSimpleCall() throws Exception {

		ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Destination outQueue = new ActiveMQQueue("bridge.output");
        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer createConsumer = session.createConsumer(outQueue);
        createConsumer.setMessageListener(new MessageListener() {
		
			public void onMessage(Message arg0) {
				BridgeTest.recieved = true;
				ActiveMQTextMessage msg = (ActiveMQTextMessage) arg0;
				assertNotNull(msg);
			}
		
		});
                        
		HttpClient client = new HttpClient();
		
		PostMethod post = new PostMethod(url);
		post.setRequestBody(new FileInputStream("src/test/resources/request.xml"));
		
		int executeMethod = client.executeMethod(post);
		
		assertEquals(202, executeMethod);

		int maxTry = 100;
		while(!recieved || maxTry == 0) {
			Thread.sleep(200);
			maxTry--;
		}
		
		session.close();
		connection.close();
				
	}
	
}
