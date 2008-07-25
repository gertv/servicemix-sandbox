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
package org.apache.servicemix.gshell.features.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import org.apache.servicemix.gshell.features.Feature;
import org.apache.servicemix.gshell.features.Repository;
import org.xml.sax.SAXException;

/**
 * The repository implementation.
 */
public class RepositoryImpl implements Repository {

    private URI uri;
    private List<Feature> features;

    public RepositoryImpl(URI uri) {
        this.uri = uri;
    }

    public URI getURI() {
        return uri;
    }

    public Feature[] getFeatures() throws Exception {
        if (features == null) {
            load();
        }
        return features.toArray(new Feature[features.size()]);
    }

    public void load() throws IOException {
        try {
            features = new ArrayList<Feature>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc = factory.newDocumentBuilder().parse(uri.toURL().openStream());
            NodeList nodes = doc.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (!(node instanceof Element) || !"feature".equals(node.getNodeName())) {
                    continue;
                }
                Element e = (Element) nodes.item(i);
                String name = e.getAttribute("name");
                FeatureImpl f = new FeatureImpl(name);
                NodeList featureNodes = e.getElementsByTagName("feature");
                for (int j = 0; j < featureNodes.getLength(); j++) {
                    Element b = (Element) featureNodes.item(j);
                    f.addDependency(b.getTextContent());
                }
                NodeList configNodes = e.getElementsByTagName("config");
                for (int j = 0; j < configNodes.getLength(); j++) {
                    Element c = (Element) configNodes.item(j);
                    String cfgName = c.getAttribute("name");
                    String data = c.getTextContent();
                    Properties properties = new Properties();
                    properties.load(new ByteArrayInputStream(data.getBytes()));
                    Map<String, String> hashtable = new Hashtable<String, String>();
                    for (Object key : properties.keySet()) {
                        String n = key.toString();
                        hashtable.put(n, properties.getProperty(n));
                    }
                    f.addConfig(cfgName, hashtable);
                }
                NodeList bundleNodes = e.getElementsByTagName("bundle");
                for (int j = 0; j < bundleNodes.getLength(); j++) {
                    Element b = (Element) bundleNodes.item(j);
                    f.addBundle(b.getTextContent());
                }
                features.add(f);
            }
        } catch (SAXException e) {
            throw (IOException) new IOException().initCause(e);
        } catch (ParserConfigurationException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }

}
