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

package org.guvnor.ala.services.api;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This class models the query parameters for a query against the runtime system.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
public class RuntimeQuery {

    /**
     * Filter the results for a particular provider. If null, no provider filtering will be applied.
     */
    private String providerId;

    /**
     * Filter the results for a particular pipeline. If null, no pipeline filtering will be applied.
     */
    private String pipelineId;

    public RuntimeQuery(String providerId) {
        this.providerId = providerId;
    }

    public RuntimeQuery(String providerId,
                        String pipelineId) {
        this.providerId = providerId;
        this.pipelineId = pipelineId;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getPipelineId() {
        return pipelineId;
    }
}
