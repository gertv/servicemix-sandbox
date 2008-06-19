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

/**
 * @version $Revision$
 */
public class SharedLibraryList {
    private String version;
    private String name;

    public SharedLibraryList() {
    }

    public SharedLibraryList(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SharedLibraryList)) {
            return false;
        }

        final SharedLibraryList sharedLibraryList = (SharedLibraryList) o;

        if (name != null ? !name.equals(sharedLibraryList.name) : sharedLibraryList.name != null) {
            return false;
        }
        if (version != null ? !version.equals(sharedLibraryList.version) : sharedLibraryList.version != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = version != null ? version.hashCode() : 0;
        result = 29 * result + name != null ? name.hashCode() : 0;
        return result;
    }

    public String toString() {
        return "SharedLibraryList[version=" + version + "; name=" + name + "]";
    }

}
