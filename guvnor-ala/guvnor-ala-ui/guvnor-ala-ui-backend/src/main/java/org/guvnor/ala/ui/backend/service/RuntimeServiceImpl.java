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
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.guvnor.ala.pipeline.Input;
import org.guvnor.ala.registry.PipelineRegistry;
import org.guvnor.ala.runtime.RuntimeId;
import org.guvnor.ala.runtime.providers.ProviderId;
import org.guvnor.ala.services.api.backend.PipelineServiceBackend;
import org.guvnor.ala.services.api.backend.RuntimeProvisioningServiceBackend;
import org.guvnor.ala.ui.backend.service.pipeline.PipelineExecutionRecord;
import org.guvnor.ala.ui.backend.service.pipeline.PipelineExecutionRegistry;
import org.guvnor.ala.ui.backend.service.pipeline.PipelineExecutionTask;
import org.guvnor.ala.ui.backend.service.pipeline.PipelineExecutionTaskManager;
import org.guvnor.ala.ui.events.NewPipelineStep;
import org.guvnor.ala.ui.events.RuntimeStatusChange;
import org.guvnor.ala.ui.model.IDataSourceInfo;
import org.guvnor.ala.ui.model.InternalGitSource;
import org.guvnor.ala.ui.model.Pipeline;
import org.guvnor.ala.ui.model.ProviderKey;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.ProviderTypeKey;
import org.guvnor.ala.ui.model.RuntimeKey;
import org.guvnor.ala.ui.model.RuntimeStatus;
import org.guvnor.ala.ui.model.StageStatus;
import org.guvnor.ala.ui.model.Step;
import org.guvnor.ala.ui.model.WF10ProviderConfigParams;
import org.guvnor.ala.ui.service.PipelineConstants;
import org.guvnor.ala.ui.service.ProviderService;
import org.guvnor.ala.ui.service.ProviderTypeService;
import org.jboss.errai.bus.server.annotations.Service;
import org.guvnor.ala.ui.model.Provider;
import org.guvnor.ala.ui.model.Source;
import org.guvnor.ala.ui.service.RuntimeService;
import org.guvnor.ala.ui.model.Runtime;

import static java.util.stream.Collectors.*;
import static org.guvnor.ala.ui.backend.service.util.ServiceUtil.putAsStrings;

