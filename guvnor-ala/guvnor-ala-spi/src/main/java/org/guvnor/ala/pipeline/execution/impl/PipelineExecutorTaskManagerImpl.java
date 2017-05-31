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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
import org.guvnor.ala.pipeline.execution.PipelineExecutorException;
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

    private static final int DEFAULT_THREAD_POOL_SIZE = 10;

    private static final String THREAD_POOL_SIZE_PROPERTY_NAME = "org.guvnor.ala.pipeline.execution.threadPoolSize";

    private ExecutorService executor;

    private Instance<ConfigExecutor> configExecutors;

    private Instance<PipelineEventListener> eventListeners;

    private PipelineExecutor pipelineExecutor = new PipelineExecutor();

    private Map<String, PipelineExecutorTask> pipelineExecutionTaskMap = new HashMap<>();

    private Map<String, Future<?>> futureTaskMap = new HashMap<>();

    private PipelineExecutorRegistry pipelineExecutorRegistry;

    private PipelineEventListener localListener;

    /**
     * Set of pipeline execution status that admits the destroy operation.
     */
    private static final Set<PipelineExecutorTask.Status> destroyEnabledStatus = new HashSet<PipelineExecutorTask.Status>() {
        {
            add(PipelineExecutorTask.Status.ERROR);
            add(PipelineExecutorTask.Status.FINISHED);
            add(PipelineExecutorTask.Status.STOPPED);
        }
    };

    /**
     * Set of pipeline execution status that admits the stop operation.
     */
    private static final Set<PipelineExecutorTask.Status> stopEnabledStatus = new HashSet<PipelineExecutorTask.Status>() {
        {
            add(PipelineExecutorTask.Status.RUNNING);
            add(PipelineExecutorTask.Status.SCHEDULED);
        }
    };

    /**
     * Set of pipeline execution that admits the restart operation.
     */
    private static final Set<PipelineExecutorTask.Status> restartEnabledStatus = new HashSet<PipelineExecutorTask.Status>() {
        {
            add(PipelineExecutorTask.Status.STOPPED);
            add(PipelineExecutorTask.Status.ERROR);
            add(PipelineExecutorTask.Status.FINISHED);
        }
    };

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
        initExecutor();
        initPipelineExecutor();
        initLocalListener();
    }

    @PreDestroy
    protected void destroy() {
        try {
            if (executor != null) {
                executor.shutdown();
            }
        } catch (Exception e) {
            logger.error("executor shutdown failed. " + e.getMessage(),
                         e);
        }
    }

    private void initExecutor() {
        final String threadPoolSizeValue = System.getProperties().getProperty(THREAD_POOL_SIZE_PROPERTY_NAME);
        int threadPoolSize;
        if (threadPoolSizeValue == null) {
            threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
            logger.debug(THREAD_POOL_SIZE_PROPERTY_NAME + " property was not set, by default value will be used: " + DEFAULT_THREAD_POOL_SIZE);
        } else {
            try {
                threadPoolSize = Integer.parseInt(threadPoolSizeValue);
                if (threadPoolSize <= 0) {
                    threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
                    logger.error(THREAD_POOL_SIZE_PROPERTY_NAME + " property must be greater than 0, by default value will be used: " + DEFAULT_THREAD_POOL_SIZE);
                } else {
                    logger.debug(THREAD_POOL_SIZE_PROPERTY_NAME + " property will be set to: " + threadPoolSize);
                }
            } catch (Exception e) {
                threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
                logger.error(THREAD_POOL_SIZE_PROPERTY_NAME + " property was set to a wrong value, by default value will be used: " + DEFAULT_THREAD_POOL_SIZE,
                             e);
            }
        }
        executor = Executors.newFixedThreadPool(threadPoolSize);
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
    public String execute(final PipelineExecutorTaskDef taskDef,
                          final ExecutionMode executionMode) {
        if (executionMode == ExecutionMode.ASYNCHRONOUS) {
            return executeAsync(taskDef);
        } else {
            return executeSync(taskDef);
        }
    }

    /**
     * Executes the task definition in asynchronous mode.
     * @param taskDef task definition for executing.
     * @return the taskId assigned to the running task.
     * @see PipelineExecutorTaskDef
     */
    private synchronized String executeAsync(final PipelineExecutorTaskDef taskDef) {
        final PipelineExecutorTask task = prepareTask(taskDef);
        return executeAsync(task);
    }

    /**
     * Executes a task in asynchronous mode.
     * @param task the task for execute.
     * @return the taskId for of the task.
     */
    private synchronized String executeAsync(final PipelineExecutorTask task) {
        final Future<?> future = executor.submit(() -> pipelineExecutor.execute(task.getTaskDef().getInput(),
                                                                                task.getTaskDef().getPipeline(),
                                                                                output -> processPipelineOutput(task,
                                                                                                                output),
                                                                                localListener));
        storeFutureTask(task.getId(),
                        future);
        return task.getId();
    }

    /**
     * Executes a task definition in synchronous mode.
     * @param taskDef task definition for executing.
     * @return the taskId assigned to the executed task.
     */
    private String executeSync(final PipelineExecutorTaskDef taskDef) {
        final PipelineExecutorTask task = prepareTask(taskDef);
        pipelineExecutor.execute(taskDef.getInput(),
                                 taskDef.getPipeline(),
                                 output -> processPipelineOutput(task,
                                                                 output),
                                 localListener);
        return task.getId();
    }

    private void processPipelineOutput(final PipelineExecutorTask task,
                                       final Object output) {
        ((PipelineExecutorTaskImpl) task).setOutput(output);
    }

    @Override
    public void destroy(final String taskId) throws PipelineExecutorException {
        final PipelineExecutorTask task = getSafeTask(taskId);
        final PipelineExecutorTask.Status currentStatus = task.getPipelineStatus();
        if (!destroyEnabledStatus.contains(currentStatus)) {
            throw new PipelineExecutorException("A PipelineExecutorTask in status: " + currentStatus.name() + " can not" +
                                                        " be destroyed. Destroy operation is available for the following status set: " + destroyEnabledStatus);
        }
        pipelineExecutorRegistry.deregister(taskId);
        pipelineExecutionTaskMap.remove(taskId);
        destroyFutureTask(taskId);
    }

    @Override
    public void stop(final String taskId) throws PipelineExecutorException {
        final PipelineExecutorTaskImpl task = getSafeTask(taskId);
        final PipelineExecutorTask.Status currentStatus = task.getPipelineStatus();
        if (!stopEnabledStatus.contains(currentStatus)) {
            throw new PipelineExecutorException("A PipelineExecutorTask in status: " + currentStatus.name() + " can not" +
                                                        " be stopped. Stop operation is available for the following status set: " + stopEnabledStatus);
        }
        destroyFutureTask(taskId);
        pipelineExecutorRegistry.deregister(taskId);
        task.setPipelineStatus(PipelineExecutorTask.Status.STOPPED);
        task.getTaskDef().getPipeline().getStages().forEach(stage -> task.setStageStatus(stage,
                                                                                         PipelineExecutorTask.Status.STOPPED));
        task.setOutput(null);
        task.clearErrors();
        pipelineExecutorRegistry.register(new PipelineExecutorTraceImpl(task));
    }

    @Override
    public void restart(final String taskId) throws PipelineExecutorException {
        final PipelineExecutorTask task = getSafeTask(taskId);
        final PipelineExecutorTask.Status currentStatus = task.getPipelineStatus();
        if (!restartEnabledStatus.contains(currentStatus)) {
            throw new PipelineExecutorException("A PipelineExecutorTask in status: " + currentStatus.name() + " can not" +
                                                        " be restarted. Restart operation is available for the following status set: " + restartEnabledStatus);
        }
        destroyFutureTask(taskId);
        final PipelineExecutorTask newTask = prepareTask(task.getTaskDef(),
                                                         taskId);
        executeAsync(newTask);
    }

    protected void beforePipelineExecution(final BeforePipelineExecutionEvent bpee) {
        PipelineExecutorTaskImpl task = getTask(bpee);
        if (task != null) {
            task.setPipelineStatus(PipelineExecutorTask.Status.RUNNING);
        }
    }

    protected void afterPipelineExecution(final AfterPipelineExecutionEvent apee) {
        PipelineExecutorTaskImpl task = getTask(apee);
        if (task != null) {
            task.setPipelineStatus(PipelineExecutorTask.Status.FINISHED);
        }
    }

    protected void beforeStageExecution(final BeforeStageExecutionEvent bsee) {
        PipelineExecutorTaskImpl task = getTask(bsee);
        if (task != null) {
            task.setStageStatus(bsee.getStage(),
                                PipelineExecutorTask.Status.RUNNING);
        }
    }

    protected void onStageError(final OnErrorStageExecutionEvent oesee) {
        PipelineExecutorTaskImpl task = getTask(oesee);
        if (task != null) {
            task.setStageStatus(oesee.getStage(),
                                PipelineExecutorTask.Status.ERROR);
            task.setStageError(oesee.getStage(),
                               oesee.getError());
        }
    }

    protected void afterStageExecution(final AfterStageExecutionEvent asee) {
        PipelineExecutorTaskImpl task = getTask(asee);
        if (task != null) {
            task.setStageStatus(asee.getStage(),
                                PipelineExecutorTask.Status.FINISHED);
        }
    }

    protected void onPipelineError(final OnErrorPipelineExecutionEvent oepee) {
        PipelineExecutorTaskImpl task = getTask(oepee);
        if (task != null) {
            task.setPipelineStatus(PipelineExecutorTask.Status.ERROR);
            task.setPipelineError(oepee.getError());
        }
    }

    private PipelineExecutorTaskImpl getTask(final PipelineEvent event) {
        return getTask(event.getExecutionId());
    }

    private synchronized PipelineExecutorTaskImpl getTask(final String taskId) {
        return (PipelineExecutorTaskImpl) pipelineExecutionTaskMap.get(taskId);
    }

    private PipelineExecutorTaskImpl getSafeTask(final String taskId) throws PipelineExecutorException {
        final PipelineExecutorTaskImpl task = getTask(taskId);
        if (task == null) {
            throw new PipelineExecutorException("No PipelineExecutorTask was not found for taskId: " + taskId);
        }
        return task;
    }

    private synchronized PipelineExecutorTask prepareTask(final PipelineExecutorTaskDef taskDef) {
        String executionId = ExecutionIdGenerator.generateExecutionId();
        return prepareTask(taskDef,
                           executionId);
    }

    private synchronized PipelineExecutorTask prepareTask(final PipelineExecutorTaskDef taskDef,
                                                          final String executionId) {
        PipelineExecutorTask task = new PipelineExecutorTaskImpl(taskDef,
                                                                 executionId);
        pipelineExecutionTaskMap.put(task.getId(),
                                     task);
        pipelineExecutorRegistry.register(new PipelineExecutorTraceImpl(task));
        return task;
    }

    private synchronized void storeFutureTask(final String taskId,
                                              final Future future) {
        futureTaskMap.put(taskId,
                          future);
    }

    /**
     * Safe method for destroying a Future task. Used only internally.
     * @param taskId the task id to be destroyed.
     * @return true if the task was destroyed with no errors, false in any other case.
     */
    private synchronized boolean destroyFutureTask(final String taskId) {
        final Future future = futureTaskMap.remove(taskId);
        if (future != null && !future.isCancelled() && !future.isDone()) {
            try {
                future.cancel(true);
            } catch (Exception e) {
                logger.error("Cancellation of Future task: " + taskId + " failed. " + e.getMessage(),
                             e);
                return false;
            }
        }
        return true;
    }

    /**
     * @param event a Pipeline event.
     * @return true if the event comes from one of the pipeline executions launched by the task manager, false in any
     * other case.
     */
    private boolean isInternal(final PipelineEvent event) {
        return getTask(event) != null;
    }

    private void notifyExternalListeners(final PipelineEvent event) {
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