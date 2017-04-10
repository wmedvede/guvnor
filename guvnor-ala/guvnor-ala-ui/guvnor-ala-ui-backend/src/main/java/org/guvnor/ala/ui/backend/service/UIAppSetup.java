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
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.ala.build.maven.config.MavenBuildConfig;
import org.guvnor.ala.build.maven.config.MavenBuildExecConfig;
import org.guvnor.ala.build.maven.config.MavenProjectConfig;
import org.guvnor.ala.config.BinaryConfig;
import org.guvnor.ala.config.BuildConfig;
import org.guvnor.ala.config.ProjectConfig;
import org.guvnor.ala.config.ProviderConfig;
import org.guvnor.ala.config.RuntimeConfig;
import org.guvnor.ala.config.SourceConfig;
import org.guvnor.ala.pipeline.Input;
import org.guvnor.ala.pipeline.Pipeline;
import org.guvnor.ala.pipeline.PipelineFactory;
import org.guvnor.ala.pipeline.Stage;
import org.guvnor.ala.registry.PipelineRegistry;
import org.guvnor.ala.source.git.config.GitConfig;
import org.guvnor.ala.wildfly.config.WildflyProviderConfig;
import org.guvnor.ala.wildfly.config.impl.ContextAwareWildflyRuntimeExecConfig;
import org.uberfire.commons.services.cdi.Startup;
import org.uberfire.commons.services.cdi.StartupType;

import static org.guvnor.ala.pipeline.StageUtil.*;

/**
 * Temporal component for the pipelines initialization.
 */
@ApplicationScoped
@Startup( StartupType.BOOTSTRAP )
public class UIAppSetup {

    private PipelineRegistry pipelineRegistry;

    public UIAppSetup( ) {
        //Empty constructor for Weld proxying
    }

    @Inject
    public UIAppSetup( PipelineRegistry pipelineRegistry ) {
        this.pipelineRegistry = pipelineRegistry;
    }

    @PostConstruct
    private void init() {
        initPipelines();
    }

    protected void initPipelines(){

        // Create Wildfly Pipeline Configuration
        final Stage< Input, SourceConfig > sourceConfig = config( "Git Source", gitConfig -> new GitConfig( ) {
        } );

        final Stage< SourceConfig, ProjectConfig > projectConfig = config( "Maven Project", mavenProjectConfig -> new MavenProjectConfig( ) {
        } );

        final Stage< ProjectConfig, BuildConfig > buildConfig = config( "Maven Build Config", mavenBuildConfig ->
                new MavenBuildConfig( ) {
                    @Override
                    public List< String > getGoals( ) {
                        final List< String > result = new ArrayList<>( );
                        result.add( "clean" );
                        result.add( "package" );
                        return result;
                    }

                    @Override
                    public Properties getProperties( ) {
                        final Properties result = new Properties( );
                        result.setProperty( "failIfNoTests", "false" );
                        return result;
                    }
                } );

        final Stage< BuildConfig, BinaryConfig > buildExec = config( "Maven Build", mavenBuildExecConfig ->
                new MavenBuildExecConfig( ) {
                } );

        final Stage< BinaryConfig, ProviderConfig > providerConfig = config( "Wildfly Provider Config", wildflyProviderConfig ->
                new WildflyProviderConfig( ) {
                    @Override
                    public String getName( ) {
                        return "${input.provider-name}";
                    }
                } );

        final Stage<ProviderConfig, RuntimeConfig> runtimeExec = config( "Wildfly Runtime Exec", wildflyRuntimeExecConfig ->
                new ContextAwareWildflyRuntimeExecConfig()
        );

        final Pipeline wildflyPipeline = PipelineFactory
                .startFrom( sourceConfig )
                .andThen( projectConfig )
                .andThen( buildConfig )
                .andThen( buildExec )
                .andThen( providerConfig )
                .andThen( runtimeExec ).buildAs( "wildfly provisioning pipeline" );
        //Registering the Wildfly Pipeline to be available to the whole workbench
        pipelineRegistry.registerPipeline(wildflyPipeline);
    }

}