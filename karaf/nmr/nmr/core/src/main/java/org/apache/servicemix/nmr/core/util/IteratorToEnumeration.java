/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/**
 * Copied from org.apache.felix.framework.util package
 */
package org.apache.servicemix.nmr.core.util;

import java.util.Enumeration;
import java.util.Iterator;

public class IteratorToEnumeration implements Enumeration {

    private final Iterator m_iter;

    public IteratorToEnumeration(Iterator iter) {
        if (iter == null) {
            throw new IllegalArgumentException("Source iterator must not be null");
        }
        m_iter = iter;
    }

    public boolean hasMoreElements() {
        return m_iter.hasNext();
    }

    public Object nextElement() {
        return m_iter.next();
    }
}
