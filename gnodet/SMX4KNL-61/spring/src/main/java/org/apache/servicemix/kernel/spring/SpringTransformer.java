/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.kernel.spring;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

public class SpringTransformer {

    static Transformer transformer;
    static DocumentBuilderFactory dbf;


    public static void transform(URL url, OutputStream os) throws Exception {
        Document doc = parse(url);
        String name = url.getPath();
        int idx = name.lastIndexOf('/');
        if (idx >= 0) {
            name = name.substring(idx + 1);
        }
        String[] str = extractNameVersionType(name);

        Manifest m = new Manifest();
        m.getMainAttributes().putValue("Manifest-Version", "2");
        m.getMainAttributes().putValue("Bundle-SymbolicName", str[0]);
        m.getMainAttributes().putValue("Bundle-Version", str[1]);
        m.getMainAttributes().putValue("Spring-Context", "*;publish-context:=false;create-asynchronously:=true");
        InputStream is = url.openStream();
        String importPkgs = getImportPackages(analyze(is));
        is.close();
        if (importPkgs != null && importPkgs.length() > 0) {
            m.getMainAttributes().putValue("Import-Package", importPkgs);
        }
        m.getMainAttributes().putValue("DynamicImport-Package", "*");

        JarOutputStream out = new JarOutputStream(os);
        ZipEntry e = new ZipEntry(JarFile.MANIFEST_NAME);
        out.putNextEntry(e);
        m.write(out);
        out.closeEntry();
        e = new ZipEntry("META-INF/");
        out.putNextEntry(e);
        e = new ZipEntry("META-INF/spring/");
        out.putNextEntry(e);
        out.closeEntry();
        e = new ZipEntry("META-INF/spring/" + name);
        out.putNextEntry(e);
        is = url.openStream();
        copyInputStream(is, out);
        is.close();
        out.closeEntry();
        out.close();
    }

    private static final String DEFAULT_VERSION = "0.0.0";

    private static final Pattern ARTIFACT_MATCHER = Pattern.compile("(.+)(?:-(\\d+)(?:\\.(\\d+)(?:\\.(\\d+))?)?(?:[^a-zA-Z0-9](.*))?)(?:\\.([^\\.]+))", Pattern.DOTALL);
    private static final Pattern FUZZY_MODIFIDER = Pattern.compile("(?:\\d+[.-])*(.*)", Pattern.DOTALL);

    public static String[] extractNameVersionType(String url) {
        Matcher m = ARTIFACT_MATCHER.matcher(url);
        if (!m.matches()) {
            return new String[] { url, DEFAULT_VERSION };
        }
        else {
            //System.err.println(m.groupCount());
            //for (int i = 1; i <= m.groupCount(); i++) {
            //    System.err.println("Group " + i + ": " + m.group(i));
            //}

            StringBuffer v = new StringBuffer();
            String d1 = m.group(1);
            String d2 = m.group(2);
            String d3 = m.group(3);
            String d4 = m.group(4);
            String d5 = m.group(5);
            String d6 = m.group(6);
            if (d2 != null) {
                v.append(d2);
                if (d3 != null) {
                    v.append('.');
                    v.append(d3);
                    if (d4 != null) {
                        v.append('.');
                        v.append(d4);
                        if (d5 != null) {
                            v.append(".");
                            cleanupModifier(v, d5);
                        }
                    } else if (d5 != null) {
                        v.append(".0.");
                        cleanupModifier(v, d5);
                    }
                } else if (d5 != null) {
                    v.append(".0.0.");
                    cleanupModifier(v, d5);
                }
            }
            return new String[] { d1, v.toString(), d6 };
        }
    }

    private static void cleanupModifier(StringBuffer result, String modifier) {
        Matcher m = FUZZY_MODIFIDER.matcher(modifier);
        if (m.matches()) {
            modifier = m.group(1);
        }
        for (int i = 0; i < modifier.length(); i++) {
            char c = modifier.charAt(i);
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '-') {
                result.append(c);
            }
        }
    }

    public static Set<String> analyze(InputStream in) throws Exception {
        if (transformer == null) {
            TransformerFactory tf = TransformerFactory.newInstance();
            Source source = new StreamSource(SpringTransformer.class.getResourceAsStream("extract.xsl"));
            transformer = tf.newTransformer(source);
        }

        Set<String> refers = new TreeSet<String>();

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Result r = new StreamResult(bout);
        Source s = new StreamSource(in);
        transformer.transform(s, r);

        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        bout.close();

        BufferedReader br = new BufferedReader(new InputStreamReader(bin));

        String line = br.readLine();
        while (line != null) {
            line = line.trim();
            if (line.length() > 0) {
                String parts[] = line.split("\\s*,\\s*");
                for (int i = 0; i < parts.length; i++) {
                    int n = parts[i].lastIndexOf('.');
                    if (n > 0) {
                        refers.add(parts[i].substring(0, n));
                    }
                }
            }
            line = br.readLine();
        }
        br.close();
        return refers;
    }

    protected static String getImportPackages(Set<String> packages) {
        StringBuilder sb = new StringBuilder();
        for (String pkg : packages) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(pkg);
        }
        return sb.toString();
    }

    protected static Document parse(URL url) throws Exception {
        if (dbf == null) {
            dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
        }
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(url.toString());
    }

    protected static void copyInputStream(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[4096];
        int len = in.read(buffer);
        while (len >= 0) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }
    }
}
