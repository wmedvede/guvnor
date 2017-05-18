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

package org.guvnor.ala.pipeline.execution.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.guvnor.ala.pipeline.ConfigExecutor;
import org.guvnor.ala.pipeline.events.AfterPipelineExecutionEvent;
import org.guvnor.ala.pipeline.events.AfterStageExecutionEvent;
import org.guvnor.ala.pipeline.events.BeforePipelineExecutionEvent;
import org.guvnor.ala.pipeline.events.BeforeStageExecutionEvent;
import org.guvnor.ala.pipeline.events.OnErrorPipelineExecutionEvent;
import org.guvnor.ala.pipeline.events.OnErrorStageExecutionEvent;
import org.guvnor.ala.pipeline.events.PipelineEvent;
import org.guvnor.ala.pipeline.events.PipelineEventListener;
import org.guvnor.ala.pipeline.execution.ExecutionIdGenerator;
import org.guvnor.ala.pipeline.execution.PipelineExecutor;
import org.guvnor.ala.pipeline.execution.PipelineExecutorTask;
import org.guvnor.ala.pipeline.execution.PipelineExecutorTaskDef;
import org.guvnor.ala.pipeline.execution.PipelineExecutorTaskManager;
import org.guvnor.ala.registry.PipelineExecutorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PipelineExecutorTaskManagerImpl
        implements PipelineExecutorTaskManager {

    private static final Logger logger = LoggerFactory.getLogger(PipelineExecutorTaskManagerImpl.class);

    //TODO set the proper executor configuration.
    private ExecutorService executor = Executors.newFixedThreadPool(5);

    private Instance<ConfigExecutor> configExecutors;

    private Instance<PipelineEventListener> eventListeners;

    private PipelineExecutor pipelineExecutor = new PipelineExecutor();

    private Map<String, PipelineExecutorTask> pipelineExecutionTaskMap = new HashMap<>();

    private Map<String, Future<?>> futureTaskMap = new HashMap<>();

    private PipelineExecutorRegistry pipelineExecutorRegistry;

    private PipelineEventListener localListener;

    public PipelineExecutorTaskManagerImpl() {
        //Empty constructor for Weld proxying
    }

    @Inject
    public PipelineExecutorTaskManagerImpl(@Any final Instance<ConfigExecutor> configExecutors,
                                           @Any final Instance<PipelineEventListener> eventListeners,
                                           final PipelineExecutorRegistry pipelineExecutorRegistry) {
        this.configExecutors = configExecutors;
        this.eventListeners = eventListeners;
        this.pipelineExecutorRegistry = pipelineExecutorRegistry;
    }

    @PostConstruct
    protected void init() {
        initPipelineExecutor();
        initLocalListener();
    }

    private void initPipelineExecutor() {
        final Collection<ConfigExecutor> configs = new ArrayList<>();
        configExecutors.iterator().forEachRemaining(configs::add);
        pipelineExecutor = new PipelineExecutor(configs);
    }

    private void initLocalListener() {
        localListener = new PipelineEventListener() {
            @Override
            public void beforePipelineExecution(BeforePipelineExecutionEvent bpee) {
                if (isInternal(bpee)) {
                    PipelineExecutorTaskManagerImpl.this.beforePipelineExecution(bpee);
                    notifyExternalListeners(bpee);
                }
            }

            @Override
            public void afterPipelineExecution(AfterPipelineExecutionEvent apee) {
                if (isInternal(apee)) {
                    PipelineExecutorTaskManagerImpl.this.afterPipelineExecution(apee);
                    notifyExternalListeners(apee);
                }
            }

            @Override
            public void beforeStageExecution(BeforeStageExecutionEvent bsee) {
                if (isInternal(bsee)) {
                    PipelineExecutorTaskManagerImpl.this.beforeStageExecution(bsee);
                    notifyExternalListeners(bsee);
                }
            }

            @Override
            public void onStageError(OnErrorStageExecutionEvent oesee) {
                if (isInternal(oesee)) {
                    PipelineExecutorTaskManagerImpl.this.onStageError(oesee);
                    notifyExternalListeners(oesee);
                }
            }

            @Override
            public void afterStageExecution(AfterStageExecutionEvent asee) {
                if (isInternal(asee)) {
                    PipelineExecutorTaskManagerImpl.this.afterStageExecution(asee);
                    notifyExternalListeners(asee);
                }
            }

            @Override
            public void onPipelineError(OnErrorPipelineExecutionEvent oepee) {
                if (isInternal(oepee)) {
                    PipelineExecutorTaskManagerImpl.this.onPipelineError(oepee);
                    notifyExternalListeners(oepee);
                }
            }
        };
    }

    @Override
    public String execute(PipelineExecutorTaskDef taskDef,
                          ExecutionMode executionMode) {
        if (executionMode == ExecutionMode.ASYNCHRONOUS) {
            return executeAsync(taskDef);
        } else {
            return executeSync(taskDef);
        }
    }

    private synchronized String executeAsync(PipelineExecutorTaskDef taskDef) {
        PipelineExecutorTask task = prepareTask(taskDef);
        Future<?> future = executor.submit(() -> pipelineExecutor.execute(taskDef.getInput(),
                                                                          taskDef.getPipeline(),
                                                                          output -> processPipelineOutput(task,
                                                                                                          output),
                                                                          localListener));
        storeFutureTask(task,
                        future);
        return task.getId();
    }

    private String executeSync(PipelineExecutorTaskDef taskDef) {
        PipelineExecutorTask task = prepareTask(taskDef);
        pipelineExecutor.execute(taskDef.getInput(),
                                 taskDef.getPipeline(),
                                 output -> processPipelineOutput(task,
                                                                 output),
                                 localListener);
        return task.getId();
    }

    private void processPipelineOutput(PipelineExecutorTask task,
                                       Object output) {
        ((PipelineExecutorTaskImpl) task).setOutput(output);
    }

    protected void beforePipelineExecution(BeforePipelineExecutionEvent bpee) {
        PipelineExecutorTaskImpl task = getTask(bpee);
        if (task != null) {
            task.setPipelineStatus(PipelineExecutorTask.Status.RUNNING);
        }
    }

    protected void afterPipelineExecution(AfterPipelineExecutionEvent apee) {
        PipelineExecutorTaskImpl task = getTask(apee);
        if (task != null) {
            task.setPipelineStatus(PipelineExecutorTask.Status.FINISHED);
        }
    }

    protected void beforeStageExecution(BeforeStageExecutionEvent bsee) {
        PipelineExecutorTaskImpl task = getTask(bsee);
        if (task != null) {
            task.setStageStatus(bsee.getStage(),
                                PipelineExecutorTask.Status.RUNNING);
        }
    }

    protected void onStageError(OnErrorStageExecutionEvent oesee) {
        PipelineExecutorTaskImpl task = getTask(oesee);
        if (task != null) {
            task.setStageStatus(oesee.getStage(),
                                PipelineExecutorTask.Status.ERROR);
            task.setStageError(oesee.getStage(),
                               oesee.getError());
        }
    }

    protected void afterStageExecution(AfterStageExecutionEvent asee) {
        PipelineExecutorTaskImpl task = getTask(asee);
        if (task != null) {
            task.setStageStatus(asee.getStage(),
                                PipelineExecutorTask.Status.FINISHED);
        }
    }

    protected void onPipelineError(OnErrorPipelineExecutionEvent oepee) {
        PipelineExecutorTaskImpl task = getTask(oepee);
        if (task != null) {
            task.setPipelineStatus(PipelineExecutorTask.Status.ERROR);
            task.setPipelineError(oepee.getError());
        }
    }

    private synchronized PipelineExecutorTaskImpl getTask(PipelineEvent event) {
        return (PipelineExecutorTaskImpl) pipelineExecutionTaskMap.get(event.getExecutionId());
    }

    private synchronized PipelineExecutorTask prepareTask(PipelineExecutorTaskDef taskDef) {
        String executionId = ExecutionIdGenerator.generateExecutionId();
        PipelineExecutorTask task = new PipelineExecutorTaskImpl(taskDef,
                                                                 executionId);
        pipelineExecutionTaskMap.put(task.getId(),
                                     task);
        pipelineExecutorRegistry.register(new PipelineExecutorTraceImpl(task));
        return task;
    }

    private synchronized void storeFutureTask(PipelineExecutorTask task,
                                              Future future) {
        futureTaskMap.put(task.getId(),
                          future);
    }

    /**
     * @param event a Pipeline event.
     * @return true if the event comes from one of the pipeline executions launched by the task manager, false in any
     * other case.
     */
    private boolean isInternal(PipelineEvent event) {
        return getTask(event) != null;
    }

    private void notifyExternalListeners(PipelineEvent event) {
        eventListeners.forEach(listener -> {
            try {
                if (event instanceof BeforePipelineExecutionEvent) {
                    listener.beforePipelineExecution((BeforePipelineExecutionEvent) event);
                } else if (event instanceof BeforeStageExecutionEvent) {
                    listener.beforeStageExecution((BeforeStageExecutionEvent) event);
                } else if (event instanceof AfterStageExecutionEvent) {
                    listener.afterStageExecution((AfterStageExecutionEvent) event);
                } else if (event instanceof AfterPipelineExecutionEvent) {
                    listener.afterPipelineExecution((AfterPipelineExecutionEvent) event);
                } else if (event instanceof OnErrorPipelineExecutionEvent) {
                    listener.onPipelineError((OnErrorPipelineExecutionEvent) event);
                } else if (event instanceof OnErrorStageExecutionEvent) {
                    listener.onStageError((OnErrorStageExecutionEvent) event);
                }
            } catch (Exception e) {
                //if the notification of the event in a particular listener fails let the execution continue.
                logger.error("Pipeline event notification on listener: " + listener + " failed: " + e.getMessage(),
                             e);
            }
        });
    }
}