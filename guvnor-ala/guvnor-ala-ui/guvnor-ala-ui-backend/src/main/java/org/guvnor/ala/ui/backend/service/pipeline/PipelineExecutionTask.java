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

package org.guvnor.ala.ui.backend.service.pipeline;

import java.util.HashMap;
import java.util.Map;

import org.guvnor.ala.pipeline.Input;
import org.guvnor.ala.pipeline.Pipeline;
import org.guvnor.ala.pipeline.Stage;
import org.guvnor.ala.runtime.RuntimeId;
import org.guvnor.ala.runtime.providers.ProviderId;

/**
 * Class for launching and monitoring pipelines.
 */
public class PipelineExecutionTask {

    /**
     * Indicates the pipeline execution status.
     */
    public enum Status { SCHEDULED, RUNNING, FINISHED, ERROR }

    /**
     * The UUID for the pipeline execution.
     */
    String executionId;

    /**
     * Indicates the current pipeline execution status.
     */
    Status pipelineStatus = Status.SCHEDULED;

    /**
     * The pipeline that is being executed or was executed by this executor.
     */
    Pipeline pipeline;

    /**
     * The pipeline input that was used for the pipeline execution.
     */
    Input input;

    /**
     * The provider for which the pipeline is being executed or was executed.
     */
    ProviderId providerId;

    /**
     * The runtime id for which the pipeline was executed.
     */
    RuntimeId runtimeId;

    /**
     * Holds the execution status for the pipeline stages.
     */
    Map<Stage, Status> stageStatus = new HashMap<>();

    /**
     * Holds the execution error for the stages in case there were errors.
     */
    Map<Stage, Throwable> stageError = new HashMap<>();

    /**
     * Holds the pipeline error in case the pipeline failed.
     */
    private Throwable pipelineError;

    public PipelineExecutionTask(Pipeline pipeline,
                                 Input input,
                                 ProviderId providerId,
                                 RuntimeId runtimeId) {
        this.pipeline = pipeline;
        this.input = input;
        this.providerId = providerId;
        this.runtimeId = runtimeId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
        getInput().put("executionId", executionId);
    }

    public Status getPipelineStatus() {
        return pipelineStatus;
    }

    public void setPipelineStatus(Status pipelineStatus) {
        this.pipelineStatus = pipelineStatus;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public ProviderId getProviderId() {
        return providerId;
    }

    public void setProviderId(ProviderId providerId) {
        this.providerId = providerId;
    }

    public RuntimeId getRuntimeId() {
        return runtimeId;
    }

    public void setRuntimeId(RuntimeId runtimeId) {
        this.runtimeId = runtimeId;
    }

    public void setStageStatus(Stage stage, Status status) {
        stageStatus.put(stage, status);
    }

    public Status getStageStatus(Stage stage) {
        return stageStatus.get(stage);
    }

    public Status getStageStatus(String name) {
        return stageStatus.entrySet()
                .stream()
                .filter(entry -> entry.getKey().getName().equals(name))
                .map(entry -> entry.getValue())
                .findFirst()
                .orElse(null);
    }

    public void setStageError(Stage stage, Throwable error) {
        stageError.put(stage, error);
    }

    public Throwable getStageError(Stage stage) {
        return stageError.get(stage);
    }

    public void setPipelineError(Throwable error) {
        this.pipelineError = error;
    }

    public Throwable getPipelineError() {
        return pipelineError;
    }
}