@Service
@ApplicationScoped
public class RuntimeServiceImpl
        implements RuntimeService {

    private final Map<ProviderKey, Collection<Runtime>> runtimes = new HashMap<>();

    private Map<ProviderType, Collection<ExecutablePipeline>> templatePipelines = new HashMap<>();

    private Map<ProviderTypeKey, Collection<org.guvnor.ala.pipeline.Pipeline>> internalPipelines = new HashMap<>();

    private Instance<ExecutablePipeline> executablePipelineInstance;

    private Event<NewPipelineStep> newPipelineStep;

    private Event<RuntimeStatusChange> runtimeStatusChange;

    private RuntimeProvisioningServiceBackend runtimeProvisioningService;

    private ProviderTypeService providerTypeService;

    private ProviderService providerService;

    private PipelineRegistry pipelineRegistry;

    private PipelineServiceBackend pipelineService;

    private PipelineExecutionTaskManager pipelineExecutionTaskManager;

    private PipelineExecutionRegistry pipelineExecutionRegistry;

    public RuntimeServiceImpl( ) {
        //Empty constructor for Weld proxying
    }

    @Inject
    public RuntimeServiceImpl( Instance<ExecutablePipeline> executablePipelineInstance,
                               Event<NewPipelineStep> newPipelineStep,
                               Event<RuntimeStatusChange> runtimeStatusChange,
                               RuntimeProvisioningServiceBackend runtimeProvisioningService,
                               PipelineRegistry pipelineRegistry,
                               ProviderTypeService providerTypeService,
                               ProviderService providerService,
                               PipelineServiceBackend pipelineService,
                               PipelineExecutionTaskManager pipelineExecutionTaskManager,
                               PipelineExecutionRegistry pipelineExecutionRegistry) {
        this.executablePipelineInstance = executablePipelineInstance;
        this.newPipelineStep = newPipelineStep;
        this.runtimeStatusChange = runtimeStatusChange;
        this.runtimeProvisioningService = runtimeProvisioningService;
        this.pipelineRegistry = pipelineRegistry;
        this.providerTypeService = providerTypeService;
        this.providerService = providerService;
        this.pipelineService = pipelineService;
        this.pipelineExecutionTaskManager = pipelineExecutionTaskManager;
        this.pipelineExecutionRegistry = pipelineExecutionRegistry;
    }

    @PostConstruct
    public void init( ) {
        List< org.guvnor.ala.pipeline.Pipeline > pipelines = pipelineRegistry.getPipelines( 0, 10, "name", true );
        providerTypeService.getAvialableProviderTypes( )
                .forEach( providerType -> internalPipelines.putIfAbsent( providerType, new HashSet<>( ) ) );
        pipelines.forEach( pipeline -> {
            ProviderType providerType = getSupportedProviderType( pipeline );
            if ( providerType != null ) {
                internalPipelines.get( providerType ).add( pipeline );
            }
        } );
    }

    private ProviderType getSupportedProviderType( org.guvnor.ala.pipeline.Pipeline pipeline ) {
        //TODO we need and abstraction or something to get the ProviderType for a given pipeline.
        return providerTypeService.getAvialableProviderTypes()
                .stream()
                .filter( providerType -> pipeline.getName().startsWith( providerType.getId() ) )
                .findFirst()
                .orElse( null );
    }

    @Override
    public Runtime getRuntime( final RuntimeKey runtimeKey ) {
        List< org.guvnor.ala.runtime.Runtime > runtimes = runtimeProvisioningService.getRuntimes(0,
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
    public Collection< Runtime > getRuntimes(final Provider provider) {

        Collection<Runtime> result = pipelineExecutionRegistry.getExecutionRecords().stream()
                .filter( record -> {
                    //TODO check this filtering
                    return record.getTask().getProviderId().getId().equals(provider.getId());
                } )
                .map( record -> convertToUIRuntime(record) )
                .collect(Collectors.toList());

        return result;
    }

    private Runtime convertToUIRuntime(PipelineExecutionRecord record) {

        Runtime result = new Runtime(new ProviderKey(new ProviderTypeKey(record.getTask().getProviderId().getProviderType().getProviderTypeName()),
                                                     record.getTask().getProviderId().getId(),
                                                     record.getTask().getProviderId().getId()),
                                     record.getTask().getRuntimeId().getId(),
                                     transformToRuntimeStatus(record.getTask().getPipelineStatus()),
                                     null,
                                     "endpoint",
                                     "started at");

        Pipeline pipeline = new Pipeline(record.getTask().getPipeline().getName(),
                                         result);

        record.getTask().getPipeline().getStages().stream()
                .forEach(stage -> pipeline.addStep(new Step(pipeline,
                                                            stage.getName(),
                                                            transformToStageStatus(record.getTask().getStageStatus(stage.getName())))));

        result.setPipeline(pipeline);

        return result;
    }

    private RuntimeStatus transformToRuntimeStatus( PipelineExecutionTask.Status status ) {
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

    private StageStatus transformToStageStatus(PipelineExecutionTask.Status status ) {
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

    private Runtime convertToUIRuntime(org.guvnor.ala.runtime.Runtime runtime,
                                       ProviderKey providerKey) {
        Runtime result = new Runtime(providerKey,
                                     runtime.getId(),
                                     convertToUIRuntimeStatus(runtime.getState().getState()),
                                     null,
                                     buildRuntimeEndpoint(runtime),
                                     runtime.getState() != null ? runtime.getState().getStartedAt() : "");

        Pipeline pipeline = new Pipeline(PipelineConstants.WILDFLY_PROVISIONING_PIPELINE, result);
        pipeline.addStep( new Step(pipeline, "Clone Repository", StageStatus.SCHEDULED));
        pipeline.addStep( new Step(pipeline, "Prepare Project", StageStatus.SCHEDULED));
        pipeline.addStep( new Step(pipeline, "Build Application", StageStatus.SCHEDULED));
        pipeline.addStep( new Step(pipeline, "Deploy Application", StageStatus.SCHEDULED));

        result.setPipeline(pipeline);
        return result;
    }

    private String buildRuntimeEndpoint(org.guvnor.ala.runtime.Runtime runtime) {
        StringBuilder url = new StringBuilder();
        if ( runtime.getEndpoint() != null ) {
            return "http://" + runtime.getEndpoint().getHost() +
                    ":" + runtime.getEndpoint().getPort() +
                    "/" + runtime.getEndpoint().getContext();
        } else {
            return "";
        }
    }

    private RuntimeStatus convertToUIRuntimeStatus( String status ) {
        //TODO check this conversion, because the status is apparently an arbitrary String in the guvnor-ala-service world
        return RuntimeStatus.STARTED;
    }

    @Override
    public Collection<String> getPipelines( final ProviderKey providerKey ) {
        return print( "getPipelines", getPipelines( providerKey.getProviderTypeKey() ) );
    }

    @Override
    public Collection< String > getPipelines( final ProviderTypeKey providerTypeKey ) {
        return internalPipelines.getOrDefault( providerTypeKey, new ArrayList<>( ) )
                .stream( )
                .map( org.guvnor.ala.pipeline.Pipeline::getName )
                .sorted(new Comparator< String >() {
                    @Override
                    public int compare(String o1,
                                       String o2) {
                        //force the WILDFLY_PROVISIONING_PIPELINE to the left ;)
                        return o1.equals(PipelineConstants.WILDFLY_PROVISIONING_PIPELINE) ? -1 : 1;
                    }
                })
                .collect( toList( ) );
    }


    public void createRuntime( ProviderKey providerKey, String runtimeId, Source source, String pipelineName ) {
        Provider provider = providerService.getProvider(providerKey);
        if ( provider == null ) {
            throw new RuntimeException("No provider was found for providerKey: " + providerKey);
        }

        org.guvnor.ala.pipeline.Pipeline pipeline =
                internalPipelines.getOrDefault( provider.getProviderTypeKey(), Collections.emptyList( ) )
                        .stream( )
                        .filter( p -> p.getName( ).equals( pipelineName ) )
                        .findFirst( ).orElse( null );
        if ( pipeline == null ) {
            throw new RuntimeException( "No pipeline was found for provider: " + provider + "and pipelineName: " + pipelineName );
        }

        //TODO review this ProviderId
        //BaseProviderType
        Input input = buildPipelineInput(provider, pipeline, runtimeId, source);

        //pipelineService.runPipeline( pipelineName, input );

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
                        return provider.getProviderTypeKey().getId();
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
            public ProviderId getProviderId() {
                return alaProviderId;
            }
        };
        PipelineExecutionTask task = new PipelineExecutionTask(pipeline, input, alaProviderId, alaRuntimeId );
        pipelineExecutionTaskManager.execute(task);
    }

    private Input buildPipelineInput( Provider provider, org.guvnor.ala.pipeline.Pipeline pipeline, String runtimeId, Source source ) {
        //TODO build the proper Input according with the provider and pipeline.

        Input input = new Input();
        input.put(WF10ProviderConfigParams.PROVIDER_NAME, provider.getId() );
        input.put(WF10ProviderConfigParams.MANAGEMENT_REALM, "ManagementRealm");
        putAsStrings( input, provider.getValues() );

        input.put("repo-name", ((InternalGitSource)source).getRepository() );
        input.put("branch", ((InternalGitSource)source).getBranch() );
        input.put("project-dir", ((InternalGitSource)source).getProject() );

        if ( ((InternalGitSource) source).getDataSourceInfo() != null ) {
            IDataSourceInfo dataSourceInfo = ((InternalGitSource) source).getDataSourceInfo();
            input.put("jndi-data-source", dataSourceInfo.getJndi( ) );
            if ( dataSourceInfo.isKieDataSource( ) ) {
                input.put( "kie-data-source", dataSourceInfo.getKieUuid( ) );
                input.put( "kie-data-source-deployment-id", dataSourceInfo.getDeploymentId( ) );
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


/*
    int cont = 0;
    public void createRuntimeOLD( ProviderKey provider, String runtimeId, Source source, String pipeline ) {
        cont++;
        final Runtime runtime;
        if ( cont % 3 == 0 ) {
            runtime = new Runtime( provider, runtimeId, RuntimeStatus.LOADING, source );
            final Pipeline _pipeline = getRunningPipeline( pipeline, runtime );
            runtime.setPipeline( _pipeline );
            runtimes.putIfAbsent( provider, new ArrayList<>() );
            runtimes.get( provider ).add( runtime );

            new Thread( () -> {
                try {
                    Thread.sleep( 2000 );
                } catch ( InterruptedException e ) {
                    e.printStackTrace();
                }

                final Step newDeploying = new Step( _pipeline, "Deploying" );
                _pipeline.addStep( newDeploying );
                newPipelineStep.fire( new NewPipelineStep( newDeploying ) );

                try {
                    Thread.sleep( 5000 );
                } catch ( InterruptedException e ) {
                    e.printStackTrace();
                }

                final Step newReady = new Step( _pipeline, "Ready" );
                _pipeline.addStep( newReady );
                newPipelineStep.fire( new NewPipelineStep( newReady ) );

                try {
                    Thread.sleep( 1000 );
                } catch ( InterruptedException e ) {
                    e.printStackTrace();
                }
                runtime.setStatus( RuntimeStatus.STARTED );
                runtime.setEndpoint( "http://10.0.0.1/my-app" + new Random().ints().findFirst().getAsInt() );
                runtimeStatusChange.fire( new RuntimeStatusChange( runtime ) );

                try {
                    Thread.sleep( 6000 );
                } catch ( InterruptedException e ) {
                    e.printStackTrace();
                }
                runtime.setStatus( RuntimeStatus.WARN );
                runtimeStatusChange.fire( new RuntimeStatusChange( runtime ) );
            } ).start();

        } else if ( cont % 2 == 0 ) {
            runtime = new Runtime( provider, runtimeId, RuntimeStatus.STARTED, source, "http://10.0.0.1/my-app", "date1");
            runtime.setPipeline( getDonePipeline( pipeline, runtime ) );
            runtimes.putIfAbsent( provider, new ArrayList<>() );
            runtimes.get( provider ).add( runtime );

            new Thread( () -> {
                try {
                    Thread.sleep( 8000 );
                } catch ( InterruptedException e ) {
                    e.printStackTrace();
                }
                runtime.setStatus( RuntimeStatus.STOPPED );
                runtimeStatusChange.fire( new RuntimeStatusChange( runtime ) );
            } ).start();

        } else {
            runtime = new Runtime( provider, runtimeId, RuntimeStatus.ERROR, source, "http://10.0.0.1/my-app", "date2" );
            runtime.setPipeline( getDonePipeline( pipeline, runtime ) );
            runtimes.putIfAbsent( provider, new ArrayList<>() );
            runtimes.get( provider ).add( runtime );

            new Thread( () -> {
                try {
                    Thread.sleep( 15000 );
                } catch ( InterruptedException e ) {
                    e.printStackTrace();
                }
                runtime.setStatus( RuntimeStatus.STARTED );
                runtimeStatusChange.fire( new RuntimeStatusChange( runtime ) );
            } ).start();

        }
    }

    private Pipeline getRunningPipeline( final String pipelineName,
                                         final Runtime runtime ) {

        final Pipeline pipeline = new Pipeline( pipelineName, runtime );

        pipeline.addStep( new Step( pipeline, "Deploy Trigger via UI" ) );
//        pipeline.addStep( new Step( pipeline, "Maven Build" ) );

        return pipeline;
    }
*/

/*
    private Pipeline getDonePipeline( final String pipelineName,
                                      final Runtime runtime ) {

        final Pipeline pipeline = new Pipeline( pipelineName, runtime );

        pipeline.addStep( new Step( pipeline, "Source" ) );
        pipeline.addStep( new Step( pipeline, "Maven Build" ) );
        pipeline.addStep( new Step( pipeline, "Deploy" ) );
        pipeline.addStep( new Step( pipeline, "Ready" ) );

        return pipeline;
    }

*/
    @Override
    public void start( RuntimeKey runtimeKey ) {

    }

    @Override
    public void stop( RuntimeKey runtimeKey ) {

    }

    @Override
    public void rebuild( RuntimeKey runtimeKey ) {

    }

    @Override
    public void delete( RuntimeKey runtimeKey ) {

    }

    public <T> Collection<T> print( final String ref,
                                    final Collection<T> input ) {

        System.out.println( ref + " [" + input.toString() + "]" );

        return input;
    }
}
