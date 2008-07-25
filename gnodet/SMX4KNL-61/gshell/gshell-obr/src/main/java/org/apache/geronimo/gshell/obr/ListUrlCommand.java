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

import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.osgi.service.obr.Repository;
import org.osgi.service.obr.RepositoryAdmin;

@CommandComponent(id="obr:listUrl", description="Display the repository URLs currently associated with the repository service.")
public class ListUrlCommand extends ObrCommandSupport {

    protected void doExecute(RepositoryAdmin admin) {
        Repository[] repos = admin.listRepositories();
        if ((repos != null) && (repos.length > 0)) {
            for (int i = 0; i < repos.length; i++) {
                io.out.println(repos[i].getURL());
            }
        } else {
            io.out.println("No repository URLs are set.");
        }
    }

}