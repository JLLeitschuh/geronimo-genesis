/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.genesis.plugins.tools;

import java.util.Iterator;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;

import org.apache.geronimo.genesis.dependency.DependencyHelper;
import org.apache.geronimo.genesis.dependency.DependencyResolutionListener;
import org.apache.geronimo.genesis.MojoSupport;

/**
 * Helper to show a projects dependencies.
 *
 * @goal show-dependencies
 * 
 * @version $Rev$ $Date$
 */
public class ShowDependenciesMojo
    extends MojoSupport
{
    /**
     * Enable verbose details (version and scope).
     * 
     * @parameter expression="${verbose}"
     */
    private boolean verbose = false;

    /**
     * @component
     */
    private DependencyHelper helper = null;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    protected ArtifactRepository localRepository;

    protected void doExecute() throws Exception {
        DependencyResolutionListener listener = helper.resolveProject(project, localRepository);
        printDependencyListing(listener.getRootNode(), "");
    }

    private void printDependencyListing(DependencyResolutionListener.Node node, final String pad) {
        Artifact artifact = node.getArtifact();
        String id = artifact.getDependencyConflictId();

        StringBuffer buff = new StringBuffer(id);
        if (verbose) {
            buff.append(" ");
            buff.append("{ version=").append(artifact.getVersion());
            buff.append(", scope=").append(artifact.getScope());
            buff.append(" }");
        }
        log.info(pad + buff);

        if (!node.getChildren().isEmpty()) {
            for (Iterator deps = node.getChildren().iterator(); deps.hasNext();) {
                DependencyResolutionListener.Node dep = (DependencyResolutionListener.Node) deps.next();
                printDependencyListing(dep, pad + "    ");
            }
        }
    }
}
