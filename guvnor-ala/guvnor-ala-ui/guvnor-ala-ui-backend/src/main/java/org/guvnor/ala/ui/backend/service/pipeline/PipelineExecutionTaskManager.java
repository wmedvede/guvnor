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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.ala.pipeline.events.AfterPipelineExecutionEvent;
import org.guvnor.ala.pipeline.events.AfterStageExecutionEvent;
import org.guvnor.ala.pipeline.events.BeforePipelineExecutionEvent;
import org.guvnor.ala.pipeline.events.BeforeStageExecutionEvent;
import org.guvnor.ala.pipeline.events.OnErrorPipelineExecutionEvent;
import org.guvnor.ala.pipeline.events.OnErrorStageExecutionEvent;
import org.guvnor.ala.pipeline.events.PipelineEvent;
import org.guvnor.ala.pipeline.events.PipelineEventListener;
import org.guvnor.ala.services.api.backend.PipelineServiceBackend;

@ApplicationScoped
public class PipelineExecutionTaskManager
        implements PipelineEventListener {

    private PipelineServiceBackend pipelineService;

    //TODO set the proper executor configuration.
    private ExecutorService executor = Executors.newFixedThreadPool( 5 );

    private Map<String, PipelineExecutionTask > pipelineExecutionTaskMap = new HashMap<>();

    private PipelineExecutionRegistry pipelineExecutionRegistry;

    private PipelineExecutionTaskChangeHandler taskChangeHandler;

    public PipelineExecutionTaskManager() {
        //Empty constructor for Weld proxying
    }

    @Inject
    public PipelineExecutionTaskManager(PipelineServiceBackend pipelineService,
                                        PipelineExecutionRegistry pipelineExecutionRegistry,
                                        PipelineExecutionTaskChangeHandler taskChangeHandler) {
        this.pipelineService = pipelineService;
        this.pipelineExecutionRegistry = pipelineExecutionRegistry;
        this.taskChangeHandler = taskChangeHandler;
    }

    public void execute( PipelineExecutionTask task ) {
        String executionId = pipelineService.generateExecutionId();
        task.setExecutionId(executionId);
        pipelineExecutionTaskMap.put(executionId, task);
        pipelineExecutionRegistry.register(new PipelineExecutionRecord(executionId, task));
        executor.execute(() -> pipelineService.runPipeline(task.getPipeline().getName(), task.getInput()));
    }

    @Override
    public void beforePipelineExecution(BeforePipelineExecutionEvent bpee) {
        PipelineExecutionTask task = getTask(bpee);
        if (task != null) {
            task.setPipelineStatus(PipelineExecutionTask.Status.RUNNING);
            taskChangeHandler.onTaskStarted(task);
        }
    }

    @Override
    public void afterPipelineExecution(AfterPipelineExecutionEvent apee) {
        PipelineExecutionTask task = getTask(apee);
        if (task != null) {
            task.setPipelineStatus(PipelineExecutionTask.Status.FINISHED);
            taskChangeHandler.onTaskFinished(task);
        }
    }

    @Override
    public void beforeStageExecution(BeforeStageExecutionEvent bsee) {
        PipelineExecutionTask task = getTask(bsee);
        if (task != null) {
            task.setStageStatus(bsee.getStage(),
                                PipelineExecutionTask.Status.RUNNING);
            taskChangeHandler.onStageStarted(task, bsee.getStage());
        }
    }

    @Override
    public void onStageError(OnErrorStageExecutionEvent oesee) {
        PipelineExecutionTask task = getTask(oesee);
        if (task != null) {
            task.setStageStatus(oesee.getStage(),
                                PipelineExecutionTask.Status.ERROR);
            task.setStageError(oesee.getStage(), oesee.getError());
            taskChangeHandler.onStageError(task, oesee.getStage(), oesee.getError());
        }
    }

    @Override
    public void afterStageExecution(AfterStageExecutionEvent asee) {
        PipelineExecutionTask task = getTask(asee);
        if (task != null) {
            task.setStageStatus(asee.getStage(),
                                PipelineExecutionTask.Status.FINISHED);
            taskChangeHandler.onStageFinished(task, asee.getStage());
        }
    }

    @Override
    public void onPipelineError(OnErrorPipelineExecutionEvent oepee) {
        PipelineExecutionTask task = getTask(oepee);
        if (task != null) {
            task.setPipelineStatus(PipelineExecutionTask.Status.ERROR);
            taskChangeHandler.onTaskError(task, oepee.getError());
        }
    }

    private PipelineExecutionTask getTask(PipelineEvent event) {
        return pipelineExecutionTaskMap.get(event.getExecutionId());
    }
}