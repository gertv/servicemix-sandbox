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
package org.apache.servicemix.jbi.runtime.impl;

import org.apache.servicemix.nmr.api.Exchange;

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.Fault;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Oct 5, 2007
 * Time: 5:20:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class InOnlyImpl extends MessageExchangeImpl implements InOnly {

    public InOnlyImpl(Exchange exchange) {
        super(exchange);
    }

    public void setOutMessage(NormalizedMessage message) throws MessagingException {
        throw new MessagingException("Out message not supported");
    }

    public void setFault(Fault message) throws MessagingException {
        throw new MessagingException("Fault message not supported");
    }

}
