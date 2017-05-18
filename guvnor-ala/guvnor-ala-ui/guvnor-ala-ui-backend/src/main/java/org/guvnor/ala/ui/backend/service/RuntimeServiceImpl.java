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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.ala.config.ProviderConfig;
import org.guvnor.ala.config.RuntimeConfig;
import org.guvnor.ala.pipeline.Input;
import org.guvnor.ala.pipeline.execution.PipelineExecutorTask;
import org.guvnor.ala.pipeline.execution.PipelineExecutorTrace;
import org.guvnor.ala.registry.PipelineExecutorRegistry;
import org.guvnor.ala.registry.PipelineRegistry;
import org.guvnor.ala.services.api.RuntimeQuery;
import org.guvnor.ala.services.api.RuntimeQueryResultItem;
import org.guvnor.ala.services.api.backend.PipelineServiceBackend;
import org.guvnor.ala.services.api.backend.RuntimeProvisioningServiceBackend;
import org.guvnor.ala.ui.model.IDataSourceInfo;
import org.guvnor.ala.ui.model.InternalGitSource;
import org.guvnor.ala.ui.model.Pipeline;
import org.guvnor.ala.ui.model.PipelineExecutionTrace;
import org.guvnor.ala.ui.model.PipelineExecutionTraceKey;
import org.guvnor.ala.ui.model.PipelineKey;
import org.guvnor.ala.ui.model.Provider;
import org.guvnor.ala.ui.model.ProviderKey;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.ProviderTypeKey;
import org.guvnor.ala.ui.model.Runtime;
import org.guvnor.ala.ui.model.RuntimeKey;
import org.guvnor.ala.ui.model.RuntimeListItem;
import org.guvnor.ala.ui.model.RuntimeStatus;
import org.guvnor.ala.ui.model.Source;
import org.guvnor.ala.ui.model.StageStatus;
import org.guvnor.ala.ui.model.Step;
import org.guvnor.ala.ui.model.WF10ProviderConfigParams;
import org.guvnor.ala.ui.service.PipelineConstants;
import org.guvnor.ala.ui.service.ProviderService;
import org.guvnor.ala.ui.service.ProviderTypeService;
import org.guvnor.ala.ui.service.RuntimeService;
import org.guvnor.common.services.backend.exceptions.ExceptionUtilities;
import org.jboss.errai.bus.server.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;
import static org.guvnor.ala.ui.backend.service.util.ServiceUtil.putAsStrings;

