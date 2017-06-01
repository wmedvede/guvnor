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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.ala.pipeline.Input;
import org.guvnor.ala.services.api.RuntimeQuery;
import org.guvnor.ala.services.api.RuntimeQueryBuilder;
import org.guvnor.ala.services.api.RuntimeQueryResultItem;
import org.guvnor.ala.services.api.backend.PipelineServiceBackend;
import org.guvnor.ala.services.api.backend.RuntimeProvisioningServiceBackend;
import org.guvnor.ala.ui.model.PipelineExecutionTraceKey;
import org.guvnor.ala.ui.model.Provider;
import org.guvnor.ala.ui.model.ProviderKey;
import org.guvnor.ala.ui.model.ProviderTypeKey;
import org.guvnor.ala.ui.model.RuntimeKey;
import org.guvnor.ala.ui.model.RuntimeListItem;
import org.guvnor.ala.ui.model.Source;
import org.guvnor.ala.ui.service.ProviderService;
import org.guvnor.ala.ui.service.RuntimeService;
import org.guvnor.common.services.backend.exceptions.ExceptionUtilities;
import org.jboss.errai.bus.server.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

@Service
@ApplicationScoped
public class RuntimeServiceImpl
        implements RuntimeService {

    private static final Logger logger = LoggerFactory.getLogger(RuntimeServiceImpl.class);

    private RuntimeProvisioningServiceBackend runtimeProvisioningService;

    private PipelineServiceBackend pipelineService;

    private ProviderService providerService;

    public RuntimeServiceImpl() {
        //Empty constructor for Weld proxying
    }

    @Inject
    public RuntimeServiceImpl(final RuntimeProvisioningServiceBackend runtimeProvisioningService,
                              final PipelineServiceBackend pipelineService,
                              final ProviderService providerService) {
        this.runtimeProvisioningService = runtimeProvisioningService;
        this.pipelineService = pipelineService;
        this.providerService = providerService;
    }

    @Override
    public Collection<RuntimeListItem> getRuntimesInfo(final ProviderKey providerKey) {
        checkNotNull("providerKey",
                     providerKey);

        final RuntimeQuery query = RuntimeQueryBuilder.newInstance()
                .withProviderId(providerKey.getId())
                .build();
        return buildRuntimeQueryResult(runtimeProvisioningService.executeQuery(query));
    }

    @Override
    public RuntimeListItem getRuntimeInfo(final PipelineExecutionTraceKey pipelineExecutionTraceKey) {
        checkNotNull("pipelineExecutionTraceKey",
                     pipelineExecutionTraceKey);

        final RuntimeQuery query = RuntimeQueryBuilder.newInstance()
                .withPipelineExecutionId(pipelineExecutionTraceKey.getId())
                .build();
        return buildRuntimeQueryResult(runtimeProvisioningService.executeQuery(query)).stream()
                .findFirst()
                .orElse(null);
    }

    private Collection<RuntimeListItem> buildRuntimeQueryResult(List<RuntimeQueryResultItem> resultItems) {
        final Collection<RuntimeListItem> result = resultItems.stream()
                .map(item -> RuntimeListItemBuilder.newInstance().withItem(item).build())
                .collect(Collectors.toList());
        return result;
    }

    @Override
    public Collection<String> getPipelines(final ProviderTypeKey providerTypeKey) {
        checkNotNull("providerTypeKey",
                     providerTypeKey);

        return pipelineService.getPipelineNames(new org.guvnor.ala.runtime.providers.ProviderType() {
                                                    @Override
                                                    public String getProviderTypeName() {
                                                        return providerTypeKey.getId();
                                                    }

                                                    @Override
                                                    public String getVersion() {
                                                        return providerTypeKey.getVersion();
                                                    }
                                                },
                                                0,
                                                1000,
                                                "name",
                                                true
        );
    }

    @Override
    public String createRuntime(final ProviderKey providerKey,
                                final String runtimeName,
                                final Source source,
                                final String pipelineName) {
        checkNotNull("providerKey",
                     providerKey);
        checkNotNull("runtimeName",
                     runtimeName);
        checkNotNull("source",
                     source);
        checkNotNull("pipelineName",
                     pipelineName);

        final Provider provider = providerService.getProvider(providerKey);
        if (provider == null) {
            logger.error("No provider was found for providerKey: " + providerKey);
            throw new RuntimeException("No provider was found for providerKey: " + providerKey);
        }
        try {
            final Input input = PipelineInputBuilder.newInstance()
                    .withRuntimeName(runtimeName)
                    .withProvider(providerKey)
                    .withSource(source)
                    .build();
            return pipelineService.runPipeline(pipelineName,
                                               input);
        } catch (Exception e) {
            logger.error("Runtime creation failed.");
            throw ExceptionUtilities.handleException(e);
        }
    }

    @Override
    public void start(final RuntimeKey runtimeKey) {

    }

    @Override
    public void stop(final RuntimeKey runtimeKey) {

    }

    @Override
    public void rebuild(final RuntimeKey runtimeKey) {

    }

    @Override
    public void delete(final RuntimeKey runtimeKey) {

    }
}
