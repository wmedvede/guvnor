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

package org.guvnor.ala.registry.local;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;

import org.guvnor.ala.pipeline.execution.PipelineExecutorTrace;
import org.guvnor.ala.registry.PipelineExecutorRegistry;
import org.uberfire.commons.validation.PortablePreconditions;

@ApplicationScoped
public class InMemoryPipelineExecutorRegistry
        implements PipelineExecutorRegistry {

    private Map<String, PipelineExecutorTrace> recordsMap = new ConcurrentHashMap<>();

    public InMemoryPipelineExecutorRegistry() {
        //Empty constructor for Weld proxying
    }

    @Override
    public void register(final PipelineExecutorTrace record) {
        PortablePreconditions.checkNotNull("record",
                                           record);
        recordsMap.put(record.getTaskId(),
                       record);
    }

    public void deregister(final String taskId) {
        if (taskId != null) {
            recordsMap.remove(taskId);
        }
    }

    @Override
    public PipelineExecutorTrace getExecutorTrace(final String pipelineExecutionId) {
        return recordsMap.get(pipelineExecutionId);
    }

    @Override
    public Collection<PipelineExecutorTrace> getExecutorTraces() {
        return recordsMap.values();
    }

    @Override
    public Collection<PipelineExecutorTrace> getExecutorTraces(final String pipelineId) {
        PortablePreconditions.checkNotNull("pipelineId",
                                           pipelineId);
        return getExecutorTraces().stream()
                .filter(trace -> pipelineId.equals(trace.getPipelineId()))
                .collect(Collectors.toList());
    }
}