@Service
@ApplicationScoped
public class RuntimeServiceImpl
        implements RuntimeService {

    private static final Logger logger = LoggerFactory.getLogger(RuntimeServiceImpl.class);

    private Map<ProviderTypeKey, Collection<org.guvnor.ala.pipeline.Pipeline>> internalPipelines = new HashMap<>();

    private RuntimeProvisioningServiceBackend runtimeProvisioningService;

    private PipelineServiceBackend pipelineServiceBackend;

    private ProviderTypeService providerTypeService;

    private ProviderService providerService;

    private PipelineRegistry pipelineRegistry;

    private PipelineExecutorRegistry pipelineExecutorRegistry;

    public RuntimeServiceImpl() {
        //Empty constructor for Weld proxying
    }

    @Inject
    public RuntimeServiceImpl(RuntimeProvisioningServiceBackend runtimeProvisioningService,
                              PipelineServiceBackend pipelineServiceBackend,
                              PipelineRegistry pipelineRegistry,
                              ProviderTypeService providerTypeService,
                              ProviderService providerService,
                              PipelineExecutorRegistry pipelineExecutorRegistry) {
        this.runtimeProvisioningService = runtimeProvisioningService;
        this.pipelineServiceBackend = pipelineServiceBackend;
        this.pipelineRegistry = pipelineRegistry;
        this.providerTypeService = providerTypeService;
        this.providerService = providerService;
        this.pipelineExecutorRegistry = pipelineExecutorRegistry;
    }

    @PostConstruct
    public void init() {
        List<org.guvnor.ala.pipeline.Pipeline> pipelines = pipelineRegistry.getPipelines(0,
                                                                                         10,
                                                                                         "name",
                                                                                         true);
        providerTypeService.getAvialableProviderTypes()
                .forEach(providerType -> internalPipelines.putIfAbsent(providerType.getKey(),
                                                                       new HashSet<>()));
        pipelines.forEach(pipeline -> {
            ProviderType providerType = getSupportedProviderType(pipeline);
            if (providerType != null) {
                internalPipelines.get(providerType.getKey()).add(pipeline);
            }
        });
    }

    private ProviderType getSupportedProviderType(org.guvnor.ala.pipeline.Pipeline pipeline) {
        //TODO we need and abstraction or something to get the ProviderType for a given pipeline.
        return providerTypeService.getAvialableProviderTypes()
                .stream()
                .filter(providerType -> pipeline.getName().startsWith(providerType.getKey().getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Runtime getRuntime(final RuntimeKey runtimeKey) {
        List<org.guvnor.ala.runtime.Runtime> runtimes = runtimeProvisioningService.getRuntimes(0,
                                                                                               10,
                                                                                               "id",
                                                                                               true);
        return runtimes.stream()
                .filter(runtime -> {
                    //TODO check this filtering...
                    return runtime.getId().equals(runtimeKey.getId()) &&
                            runtime.getProviderId().getId().equals(runtimeKey.getProviderKey().getId());
                })
                .map(runtime -> convertToUIRuntime(runtime,
                                                   runtimeKey.getProviderKey())
                ).findFirst().orElse(null);
    }

    @Override
    public Collection<Runtime> getRuntimes(final ProviderKey providerKey) {

        /*
        Collection<Runtime> result = pipelineExecutorRegistry.getExecutorTraces().stream()
                .filter(record -> {
                    //TODO check this filtering
                    ProviderId providerId = record.getTask().getTaskDef().getProviderId();
                    return providerId != null && providerId.getId().equals(providerKey.getId());
                })
                .map(record -> convertToUIRuntime(record))
                .collect(Collectors.toList());

        return result;
        */

        RuntimeQuery query = new RuntimeQuery(providerKey.getId());

        List<RuntimeQueryResultItem> resultItems = runtimeProvisioningService.executeQuery(query);
        Collection<Runtime> result = resultItems.stream()
                .map( item -> convertToUIRuntime(item) ).collect(Collectors.toList());
        return result;
    }

    @Override
    public Collection<RuntimeListItem> getRuntimesInfo(final ProviderKey providerKey) {
        RuntimeQuery query = new RuntimeQuery(providerKey.getId());
        List<RuntimeQueryResultItem> resultItems = runtimeProvisioningService.executeQuery(query);
        Collection<RuntimeListItem> result = resultItems.stream()
                .map( item -> convertToUIRuntimeListItem(item) ).collect(Collectors.toList());
        return result;
    }

    private RuntimeListItem convertToUIRuntimeListItem(RuntimeQueryResultItem item) {

        RuntimeListItem result;
        PipelineExecutionTrace pipelineTrace;
        Runtime runtime;
        String runtimeName;

        if (item.getRuntimeName() != null) {
            runtimeName = item.getRuntimeName();
        } else {
            runtimeName = item.getRuntimeId();
        }

        if (item.getPipelineExecutionId() != null) {
            Pipeline pipeline = new Pipeline(new PipelineKey(item.getPipelineId()));
            pipelineTrace = new PipelineExecutionTrace(new PipelineExecutionTraceKey(item.getPipelineExecutionId()));
            item.getPipelineStageItems().getItems()
                    .forEach(stage -> {
                                 pipeline.addStep(new Step(pipeline.getKey(),
                                                           stage.getName(),
                                                           transformToStageStatus(stage.getStatus())));
                                 pipelineTrace.setStageStatus(stage.getName(),
                                                              transformToStageStatus(stage.getStatus()));
                                 pipelineTrace.setStageError(stage.getName(),
                                                             stage.getErrorMessage());
                             }
                    );
            pipelineTrace.setPipeline(pipeline);
        } else {
            pipelineTrace = null;
        }

        if (item.getRuntimeId() != null) {
            runtime = new Runtime(new RuntimeKey(new ProviderKey(new ProviderTypeKey(item.getProviderTypeName()),
                                                                 item.getProviderId()),
                                                 runtimeName),
                                  transformToRuntimeStatus(item.getRuntimeStatus()),
                                  item.getRuntimeEndpoint(),
                                  "not yet implemented");
            runtime.setPipelineTrace(pipelineTrace);
            result = new RuntimeListItem(runtimeName,
                                         runtime);
        } else {
            result = new RuntimeListItem(runtimeName,
                                         pipelineTrace);
        }
        return result;
    }


    private Runtime convertToUIRuntime(RuntimeQueryResultItem item) {

        Runtime result;
        String runtimeName = null;
        if ( item.getRuntimeName() != null ) {
            runtimeName = item.getRuntimeName();
        } else {
            runtimeName = item.getRuntimeId();
        }

        if (item.getPipelineExecutionId() != null) {
            //we have a pipeline
            result = new Runtime(new RuntimeKey(new ProviderKey(new ProviderTypeKey(item.getProviderTypeName()),
                                                                        item.getProviderId()),
                                                        runtimeName),
                                         transformToRuntimeStatus(item.getRuntimeStatus()),
                                         "endpoint",
                                         "started at");

            Pipeline pipeline = new Pipeline(new PipelineKey(item.getPipelineId()));
            PipelineExecutionTrace pipelineTrace = new PipelineExecutionTrace(new PipelineExecutionTraceKey(item.getPipelineExecutionId()));
            item.getPipelineStageItems().getItems()
                    .forEach(stage -> {
                                 pipeline.addStep(new Step(pipeline.getKey(),
                                                           stage.getName(),
                                                           transformToStageStatus(stage.getStatus())));
                                 pipelineTrace.setStageStatus(stage.getName(),
                                                              transformToStageStatus(stage.getStatus()));
                             }
                    );

            pipelineTrace.setPipeline(pipeline);
            result.setPipelineTrace(pipelineTrace);

        } else {
            result = new Runtime(new RuntimeKey(new ProviderKey(new ProviderTypeKey(item.getProviderTypeName()),
                                                                                     item.getProviderId()),
                                                                     runtimeName),
                                                      transformToRuntimeStatus(item.getRuntimeStatus()),
                                                      "endpoint",
                                                      "started at");
        }
        return result;
    }

    private RuntimeKey createRuntimeKey(PipelineExecutorTrace record) {
        return new RuntimeKey(new ProviderKey(new ProviderTypeKey(record.getTask().getTaskDef().getProviderId().getProviderType().getProviderTypeName()),
                                              record.getTask().getTaskDef().getProviderId().getId()),
                              record.getTask().getTaskDef().getRuntimeId().getId());
    }

    private ProviderKey createProviderKey(PipelineExecutorTrace record) {
        return new ProviderKey(new ProviderTypeKey(record.getTask().getTaskDef().getProviderId().getProviderType().getProviderTypeName()),
                               record.getTask().getTaskDef().getProviderId().getId());
    }

    private ProviderTypeKey createProviderTypeKey(PipelineExecutorTrace record) {
        return new ProviderTypeKey(record.getTask().getTaskDef().getProviderId().getProviderType().getProviderTypeName());
    }

    private RuntimeStatus transformToRuntimeStatus(PipelineExecutorTask.Status status) {
        if (status == null) {
            return null;
        } else {
            switch (status) {
                case SCHEDULED:
                case RUNNING:
                    return RuntimeStatus.LOADING;
                case FINISHED:
                    return RuntimeStatus.STARTED;
                case ERROR:
                    return RuntimeStatus.ERROR;
            }
            return null;
        }
    }

    private RuntimeStatus transformToRuntimeStatus(String status) {
        if (status == null) {
            return null;
        } else {
            switch (status) {
                case "SCHEDULED":
                case "RUNNING":
                    return RuntimeStatus.LOADING;
                case "FINISHED":
                    return RuntimeStatus.STARTED;
                case "ERROR":
                    return RuntimeStatus.ERROR;
            }
            return null;
        }
    }

    private StageStatus transformToStageStatus(PipelineExecutorTask.Status status) {
        if (status == null) {
            return null;
        } else {
            switch (status) {
                case SCHEDULED:
                    return StageStatus.SCHEDULED;
                case RUNNING:
                    return StageStatus.RUNNING;
                case FINISHED:
                    return StageStatus.FINISHED;
                case ERROR:
                    return StageStatus.ERROR;
            }
            return null;
        }
    }

    private StageStatus transformToStageStatus(String status) {
        if (status == null) {
            return null;
        } else {
            switch (status) {
                case "SCHEDULED":
                    return StageStatus.SCHEDULED;
                case "RUNNING":
                    return StageStatus.RUNNING;
                case "FINISHED":
                    return StageStatus.FINISHED;
                case "ERROR":
                    return StageStatus.ERROR;
            }
            return null;
        }
    }

    private Runtime convertToUIRuntime(org.guvnor.ala.runtime.Runtime runtime,
                                       ProviderKey providerKey) {

        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    private RuntimeStatus convertToUIRuntimeStatus(String status) {
        //TODO check this conversion, because the status is apparently an arbitrary String in the guvnor-ala-service world
        return RuntimeStatus.STARTED;
    }

    @Override
    public Collection<String> getPipelines(final ProviderKey providerKey) {
        return print("getPipelines",
                     getPipelines(providerKey.getProviderTypeKey()));
    }

    @Override
    public Collection<String> getPipelines(final ProviderTypeKey providerTypeKey) {
        return internalPipelines.getOrDefault(providerTypeKey,
                                              new ArrayList<>())
                .stream()
                .map(org.guvnor.ala.pipeline.Pipeline::getName)
                .sorted(new Comparator<String>() {
                    @Override
                    public int compare(String o1,
                                       String o2) {
                        //force the WILDFLY_PROVISIONING_PIPELINE to the left ;)
                        return o1.equals(PipelineConstants.WILDFLY_PROVISIONING_PIPELINE) ? -1 : 1;
                    }
                })
                .collect(toList());
    }

    @Override
    public String createRuntime(ProviderKey providerKey,
                              String runtimeId,
                              Source source,
                              String pipelineName) {

        Provider provider = providerService.getProvider(providerKey);
        if (provider == null) {
            throw new RuntimeException("No provider was found for providerKey: " + providerKey);
        }

        org.guvnor.ala.pipeline.Pipeline pipeline =
                internalPipelines.getOrDefault(provider.getKey().getProviderTypeKey(),
                                               Collections.emptyList())
                        .stream()
                        .filter(p -> p.getName().equals(pipelineName))
                        .findFirst().orElse(null);
        if (pipeline == null) {
            throw new RuntimeException("No pipeline was found for provider: " + provider + "and pipelineName: " + pipelineName);
        }

        try {
            Input input = buildPipelineInput(provider,
                                             pipeline,
                                             runtimeId,
                                             source);
            return pipelineServiceBackend.runPipeline(pipelineName, input);
        } catch (Exception e) {
            logger.error("Runtime creation failed.");
            throw ExceptionUtilities.handleException(e);
        }

    }

    /*
    public void createRuntimeOLDPipelineRunner(ProviderKey providerKey,
                                               String runtimeId,
                                               Source source,
                                               String pipelineName) {
        Provider provider = providerService.getProvider(providerKey);
        if (provider == null) {
            throw new RuntimeException("No provider was found for providerKey: " + providerKey);
        }

        org.guvnor.ala.pipeline.Pipeline pipeline =
                internalPipelines.getOrDefault(provider.getKey().getProviderTypeKey(),
                                               Collections.emptyList())
                        .stream()
                        .filter(p -> p.getName().equals(pipelineName))
                        .findFirst().orElse(null);
        if (pipeline == null) {
            throw new RuntimeException("No pipeline was found for provider: " + provider + "and pipelineName: " + pipelineName);
        }

        Input input = buildPipelineInput(provider,
                                         pipeline,
                                         runtimeId,
                                         source);
        //TODO review this ProviderId
        ProviderId alaProviderId = new ProviderId() {
            @Override
            public String getId() {
                return providerKey.getId();
            }

            @Override
            public org.guvnor.ala.runtime.providers.ProviderType getProviderType() {
                return new org.guvnor.ala.runtime.providers.ProviderType() {
                    @Override
                    public String getProviderTypeName() {
                        return provider.getKey().getProviderTypeKey().getId();
                    }

                    @Override
                    public String getVersion() {
                        return null;
                    }
                };
            }
        };
        RuntimeId alaRuntimeId = new RuntimeId() {
            @Override
            public String getId() {
                return runtimeId;
            }

            @Override
            public String getName() {
                return runtimeId;
            }

            @Override
            public ProviderId getProviderId() {
                return alaProviderId;
            }
        };
        PipelineExecutionTaskDef task = new PipelineExecutionTaskDefImpl(pipeline,
                                                                         input,
                                                                         alaProviderId,
                                                                         alaRuntimeId);
        pipelineExecutionTaskManager.execute(task);
    }

    */

    private Input buildPipelineInput(Provider provider,
                                     org.guvnor.ala.pipeline.Pipeline pipeline,
                                     String runtimeId,
                                     Source source) {
        //TODO build the proper Input according with the provider and pipeline.

        Input input = new Input();

        input.put(RuntimeConfig.RUNTIME_NAME, runtimeId);

        input.put(ProviderConfig.PROVIDER_NAME,
                  provider.getKey().getId());
        input.put(WF10ProviderConfigParams.MANAGEMENT_REALM,
                  "ManagementRealm");
        putAsStrings(input,
                     provider.getValues());

        input.put("repo-name",
                  ((InternalGitSource) source).getRepository());
        input.put("branch",
                  ((InternalGitSource) source).getBranch());
        input.put("project-dir",
                  ((InternalGitSource) source).getProject());

        if (((InternalGitSource) source).getDataSourceInfo() != null) {
            IDataSourceInfo dataSourceInfo = ((InternalGitSource) source).getDataSourceInfo();
            input.put("jndi-data-source",
                      dataSourceInfo.getJndi());
            if (dataSourceInfo.isKieDataSource()) {
                input.put("kie-data-source",
                          dataSourceInfo.getKieUuid());
                input.put("kie-data-source-deployment-id",
                          dataSourceInfo.getDeploymentId());
            }
        }

        /*
        put( "repo-name", repository.getAlias() );
        put( "branch", repository.getDefaultBranch() );
        put( "project-dir", project.getProjectName() );
        put( "provider-name", "local" );
        put( "wildfly-user", "testadmin" );
        put( "wildfly-password", "testadmin" );
        put( "bindAddress", "localhost" );
        put( "host", "localhost" );
        put( "port", "8888" );
        put( "management-port", "9990" );

        input.put(pro)
        */


        /*

        Map< String, String > result = new HashMap<>( );
        DataSourceInfo dataSourceInfo = model.getDataSourceInfo( );
        result.put( "host", model.getHost( ) );
        result.put( "port", String.valueOf( model.getPort( ) ) );
        result.put( "management-port", String.valueOf( model.getManagementPort( ) ) );
        result.put( "management-realm", model.getManagementRealm( ) );
        result.put( "wildfly-user", model.getManagementUser( ) );
        result.put( "wildfly-password", model.getManagementPassword( ) );
        result.put( "wildfly-realm", model.getManagementRealm( ) );
        result.put( "jndi-data-source", dataSourceInfo.getJndi( ) );
        if ( dataSourceInfo.isKieDataSource( ) ) {
            result.put( "kie-data-source", dataSourceInfo.getKieUuid( ) );
            result.put( "kie-data-source-deployment-id", dataSourceInfo.getDeploymentId( ) );
        }
        return result;
         */
        return input;
    }

    @Override
    public void start(RuntimeKey runtimeKey) {

    }

    @Override
    public void stop(RuntimeKey runtimeKey) {

    }

    @Override
    public void rebuild(RuntimeKey runtimeKey) {

    }

    @Override
    public void delete(RuntimeKey runtimeKey) {

    }

    public <T> Collection<T> print(final String ref,
                                   final Collection<T> input) {

        System.out.println(ref + " [" + input.toString() + "]");

        return input;
    }
}
