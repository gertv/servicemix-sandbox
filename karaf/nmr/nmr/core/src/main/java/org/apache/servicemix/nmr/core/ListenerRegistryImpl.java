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
package org.apache.servicemix.nmr.core;

import java.util.Iterator;

import org.apache.servicemix.nmr.api.event.Listener;
import org.apache.servicemix.nmr.api.event.ListenerRegistry;
import org.apache.servicemix.nmr.core.util.Filter;
import org.apache.servicemix.nmr.core.util.FilterIterator;

/**
 *
 */
public class ListenerRegistryImpl extends ServiceRegistryImpl<Listener> implements ListenerRegistry {

    /**
     * Retrieve an iterator of listeners of a certain type
     *
     * @param type the type of listeners
     * @return an iterator over the registered listeners
     */
    public <T extends Listener> Iterable<T> getListeners(final Class<T> type) {
        return new Iterable<T>() {
            public Iterator<T> iterator() {
                return new FilterIterator(getServices().iterator(), new Filter() {
                    public boolean match(Object endpoint) {
                        return type.isInstance(endpoint);
                    }
                });
            }
        };
    }

}
