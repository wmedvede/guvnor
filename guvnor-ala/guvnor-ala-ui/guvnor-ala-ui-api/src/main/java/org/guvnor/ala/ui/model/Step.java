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

@Portable
public class Step {

    private PipelineKey pipelineKey;
    private String message;
    private StageStatus status;

    public Step(@MapsTo("pipelineKey") final PipelineKey pipelineKey,
                @MapsTo("message") final String message,
                @MapsTo("status") final StageStatus status) {
        this.pipelineKey = pipelineKey;
        this.message = message;
        this.status = status;
    }

    public PipelineKey getPipelineKey() {
        return pipelineKey;
    }

    public String getMessage() {
        return message;
    }

    public StageStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Step step = (Step) o;

        if (pipelineKey != null ? !pipelineKey.equals(step.pipelineKey) : step.pipelineKey != null) {
            return false;
        }
        if (message != null ? !message.equals(step.message) : step.message != null) {
            return false;
        }
        return status == step.status;
    }

    @Override
    public int hashCode() {
        int result = pipelineKey != null ? pipelineKey.hashCode() : 0;
        result = ~~result;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = ~~result;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = ~~result;
        return result;
    }
}