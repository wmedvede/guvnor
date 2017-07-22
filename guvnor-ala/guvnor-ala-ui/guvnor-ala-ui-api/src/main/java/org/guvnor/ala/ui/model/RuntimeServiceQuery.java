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

package org.guvnor.ala.ui.model;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * This class models the query parameters for a query against the runtime by using ui model parameters
 */
@Portable
public class RuntimeServiceQuery {

    private ProviderKey providerKey;

    private RuntimeKey runtimeKey;

    private PipelineExecutionTraceKey pipelineExecutionTraceKey;

    private String runtimeName;

    public RuntimeServiceQuery(@MapsTo("providerKey") final ProviderKey providerKey,
                               @MapsTo("runtimeKey") final RuntimeKey runtimeKey,
                               @MapsTo("pipelineExecutionTraceKey") final PipelineExecutionTraceKey pipelineExecutionTraceKey,
                               @MapsTo("runtimeName") final String runtimeName) {
        this.providerKey = providerKey;
        this.runtimeKey = runtimeKey;
        this.pipelineExecutionTraceKey = pipelineExecutionTraceKey;
        this.runtimeName = runtimeName;
    }

    public ProviderKey getProviderKey() {
        return providerKey;
    }

    public RuntimeKey getRuntimeKey() {
        return runtimeKey;
    }

    public PipelineExecutionTraceKey getPipelineExecutionTraceKey() {
        return pipelineExecutionTraceKey;
    }

    public String getRuntimeName() {
        return runtimeName;
    }
}
