/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.servicemix.cxf.transport.nmr;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl.EndpointReferenceUtils;
import org.apache.servicemix.nmr.api.Channel;
import org.apache.servicemix.nmr.api.Endpoint;
import org.apache.servicemix.nmr.api.NMR;
import org.apache.servicemix.nmr.api.Pattern;
import org.apache.servicemix.nmr.api.Reference;
import org.apache.servicemix.nmr.api.Status;

public class NMRConduitOutputStream extends CachedOutputStream {

    private static final Logger LOG = LogUtils.getL7dLogger(NMRConduitOutputStream.class);

    private Message message;
    private boolean isOneWay;
    private Channel channel;
    private NMRConduit conduit;
    private EndpointReferenceType target;

    public NMRConduitOutputStream(Message m, NMR nmr, EndpointReferenceType target,
                                  NMRConduit conduit) {
        message = m;
        this.channel = nmr.createChannel();
        this.conduit = conduit;
        this.target = target;
        
    }

    @Override
    protected void doFlush() throws IOException {

    }

    @Override
    protected void doClose() throws IOException {
        isOneWay = message.getExchange().isOneWay();
        commitOutputMessage();
        if (target != null) {
            target.getClass();
        }
        channel.close();
    }

    private void commitOutputMessage() throws IOException {
        try {
            Member member = (Member) message.get(Method.class.getName());
            Class<?> clz = member.getDeclaringClass();
            Exchange exchange = message.getExchange();
            BindingOperationInfo bop = exchange.get(BindingOperationInfo.class);

            LOG.info(new org.apache.cxf.common.i18n.Message("INVOKE.SERVICE", LOG).toString() + clz);

            WebService ws = clz.getAnnotation(WebService.class);
            assert ws != null;
            QName interfaceName = new QName(ws.targetNamespace(), ws.name());
            QName serviceName;
            if (target != null) {
                serviceName = EndpointReferenceUtils.getServiceName(target, conduit.getBus());
            } else {
                serviceName = message.getExchange().get(org.apache.cxf.service.Service.class).getName();
            }
          
            LOG.info(new org.apache.cxf.common.i18n.Message("CREATE.MESSAGE.EXCHANGE", LOG).toString() + serviceName);
            org.apache.servicemix.nmr.api.Exchange xchng;
            if (isOneWay) {
                xchng = channel.createExchange(Pattern.InOnly);
            } else if (bop.getOutput() == null) {
                xchng = channel.createExchange(Pattern.RobustInOnly);
            } else {
                xchng = channel.createExchange(Pattern.InOut);
            }

            org.apache.servicemix.nmr.api.Message inMsg = xchng.getIn();
            LOG.info(new org.apache.cxf.common.i18n.Message("EXCHANGE.ENDPOINT", LOG).toString() + serviceName);
            LOG.info("setup message contents on " + inMsg);
            inMsg.setBody(getMessageContent(message));
            LOG.info("service for exchange " + serviceName);

            Map<String,Object> refProps = new HashMap<String,Object>();
            refProps.put(Endpoint.INTERFACE_NAME, interfaceName);
            refProps.put(Endpoint.SERVICE_NAME, serviceName);
            Reference ref = channel.getNMR().getEndpointRegistry().lookup(refProps);
            xchng.setTarget(ref);
            xchng.setOperation(bop.getName());
            LOG.info("sending message");
            if (!isOneWay) {
                channel.sendSync(xchng);
                Source content = null;
                if (xchng.getFault(false) != null) {
                    content = xchng.getFault().getBody(Source.class);
                } else {
                    content = xchng.getOut().getBody(Source.class);
                }
                Message inMessage = new MessageImpl();
                message.getExchange().setInMessage(inMessage);
                InputStream ins = NMRMessageHelper.convertMessageToInputStream(content);
                if (ins == null) {
                    throw new IOException(new org.apache.cxf.common.i18n.Message("UNABLE.RETRIEVE.MESSAGE", LOG).toString());
                }
                inMessage.setContent(InputStream.class, ins);
                conduit.getMessageObserver().onMessage(inMessage);

                xchng.setStatus(Status.Done);
                channel.send(xchng);
            } else {
                channel.sendSync(xchng);
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            new IOException(e.toString());
        }
    }

    private Source getMessageContent(Message message2) throws IOException {
        return new StreamSource(this.getInputStream());
        
    }

    @Override
    protected void onWrite() throws IOException {

    }

}
