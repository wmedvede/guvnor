/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.guvnor.ala.ui.service;

import java.util.Collection;

import org.guvnor.ala.ui.model.PipelineExecutionTraceKey;
import org.guvnor.ala.ui.model.ProviderKey;
import org.guvnor.ala.ui.model.ProviderTypeKey;
import org.guvnor.ala.ui.model.RuntimeKey;
import org.guvnor.ala.ui.model.RuntimeListItem;
import org.guvnor.ala.ui.model.Source;
import org.jboss.errai.bus.server.annotations.Remote;

@Remote
public interface RuntimeService {

    Collection<RuntimeListItem> getRuntimesInfo(final ProviderKey providerKey);

    RuntimeListItem getRuntimeInfo(final PipelineExecutionTraceKey pipelineExecutionTraceKey);

    Collection<String> getPipelines(final ProviderTypeKey providerTypeKey);

    String createRuntime(final ProviderKey providerKey,
                         final String runtimeName,
                         final Source source,
                         final String pipelineName);

    void start(final RuntimeKey runtimeKey);

    void stop(final RuntimeKey runtimeKey);

    void rebuild(final RuntimeKey runtimeKey);

    void delete(final RuntimeKey runtimeKey);
}