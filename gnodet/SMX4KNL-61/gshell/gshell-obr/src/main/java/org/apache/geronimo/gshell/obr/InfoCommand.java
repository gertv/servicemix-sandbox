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

import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.geronimo.gshell.clp.Argument;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.osgi.service.obr.Capability;
import org.osgi.service.obr.RepositoryAdmin;
import org.osgi.service.obr.Requirement;
import org.osgi.service.obr.Resource;

@CommandComponent(id="obr:info", description="Display the meta-data for the specified bundles.")
public class InfoCommand extends ObrCommandSupport {

    @Argument(required = true, multiValued = true)
    List<String> bundles;

    protected void doExecute(RepositoryAdmin admin) throws Exception {
        for (String bundle : bundles) {
            String[] target = getTarget(bundle);
            Resource[] resources = searchRepository(admin, target[0], target[1]);
            if (resources == null)
            {
                io.err.println("Unknown bundle and/or version: "
                    + target[0]);
            }
            else
            {
                for (int resIdx = 0; resIdx < resources.length; resIdx++)
                {
                    if (resIdx > 0)
                    {
                        io.out.println("");
                    }
                    printResource(io.out, resources[resIdx]);
                }
            }
        }
    }

    private void printResource(PrintWriter out, Resource resource)
    {
        printUnderline(out, resource.getPresentationName().length());
        out.println(resource.getPresentationName());
        printUnderline(out, resource.getPresentationName().length());

        Map map = resource.getProperties();
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) iter.next();
            if (entry.getValue().getClass().isArray())
            {
                out.println(entry.getKey() + ":");
                for (int j = 0; j < Array.getLength(entry.getValue()); j++)
                {
                    out.println("   " + Array.get(entry.getValue(), j));
                }
            }
            else
            {
                out.println(entry.getKey() + ": " + entry.getValue());
            }
        }

        Requirement[] reqs = resource.getRequirements();
        if ((reqs != null) && (reqs.length > 0))
        {
            out.println("Requires:");
            for (int i = 0; i < reqs.length; i++)
            {
                out.println("   " + reqs[i].getFilter());
            }
        }

        Capability[] caps = resource.getCapabilities();
        if ((caps != null) && (caps.length > 0))
        {
            out.println("Capabilities:");
            for (int i = 0; i < caps.length; i++)
            {
                out.println("   " + caps[i].getProperties());
            }
        }
    }

}