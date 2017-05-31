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

package org.guvnor.ala.pipeline.execution;

/**
 * Task manager for executing PipelineExecutionTaskDefs.
 */
public interface PipelineExecutorTaskManager {

    enum ExecutionMode {
        SYNCHRONOUS,
        ASYNCHRONOUS
    }

    /**
     * Starts the execution of a task based on the taskDef provided as parameter.
     * @param taskDef a task definition to be executed.
     * @param executionMode indicates if the task should be executed in synchronous or asynchronous mode.
     * @return returns uuid of the created task.
     * @see PipelineExecutorTaskDef
     */
    String execute(final PipelineExecutorTaskDef taskDef,
                   final ExecutionMode executionMode);

    /**
     * Destroys a pipeline executor task.
     * @param taskId the id of the task to be destroyed.
     * @throws PipelineExecutorException
     */
    void destroy(final String taskId) throws PipelineExecutorException;

    /**
     * Stops a pipeline executor task.
     * @param taskId the id of the task to stop.
     * @throws PipelineExecutorException
     */
    void stop(final String taskId) throws PipelineExecutorException;

    /**
     * @param taskId
     * @throws PipelineExecutorException
     */
    void restart(final String taskId) throws PipelineExecutorException;
}