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

/**
 * This class models the query parameters for a query against the runtime service by using ui model parameters
 */
public class RuntimeServiceQueryBuilder {

    private ProviderKey providerKey;

    private RuntimeKey runtimeKey;

    private PipelineExecutionTraceKey pipelineExecutionTraceKey;

    private String runtimeName;

    private RuntimeServiceQueryBuilder() {
    }

    public static RuntimeServiceQueryBuilder newInstance() {
        return new RuntimeServiceQueryBuilder();
    }

    public RuntimeServiceQueryBuilder withProviderKey(final ProviderKey providerKey) {
        this.providerKey = providerKey;
        return this;
    }

    public RuntimeServiceQueryBuilder withRuntimeKey(final RuntimeKey runtimeKey) {
        this.runtimeKey = runtimeKey;
        return this;
    }

    public RuntimeServiceQueryBuilder withPipelineExecutionTraceKey(final PipelineExecutionTraceKey pipelineExecutionTraceKey) {
        this.pipelineExecutionTraceKey = pipelineExecutionTraceKey;
        return this;
    }

    public RuntimeServiceQueryBuilder withRuntimeName(final String runtimeName) {
        this.runtimeName = runtimeName;
        return this;
    }

    public RuntimeServiceQuery build() {
        return new RuntimeServiceQuery(providerKey,
                                       runtimeKey,
                                       pipelineExecutionTraceKey,
                                       runtimeName);
    }
}
