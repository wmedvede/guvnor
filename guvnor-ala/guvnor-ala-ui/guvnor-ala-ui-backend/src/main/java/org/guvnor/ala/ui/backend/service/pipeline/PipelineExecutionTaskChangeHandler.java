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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.guvnor.ala.pipeline.Stage;
import org.guvnor.ala.ui.events.StageStatusChange;
import org.guvnor.ala.ui.model.ProviderKey;
import org.guvnor.ala.ui.model.ProviderTypeKey;
import org.guvnor.ala.ui.model.Runtime;
import org.guvnor.ala.ui.events.NewPipelineStep;
import org.guvnor.ala.ui.events.RuntimeStatusChange;
import org.guvnor.ala.ui.model.RuntimeStatus;
import org.guvnor.ala.ui.model.StageStatus;

@ApplicationScoped
public class PipelineExecutionTaskChangeHandler {

    private Event<RuntimeStatusChange> runtimeStatusChangeEvent;

    private Event<StageStatusChange> stageStatusChangeEvent;

    public PipelineExecutionTaskChangeHandler() {
        //Empty constructor for Weld proxying
    }

    @Inject
    public PipelineExecutionTaskChangeHandler(Event< RuntimeStatusChange > runtimeStatusChangeEvent,
                                              Event<StageStatusChange> stageStatusChangeEvent) {
        this.runtimeStatusChangeEvent = runtimeStatusChangeEvent;
        this.stageStatusChangeEvent = stageStatusChangeEvent;
    }

    public void onTaskStarted(PipelineExecutionTask task) {
        task.setPipelineStatus(PipelineExecutionTask.Status.RUNNING);

        //TODO review this even rising
        Runtime runtime = buildRuntime(task);
        runtime.setStatus(RuntimeStatus.LOADING);
        runtimeStatusChangeEvent.fire(new RuntimeStatusChange(runtime));
    }

    public void onTaskFinished(PipelineExecutionTask task) {
        task.setPipelineStatus(PipelineExecutionTask.Status.FINISHED);

        //TODO review this even rising
        Runtime runtime = buildRuntime(task);
        runtime.setStatus(RuntimeStatus.STARTED);
        runtimeStatusChangeEvent.fire(new RuntimeStatusChange(runtime));
    }

    public void onStageStarted(PipelineExecutionTask task,
                               Stage stage) {
        task.setStageStatus(stage,
                            PipelineExecutionTask.Status.RUNNING);

        //TODO review this even rising
        stageStatusChangeEvent.fire(new StageStatusChange(buildRuntime(task), stage.getName(), StageStatus.RUNNING));
    }

    public void onStageError(PipelineExecutionTask task,
                             Stage stage,
                             Throwable error) {
        task.setStageStatus(stage,
                            PipelineExecutionTask.Status.ERROR);
        task.setStageError(stage, error);

        //TODO review this even rising
        stageStatusChangeEvent.fire(new StageStatusChange(buildRuntime(task), stage.getName(), StageStatus.ERROR));
    }

    public void onStageFinished(PipelineExecutionTask task,
                                Stage stage) {
        task.setStageStatus(stage,
                            PipelineExecutionTask.Status.FINISHED);

        //TODO review this even rising
        stageStatusChangeEvent.fire(new StageStatusChange(buildRuntime(task), stage.getName(), StageStatus.FINISHED));

    }

    public void onTaskError(PipelineExecutionTask task, Throwable error) {
        task.setPipelineStatus(PipelineExecutionTask.Status.ERROR);
        task.setPipelineError(error);

        //TODO review this even rising
        Runtime runtime = buildRuntime(task);
        runtime.setStatus(RuntimeStatus.ERROR);
        runtimeStatusChangeEvent.fire(new RuntimeStatusChange(runtime));
    }

    private Runtime buildRuntime(PipelineExecutionTask task) {
        return new Runtime(new ProviderKey(new ProviderTypeKey(task.getProviderId().getProviderType().getProviderTypeName()),
                                           task.getProviderId().getId(),
                                           task.getProviderId().getId()),
                           task.getRuntimeId().getId());
    }
}