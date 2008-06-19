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
package org.apache.servicemix.jbi.deployment;

import org.w3c.dom.DocumentFragment;

/**
 * A POJO used to hold the descriptor extension.
 *
 * @version $Revision$
 */
public class InstallationDescriptorExtension {
    DocumentFragment descriptorExtension;

    public DocumentFragment getDescriptorExtension() {
        return descriptorExtension;
    }

    public void setDescriptorExtension(DocumentFragment descriptorExtension) {
        this.descriptorExtension = descriptorExtension;
    }
}
