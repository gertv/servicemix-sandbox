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

Welcome to the ServiceMix cxf nmr example
==========================================

This example leverages CXF and Spring-DM to publish a web service to the nmr.

Quick steps to install the sample
---------------------------------

Launch the ServiceMix Kernel by running
  bin/servicemix
in the root dir of this distribution.

When inside the console, just run the following commands to install the
example:

  features/addUrl mvn:org.apache.servicemix.nmr/apache-servicemix-nmr/${servicemix.nmr.version}/xml/features
  features/addUrl mvn:org.apache.servicemix.features/apache-servicemix/${version}/xml/features
  features/install examples-cxf-nmr

If you have all the bundles available in your local repo, the installation
of the example will be very fast, otherwise it may take some time to
download everything needed.

Testing the example
-------------------

When the feature is installed, output for publishing the cxf endpoint
is recorded in data/log/servicemix.log:
10:20:56,169 | INFO  | xtenderThread-76 | ConfigurerImpl                   |
ransport.nmr.NMRTransportFactory  127 | Could not determine bean name for
instance of class org.apache.servicemix.cxf.transport.nmr.NMRDestination.
10:20:56,171 | INFO  | xtenderThread-76 | ServerImpl                       |
e.cxf.frontend.ServerFactoryBean  118 | Setting the server's publish address
to be nmr:HelloWorld


How does it work?
-----------------

The installation leverages ServiceMix Kernel by installing what's called
'features'. You can see the features definition file using the following
command inside ServiceMix console:

optional/cat mvn:org.apache.servicemix.features/apache-servicemix/${version}/xml/features

The list of available features can be obtained using:

features/list


