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

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.List;

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.osgi.framework.Version;
import org.osgi.service.obr.RepositoryAdmin;
import org.osgi.service.obr.Resource;

@CommandComponent(id="obr:list", description="List")
public class ListCommand extends ObrCommandSupport {

    @Argument(required = false, multiValued = true)
    List<String> args;

    protected void doExecute(RepositoryAdmin admin) throws Exception {
        String substr = null;

        if (args != null) {
            for (String arg : args)
            {
                // Add a space in between tokens.
                if (substr == null)
                {
                    substr = "";
                }
                else
                {
                    substr += " ";
                }

                substr += arg;
            }
        }
        
        StringBuffer sb = new StringBuffer();
        if ((substr == null) || (substr.length() == 0))
        {
            sb.append("(|(presentationname=*)(symbolicname=*))");
        }
        else
        {
            sb.append("(|(presentationname=*");
            sb.append(substr);
            sb.append("*)(symbolicname=*");
            sb.append(substr);
            sb.append("*))");
        }
        Resource[] resources = admin.discoverResources(sb.toString());
        for (int resIdx = 0; (resources != null) && (resIdx < resources.length); resIdx++)
        {
            String name = resources[resIdx].getPresentationName();
            Version version = resources[resIdx].getVersion();
            if (version != null)
            {
                io.out.println(name + " (" + version + ")");
            }
            else
            {
                io.out.println(name);
            }
        }

        if (resources == null)
        {
            io.out.println("No matching bundles.");
        }
    }

}