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
package org.apache.geronimo.gshell.obr;

import java.net.URL;
import java.util.List;

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.osgi.service.obr.RepositoryAdmin;

@CommandComponent(id="obr:addUrl", description="Add a list of repository URLs to the repository service")
public class AddUrlCommand extends ObrCommandSupport {

    @Argument(required = true, multiValued = true, description = "Repository URLs")
    List<String> urls;

    protected void doExecute(RepositoryAdmin admin) throws Exception {
        for (String url : urls) {
            admin.addRepository(new URL(url));
        }
    }
}
