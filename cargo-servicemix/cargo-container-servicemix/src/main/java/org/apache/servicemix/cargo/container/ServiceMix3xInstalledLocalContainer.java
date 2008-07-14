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
package org.apache.servicemix.cargo.container;

import java.io.FileNotFoundException;

import org.apache.servicemix.cargo.container.internal.AbstractServiceMixInstalledLocalContainer;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * Special container support for the ServiceMix 3.x jbi container.
 * 
 * @version $Revision$
 */
public class ServiceMix3xInstalledLocalContainer extends AbstractServiceMixInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "servicemix3x";

    /**
     * {@inheritDoc}
     * @see AbstractServiceMixInstalledLocalContainer#AbstractInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public ServiceMix3xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see AbstractServiceMixInstalledLocalContainer#startUpAdditions(Java, Path)
     */
    protected void startUpAdditions(Java theJavaContainer, Path theClasspath)
        throws FileNotFoundException
    {
        // Nothing additional required
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public final String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public String getName()
    {
        return "ServiceMix " + getVersion("3.x");
    }    
}
