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

package org.guvnor.ala.ui.events;

import org.guvnor.ala.ui.model.PipelineExecutionTraceKey;
import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Event for notifying the deletion of a PipelineExecutionTrace.
 */
@Portable
public class PipelineExecutionTraceDeletedEvent {

    private PipelineExecutionTraceKey pipelineExecutionTraceKey;

    public PipelineExecutionTraceDeletedEvent(@MapsTo("pipelineExecutionTraceKey") final PipelineExecutionTraceKey pipelineExecutionTraceKey) {
        this.pipelineExecutionTraceKey = pipelineExecutionTraceKey;
    }

    public PipelineExecutionTraceKey getPipelineExecutionTraceKey() {
        return pipelineExecutionTraceKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PipelineExecutionTraceDeletedEvent that = (PipelineExecutionTraceDeletedEvent) o;

        return pipelineExecutionTraceKey != null ? pipelineExecutionTraceKey.equals(that.pipelineExecutionTraceKey) : that.pipelineExecutionTraceKey == null;
    }

    @Override
    public int hashCode() {
        return pipelineExecutionTraceKey != null ? pipelineExecutionTraceKey.hashCode() : 0;
    }
}
