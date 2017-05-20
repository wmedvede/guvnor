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

package org.guvnor.ala.ui.backend.service;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.guvnor.ala.pipeline.events.AfterPipelineExecutionEvent;
import org.guvnor.ala.pipeline.events.AfterStageExecutionEvent;
import org.guvnor.ala.pipeline.events.BeforePipelineExecutionEvent;
import org.guvnor.ala.pipeline.events.BeforeStageExecutionEvent;
import org.guvnor.ala.pipeline.events.OnErrorPipelineExecutionEvent;
import org.guvnor.ala.pipeline.events.OnErrorStageExecutionEvent;
import org.guvnor.ala.pipeline.events.PipelineEvent;
import org.guvnor.ala.pipeline.events.PipelineEventListener;
import org.guvnor.ala.pipeline.execution.PipelineExecutorTask;
import org.guvnor.ala.pipeline.execution.PipelineExecutorTrace;
import org.guvnor.ala.registry.PipelineExecutorRegistry;
import org.guvnor.ala.ui.events.PipelineStatusChange;
import org.guvnor.ala.ui.events.StageStatusChange;
import org.guvnor.ala.ui.model.PipelineExecutionTraceKey;
import org.guvnor.ala.ui.model.PipelineStatus;

/**
 * Monitors the events produced by pipelines launched by the PipelineExecutorTaskManager and raises the required
 * events to the UI or other interested parties.
 */
public class PipelineExecutionListener
        implements PipelineEventListener {

    private Event<PipelineStatusChange> pipelineStatusChangeEvent;

    private Event<StageStatusChange> stageStatusChangeEvent;

    private PipelineExecutorRegistry executorRegistry;

    @Inject
    public PipelineExecutionListener(final Event<PipelineStatusChange> pipelineStatusChangeEvent,
                                     final Event<StageStatusChange> stageStatusChangeEvent,
                                     final PipelineExecutorRegistry executorRegistry) {
        this.pipelineStatusChangeEvent = pipelineStatusChangeEvent;
        this.stageStatusChangeEvent = stageStatusChangeEvent;
        this.executorRegistry = executorRegistry;
    }

    @Override
    public void beforePipelineExecution(BeforePipelineExecutionEvent bpee) {
        PipelineExecutorTask task = getTask(bpee);
        if (task != null) {
            pipelineStatusChangeEvent.fire(new PipelineStatusChange(new PipelineExecutionTraceKey(bpee.getExecutionId()),
                                                                    PipelineStatus.RUNNING));
        }
    }

    @Override
    public void afterPipelineExecution(AfterPipelineExecutionEvent apee) {
        PipelineExecutorTask task = getTask(apee);
        if (task != null) {
            pipelineStatusChangeEvent.fire(new PipelineStatusChange(new PipelineExecutionTraceKey(apee.getExecutionId()),
                                                                    PipelineStatus.FINISHED));
        }
    }

    @Override
    public void beforeStageExecution(BeforeStageExecutionEvent bsee) {
        PipelineExecutorTask task = getTask(bsee);
        if (task != null) {
            stageStatusChangeEvent.fire(new StageStatusChange(new PipelineExecutionTraceKey(task.getId()),
                                                              bsee.getStage().getName(),
                                                              PipelineStatus.RUNNING));
        }
    }

    @Override
    public void onStageError(OnErrorStageExecutionEvent oesee) {
        PipelineExecutorTask task = getTask(oesee);
        if (task != null) {
            stageStatusChangeEvent.fire(new StageStatusChange(new PipelineExecutionTraceKey(task.getId()),
                                                              oesee.getStage().getName(),
                                                              PipelineStatus.ERROR));
        }
    }

    @Override
    public void afterStageExecution(AfterStageExecutionEvent asee) {
        PipelineExecutorTask task = getTask(asee);
        if (task != null) {
            stageStatusChangeEvent.fire(new StageStatusChange(new PipelineExecutionTraceKey(task.getId()),
                                                              asee.getStage().getName(),
                                                              PipelineStatus.FINISHED));
        }
    }

    @Override
    public void onPipelineError(OnErrorPipelineExecutionEvent oepee) {
        PipelineExecutorTask task = getTask(oepee);
        if (task != null) {
            pipelineStatusChangeEvent.fire(new PipelineStatusChange(new PipelineExecutionTraceKey(oepee.getExecutionId()),
                                                                    PipelineStatus.ERROR));
        }
    }

    private PipelineExecutorTask getTask(PipelineEvent event) {
        if (event.getExecutionId() != null) {
            PipelineExecutorTrace executionRecord = executorRegistry.getExecutorTrace(event.getExecutionId());
            return executionRecord.getTask();
        }
        return null;
    }
}
