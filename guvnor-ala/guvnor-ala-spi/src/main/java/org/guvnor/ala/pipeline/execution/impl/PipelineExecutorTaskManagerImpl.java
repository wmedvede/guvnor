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

    private PipelineExecutor pipelineExecutor;

    private final Map<String, TaskEntry> currentTasks = new HashMap<>();

    private final Map<String, Future<?>> futureTaskMap = new HashMap<>();

    private PipelineExecutorRegistry pipelineExecutorRegistry;

    private PipelineEventListener localListener;

    /**
     * Set of pipeline execution status that admits the stop operation.
     */
    private static final Set<PipelineExecutorTask.Status> stopEnabledStatus = new HashSet<PipelineExecutorTask.Status>() {
        {
            add(PipelineExecutorTask.Status.RUNNING);
            add(PipelineExecutorTask.Status.SCHEDULED);
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
            synchronized (currentTasks) {
                final Set<TaskEntry> entrySet = new HashSet<>();
                entrySet.addAll(currentTasks.values());
                entrySet.forEach(entry -> {
                    currentTasks.remove(entry.getTask().getId());
                    if (entry.isAsynch()) {
                        final PipelineExecutorTaskImpl task = entry.getTask();
                        if (stopEnabledStatus.add(task.getPipelineStatus())) {
                            try {
                                setTaskInStoppedStatus(task);
                                updateExecutorRegistry(task);
                            } catch (Exception e) {
                                logger.error("It was not possible to update task: " + task.getId() + " during " +
                                                     " PipelineExecutorTaskManager finalization. " + e.getMessage(),
                                             e);
                            }
                        }
                    }
                });
            }
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
            public void beforePipelineExecution(final BeforePipelineExecutionEvent bpee) {
                final TaskEntry taskEntry = getTaskEntry(bpee.getExecutionId());
                if (taskEntry != null) {
                    PipelineExecutorTaskManagerImpl.this.beforePipelineExecution(bpee,
                                                                                 taskEntry);
                    notifyExternalListeners(bpee);
                }
            }

            @Override
            public void afterPipelineExecution(final AfterPipelineExecutionEvent apee) {
                final TaskEntry taskEntry = getTaskEntry(apee.getExecutionId());
                if (taskEntry != null) {
                    PipelineExecutorTaskManagerImpl.this.afterPipelineExecution(apee,
                                                                                taskEntry);
                    notifyExternalListeners(apee);
                }
            }

            @Override
            public void beforeStageExecution(final BeforeStageExecutionEvent bsee) {
                final TaskEntry taskEntry = getTaskEntry(bsee.getExecutionId());
                if (taskEntry != null) {
                    PipelineExecutorTaskManagerImpl.this.beforeStageExecution(bsee,
                                                                              taskEntry);
                    notifyExternalListeners(bsee);
                }
            }

            @Override
            public void onStageError(final OnErrorStageExecutionEvent oesee) {
                final TaskEntry taskEntry = getTaskEntry(oesee.getExecutionId());
                if (taskEntry != null) {
                    PipelineExecutorTaskManagerImpl.this.onStageError(oesee,
                                                                      taskEntry);
                    notifyExternalListeners(oesee);
                }
            }

            @Override
            public void afterStageExecution(final AfterStageExecutionEvent asee) {
                final TaskEntry taskEntry = getTaskEntry(asee.getExecutionId());
                if (taskEntry != null) {
                    PipelineExecutorTaskManagerImpl.this.afterStageExecution(asee,
                                                                             taskEntry);
                    notifyExternalListeners(asee);
                }
            }

            @Override
            public void onPipelineError(final OnErrorPipelineExecutionEvent oepee) {
                final TaskEntry taskEntry = getTaskEntry(oepee.getExecutionId());
                if (taskEntry != null) {
                    PipelineExecutorTaskManagerImpl.this.onPipelineError(oepee,
                                                                         taskEntry);
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
        final PipelineExecutorTaskImpl task = createTask(taskDef);
        storeTaskEntry(TaskEntry.newAsynchEntry(task));
        startAsyncTask(task);
        updateExecutorRegistry(task);
        return task.getId();
    }

    /**
     * Executes a task in asynchronous mode.
     * @param task the task for execute.
     * @return the taskId for of the task.
     */
    private synchronized void startAsyncTask(final PipelineExecutorTask task) {
        final Future<?> future = executor.submit(() -> {
            pipelineExecutor.execute(task.getTaskDef().getInput(),
                                     task.getTaskDef().getPipeline(),
                                     output -> processPipelineOutput(task,
                                                                     output),
                                     localListener);
            removeTaskEntry(task.getId());
        });

        storeFutureTask(task.getId(),
                        future);
    }

    /**
     * Executes a task definition in synchronous mode.
     * @param taskDef task definition for executing.
     * @return the taskId assigned to the executed task.
     */
    private String executeSync(final PipelineExecutorTaskDef taskDef) {
        final PipelineExecutorTaskImpl task = createTask(taskDef);
        storeTaskEntry(TaskEntry.newSychEntry(task));
        pipelineExecutor.execute(taskDef.getInput(),
                                 taskDef.getPipeline(),
                                 output -> processPipelineOutput(task,
                                                                 output),
                                 localListener);
        removeTaskEntry(task.getId());
        updateExecutorRegistry(task);
        return task.getId();
    }

    private void processPipelineOutput(final PipelineExecutorTask task,
                                       final Object output) {
        ((PipelineExecutorTaskImpl) task).setOutput(output);
    }

    @Override
    public void stop(final String taskId) throws PipelineExecutorException {
        final TaskEntry entry = getTaskEntry(taskId);
        if (entry == null) {
            throw new PipelineExecutorException("No PipelineExecutorTask was found for taskId: " + taskId);
        }
        if (!entry.isAsynch()) {
            throw new PipelineExecutorException("Stop operation is not available for taskId: " + taskId +
                                                        " running in SYNCHRONOUS mode");
        }
        final PipelineExecutorTask.Status currentStatus = entry.getTask().getPipelineStatus();
        if (!stopEnabledStatus.contains(currentStatus)) {
            throw new PipelineExecutorException("A PipelineExecutorTask in status: " + currentStatus.name() + " can not" +
                                                        " be stopped. Stop operation is available for the following status set: " + stopEnabledStatus);
        }
        destroyFutureTask(taskId);
        removeTaskEntry(taskId);
        setTaskInStoppedStatus(entry.getTask());
        updateExecutorRegistry(entry.getTask());
    }

    @Override
    public void destroy(final String taskId) throws PipelineExecutorException {
        final TaskEntry entry = getTaskEntry(taskId);
        if (entry == null) {
            throw new PipelineExecutorException("No PipelineExecutorTask was found for taskId: " + taskId);
        }
        if (!entry.isAsynch()) {
            throw new PipelineExecutorException("Destroy operation is not available for taskId: " + taskId +
                                                        " running in SYNCHRONOUS mode");
        }
        destroyFutureTask(taskId);
        removeTaskEntry(taskId);
        pipelineExecutorRegistry.deregister(taskId);
    }

    protected void beforePipelineExecution(final BeforePipelineExecutionEvent bpee,
                                           final TaskEntry taskEntry) {
        taskEntry.getTask().setPipelineStatus(PipelineExecutorTask.Status.RUNNING);
        if (taskEntry.isAsynch()) {
            updateExecutorRegistry(taskEntry.getTask());
        }
    }

    protected void afterPipelineExecution(final AfterPipelineExecutionEvent apee,
                                          final TaskEntry taskEntry) {
        taskEntry.getTask().setPipelineStatus(PipelineExecutorTask.Status.FINISHED);
        if (taskEntry.isAsynch()) {
            updateExecutorRegistry(taskEntry.getTask());
        }
    }

    protected void beforeStageExecution(final BeforeStageExecutionEvent bsee,
                                        final TaskEntry taskEntry) {
        taskEntry.getTask().setStageStatus(bsee.getStage(),
                                           PipelineExecutorTask.Status.RUNNING);
        if (taskEntry.isAsynch()) {
            updateExecutorRegistry(taskEntry.getTask());
        }
    }

    protected void onStageError(final OnErrorStageExecutionEvent oesee,
                                final TaskEntry taskEntry) {
        taskEntry.getTask().setStageStatus(oesee.getStage(),
                                           PipelineExecutorTask.Status.ERROR);
        taskEntry.getTask().setStageError(oesee.getStage(),
                                          oesee.getError());
        if (taskEntry.isAsynch()) {
            updateExecutorRegistry(taskEntry.getTask());
        }
    }

    protected void afterStageExecution(final AfterStageExecutionEvent asee,
                                       final TaskEntry taskEntry) {
        taskEntry.getTask().setStageStatus(asee.getStage(),
                                           PipelineExecutorTask.Status.FINISHED);
        if (taskEntry.isAsynch()) {
            updateExecutorRegistry(taskEntry.getTask());
        }
    }

    protected void onPipelineError(final OnErrorPipelineExecutionEvent oepee,
                                   final TaskEntry taskEntry) {

        taskEntry.getTask().setPipelineStatus(PipelineExecutorTask.Status.ERROR);
        taskEntry.getTask().setPipelineError(oepee.getError());
        if (taskEntry.isAsynch()) {
            updateExecutorRegistry(taskEntry.getTask());
        }
    }

    private synchronized TaskEntry getTaskEntry(final String taskId) {
        return currentTasks.get(taskId);
    }

    private synchronized void removeTaskEntry(final String taskId) {
        currentTasks.remove(taskId);
    }

    private synchronized void storeTaskEntry(final TaskEntry entry) {
        currentTasks.put(entry.task.getId(),
                         entry);
    }

    private PipelineExecutorTaskImpl createTask(final PipelineExecutorTaskDef taskDef) {
        String executionId = ExecutionIdGenerator.generateExecutionId();
        return createTask(taskDef,
                          executionId);
    }

    private PipelineExecutorTaskImpl createTask(final PipelineExecutorTaskDef taskDef,
                                                final String executionId) {
        PipelineExecutorTaskImpl task = new PipelineExecutorTaskImpl(taskDef,
                                                                     executionId);
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

    private void updateExecutorRegistry(final PipelineExecutorTaskImpl task) {
        try {
            PipelineExecutorTaskImpl clone = (PipelineExecutorTaskImpl) task.clone();
            pipelineExecutorRegistry.register(new PipelineExecutorTraceImpl(clone));
        } catch (Exception e) {
            //clone is supported by construction, since PipelineExecutorTaskImpl is clonable.
            logger.error("Unexpected error: " + e.getMessage(),
                         e);
        }
    }

    private void setTaskInStoppedStatus(final PipelineExecutorTaskImpl task) {
        task.setPipelineStatus(PipelineExecutorTask.Status.STOPPED);
        task.getTaskDef().getPipeline().getStages().forEach(
                stage -> task.setStageStatus(stage,
                                             PipelineExecutorTask.Status.STOPPED)
        );
        task.clearErrors();
        task.setOutput(null);
    }

    private static class TaskEntry {

        private PipelineExecutorTaskImpl task;

        private ExecutionMode executionMode;

        private TaskEntry(PipelineExecutorTaskImpl task,
                          ExecutionMode executionMode) {
            this.task = task;
            this.executionMode = executionMode;
        }

        public static TaskEntry newAsynchEntry(PipelineExecutorTaskImpl task) {
            return new TaskEntry(task,
                                 ExecutionMode.ASYNCHRONOUS);
        }

        public static TaskEntry newSychEntry(PipelineExecutorTaskImpl task) {
            return new TaskEntry(task,
                                 ExecutionMode.SYNCHRONOUS);
        }

        public PipelineExecutorTaskImpl getTask() {
            return task;
        }

        public boolean isAsynch() {
            return ExecutionMode.ASYNCHRONOUS == executionMode;
        }
    }
}