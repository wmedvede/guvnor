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

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class PipelineExecutionTrace
        extends AbstractHasKeyObject<PipelineExecutionTraceKey> {

    private Pipeline pipeline;

    private StageStatus pipelineStatus;

    private Map<String, StageStatus> stageStatusMap = new HashMap<>();

    private Map<String, String> stageErrorMap = new HashMap<>();

    public PipelineExecutionTrace(@MapsTo("key") final PipelineExecutionTraceKey key,
                                  @MapsTo("pipeline") final Pipeline pipeline,
                                  @MapsTo("pipelineStatus") final StageStatus pipelineStatus,
                                  @MapsTo("stageStatusMap") final Map<String, StageStatus> stageStatusMap,
                                  @MapsTo("stageErrorMap") final Map<String, String> stageErrorMap) {
        super(key);
        this.pipeline = pipeline;
        this.pipelineStatus = pipelineStatus;
        this.stageStatusMap = stageStatusMap;
        this.stageErrorMap = stageErrorMap;
    }

    public PipelineExecutionTrace(PipelineExecutionTraceKey key) {
        super(key);
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public StageStatus getPipelineStatus() {
        return pipelineStatus;
    }

    public void setPipelineStatus(StageStatus pipelineStatus) {
        this.pipelineStatus = pipelineStatus;
    }

    public StageStatus getStageStatus(String stage) {
        return stageStatusMap.get(stage);
    }

    public void setStageStatus(String stage,
                               StageStatus stageStatus) {
        stageStatusMap.put(stage,
                           stageStatus);
    }

    public String getStageError(String stage) {
        return stageErrorMap.get(stage);
    }

    public void setStageError(String stage, String error) {
        stageErrorMap.put(stage, error);
    }
}
