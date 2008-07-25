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
package org.apache.servicemix.kernel.filemonitor;

import junit.framework.TestCase;

/**
 * FileMonitor Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>07/02/2008</pre>
 */
public class FileMonitorTest extends TestCase {

    public FileMonitorTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testFilePathsMatch() throws Exception {
        assertTrue(FileMonitor.filePathsMatch(
                "C:/Apps/apache-servicemix-kernel-1.0.0-rc1/src/README.txt",
                "C:/Apps/apache-servicemix-kernel-1.0.0-rc1/src/README.txt"));

        assertTrue(FileMonitor.filePathsMatch(
                "C:\\Apps\\apache-servicemix-kernel-1.0.0-rc1\\src\\README.txt",
                "C:\\Apps\\apache-servicemix-kernel-1.0.0-rc1\\src\\README.txt"));

        assertTrue(FileMonitor.filePathsMatch(
                "C:/Apps/apache-servicemix-kernel-1.0.0-rc1/src/README.txt",
                "C:\\Apps\\apache-servicemix-kernel-1.0.0-rc1\\src\\README.txt"));

        assertTrue(FileMonitor.filePathsMatch(
                "file:/C:/Apps/apache-servicemix-kernel-1.0.0-rc1/src/README.txt",
                "C:\\Apps\\apache-servicemix-kernel-1.0.0-rc1\\src\\README.txt"));

        assertTrue(FileMonitor.filePathsMatch(
                "file://C:/Apps/apache-servicemix-kernel-1.0.0-rc1/src/README.txt",
                "C:\\Apps\\apache-servicemix-kernel-1.0.0-rc1\\src\\README.txt"));
    }

    public void testNormalizeFilePath() throws Exception {
        assertEquals(
                "C:/Apps/apache-servicemix-kernel-1.0.0-rc1/src/README.txt",
                FileMonitor.normalizeFilePath("C:/Apps/apache-servicemix-kernel-1.0.0-rc1/src/README.txt"));

        assertEquals(
                "C:/Apps/apache-servicemix-kernel-1.0.0-rc1/src/README.txt",
                FileMonitor.normalizeFilePath("C:/Apps//apache-servicemix-kernel-1.0.0-rc1////src/////README.txt"));

        assertEquals(
                "C:/Apps/apache-servicemix-kernel-1.0.0-rc1/src/README.txt",
                FileMonitor.normalizeFilePath("C:\\Apps\\apache-servicemix-kernel-1.0.0-rc1\\src\\README.txt"));

        assertEquals(
                "C:/Apps/apache-servicemix-kernel-1.0.0-rc1/src/README.txt",
                FileMonitor.normalizeFilePath("C:\\\\Apps\\\\apache-servicemix-kernel-1.0.0-rc1\\\\src\\\\README.txt"));

        assertEquals(
                "C:/Apps/apache-servicemix-kernel-1.0.0-rc1/src/README.txt",
                FileMonitor.normalizeFilePath("C:\\Apps\\//apache-servicemix-kernel-1.0.0-rc1\\/\\src///\\\\README.txt"));

        assertEquals(
                "C:/Apps/apache-servicemix-kernel-1.0.0-rc1/src/README.txt",
                FileMonitor.normalizeFilePath("file:C:/Apps/apache-servicemix-kernel-1.0.0-rc1/src/README.txt"));

        assertEquals(
                "C:/Apps/apache-servicemix-kernel-1.0.0-rc1/src/README.txt",
                FileMonitor.normalizeFilePath("file:/C:/Apps/apache-servicemix-kernel-1.0.0-rc1/src/README.txt"));

        assertEquals(
                "C:/Apps/apache-servicemix-kernel-1.0.0-rc1/src/README.txt",
                FileMonitor.normalizeFilePath("file://C:/Apps/apache-servicemix-kernel-1.0.0-rc1/src/README.txt"));
    }

}
