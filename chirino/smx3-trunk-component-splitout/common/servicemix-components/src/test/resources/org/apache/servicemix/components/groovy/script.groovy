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

println "Called with inbound message $inMessage"

// lets output some message properties
def me1 = deliveryChannel.createExchangeFactoryForService(new javax.xml.namespace.QName("http://servicemix.org/cheese/", "service1")).createInOutExchange()
def in1 = me1.createMessage()
in1.bodyText = inMessage.bodyText
me1.setMessage(in1, "in")
deliveryChannel.sendSync(me1)
println "Received: " + me1.getMessage("out").bodyText

// lets output some message properties
def me2 = deliveryChannel.createExchangeFactoryForService(new javax.xml.namespace.QName("http://servicemix.org/cheese/", "service2")).createInOutExchange()
def in2 = me2.createMessage()
in2.bodyText = me1.getMessage("out").bodyText
me2.setMessage(in2, "in")
deliveryChannel.sendSync(me2)
println "Received: " + me2.getMessage("out").bodyText

// lets output some non-xml body
outMessage.bodyText = me2.getMessage("out").bodyText

