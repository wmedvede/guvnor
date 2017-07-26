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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.ala.build.maven.config.MavenBuildConfig;
import org.guvnor.ala.build.maven.config.MavenBuildExecConfig;
import org.guvnor.ala.build.maven.config.MavenProjectConfig;
import org.guvnor.ala.config.BinaryConfig;
import org.guvnor.ala.config.BuildConfig;
import org.guvnor.ala.config.Config;
import org.guvnor.ala.config.ProjectConfig;
import org.guvnor.ala.config.ProviderConfig;
import org.guvnor.ala.config.RuntimeConfig;
import org.guvnor.ala.config.SourceConfig;
import org.guvnor.ala.openshift.config.OpenShiftProviderConfig;
import org.guvnor.ala.openshift.config.impl.ContextAwareOpenShiftRuntimeExecConfig;
import org.guvnor.ala.openshift.config.impl.OpenShiftProviderConfigImpl;
import org.guvnor.ala.openshift.model.OpenShiftProviderType;
import org.guvnor.ala.pipeline.Input;
import org.guvnor.ala.pipeline.Pipeline;
import org.guvnor.ala.pipeline.PipelineFactory;
import org.guvnor.ala.pipeline.Stage;
import org.guvnor.ala.registry.PipelineRegistry;
import org.guvnor.ala.services.api.backend.PipelineConfigImpl;
import org.guvnor.ala.services.api.backend.PipelineServiceBackend;
import org.guvnor.ala.source.git.config.GitConfig;
import org.guvnor.ala.ui.model.ProviderConfiguration;
import org.guvnor.ala.ui.model.ProviderKey;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.ProviderTypeKey;
import org.guvnor.ala.ui.service.ProviderService;
import org.guvnor.ala.wildfly.config.WildflyProviderConfig;
import org.guvnor.ala.wildfly.config.impl.ContextAwareWildflyRuntimeExecConfig;
import org.guvnor.ala.wildfly.model.WildflyProviderType;
import org.uberfire.commons.services.cdi.Startup;
import org.uberfire.commons.services.cdi.StartupType;

import static org.guvnor.ala.openshift.config.OpenShiftProperty.KUBERNETES_AUTH_BASIC_PASSWORD;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.KUBERNETES_AUTH_BASIC_USERNAME;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.KUBERNETES_MASTER;
import static org.guvnor.ala.pipeline.StageUtil.config;

/**
 * TODO, remove this auxiliary component.
 * Auxiliary component for having some pre initialized pipelines for development purposes.
 */
@ApplicationScoped
@Startup(StartupType.BOOTSTRAP)
public class UIAppSetup {

    private PipelineRegistry pipelineRegistry;

    private ProviderService providerService;

    PipelineServiceBackend pipelineServiceBackend;

    public UIAppSetup() {
        //Empty constructor for Weld proxying
    }

    @Inject
    public UIAppSetup(PipelineRegistry pipelineRegistry,
                      ProviderService providerService,
                      PipelineServiceBackend pipelineServiceBackend) {
        this.pipelineRegistry = pipelineRegistry;
        this.providerService = providerService;
        this.pipelineServiceBackend = pipelineServiceBackend;
    }

    @PostConstruct
    private void init() {
        initPipelines();
        initOpenshiftProvider();
        initOpenshiftPipeline();
    }

    protected void initPipelines() {

        // Create Wildfly Pipeline Configuration
        final GitConfig gitConfig = new GitConfig() {
            @Override
            public String toString() {
                return "GitConfig";
            }
        };

        final Stage<Input, SourceConfig> sourceConfigStage = config("Git Source",
                                                                    f -> gitConfig);

        final MavenProjectConfig projectConfig = new MavenProjectConfig() {
            @Override
            public String toString() {
                return "MavenProjectConfig";
            }
        };
        final Stage<SourceConfig, ProjectConfig> projectConfigStage = config("Maven Project",
                                                                             f -> projectConfig);

        final MavenBuildConfig mavenBuildConfig = new MavenBuildConfig() {
            @Override
            public String toString() {
                return "MavenBuildConfig";
            }

            @Override
            public List<String> getGoals() {
                final List<String> result = new ArrayList<>();
                result.add("clean");
                result.add("package");
                return result;
            }

            @Override
            public Properties getProperties() {
                final Properties result = new Properties();
                result.setProperty("failIfNoTests",
                                   "false");
                return result;
            }
        };
        final Stage<ProjectConfig, BuildConfig> buildConfigStage = config("Maven Build Config",
                                                                          f -> mavenBuildConfig);

        final MavenBuildExecConfig mavenBuildExecConfig = new MavenBuildExecConfig() {
            @Override
            public String toString() {
                return "MavenBuildExecConfig";
            }
        };
        final Stage<BuildConfig, BinaryConfig> buildExecStage = config("Maven Build",
                                                                       f -> mavenBuildExecConfig);

        final WildflyProviderConfig wildflyProviderConfig = new WildflyProviderConfig() {
            @Override
            public String toString() {
                return "WildflyProviderConfig";
            }
        };

        final Stage<BinaryConfig, ProviderConfig> providerConfigStage = config("Wildfly Provider Config",
                                                                               f -> wildflyProviderConfig);

        final ContextAwareWildflyRuntimeExecConfig wildflyRuntimeExecConfig = new ContextAwareWildflyRuntimeExecConfig() {
            @Override
            public String toString() {
                return "WildflyRuntimeExecConfig";
            }
        };

        final Stage<ProviderConfig, RuntimeConfig> runtimeExecStage = config("Wildfly Runtime Exec",
                                                                             f -> wildflyRuntimeExecConfig);

        final Pipeline pipeline1 = PipelineFactory
                .startFrom(sourceConfigStage)
                .andThen(projectConfigStage)
                .andThen(buildConfigStage)
                .andThen(buildExecStage)
                .andThen(providerConfigStage)
                .andThen(runtimeExecStage).buildAs("pipeline from stages");

        final List<Config> configs = new ArrayList<>();
        configs.add(gitConfig);
        configs.add(projectConfig);
        configs.add(mavenBuildConfig);
        configs.add(mavenBuildExecConfig);
        configs.add(wildflyProviderConfig);
        configs.add(wildflyRuntimeExecConfig);

        final PipelineConfigImpl pipelineConfig = new PipelineConfigImpl("pipeline from configs",
                                                                         configs);
        final Pipeline pipeline2 = PipelineFactory.startFrom(null).build(pipelineConfig);

        pipelineRegistry.registerPipeline(pipeline1,
                                          WildflyProviderType.instance());
        pipelineRegistry.registerPipeline(pipeline2,
                                          WildflyProviderType.instance());
    }

    private void initOpenshiftPipeline() {
        final Stage<Input, ProviderConfig> providerConfig =
                config("OpenShift Provider Config",
                       (s) -> new OpenShiftProviderConfig() {
                       });

        final Stage<ProviderConfig, RuntimeConfig> runtimeExec =
                config("OpenShift Runtime Config",
                       (s) -> new ContextAwareOpenShiftRuntimeExecConfig());

        final Pipeline pipe = PipelineFactory
                .startFrom(providerConfig)
                .andThen(runtimeExec)
                .buildAs("openshift pipeline");

        pipelineRegistry.registerPipeline(pipe,
                                          OpenShiftProviderType.instance());
    }

    private void initOpenshiftProvider() {

        String namespace = createNamespace();

        OpenShiftProviderConfigImpl providerConfig = new OpenShiftProviderConfigImpl();
        //WE MUST clear first.
        providerConfig.clear();

        providerConfig.setName("openshift-provider-test");
        providerConfig.setKubernetesMaster("https://ce-os-rhel-master.usersys.redhat.com:8443");
        providerConfig.setKubernetesAuthBasicUsername("admin");
        providerConfig.setKubernetesAuthBasicPassword("admin");

        //providerConfig.setKubernetesNamespace(namespace);

        final Map<String, Object> values = new HashMap<>();
        values.put(KUBERNETES_MASTER.inputKey(),
                   "https://ce-os-rhel-master.usersys.redhat.com:8443");

        /*
        values.put(KUBERNETES_NAMESPACE.inputKey(),
                   namespace);
        */

        values.put(KUBERNETES_AUTH_BASIC_USERNAME.inputKey(),
                   "admin");
        values.put(KUBERNETES_AUTH_BASIC_PASSWORD.inputKey(),
                   "admin");

        ProviderConfiguration providerConfiguration = new ProviderConfiguration("openshift-provider-test",
                                                                                values);

        ProviderTypeKey providerTypeKey = new ProviderTypeKey(OpenShiftProviderType.instance().getProviderTypeName(),
                            OpenShiftProviderType.instance().getVersion());
        ProviderKey providerKey = new ProviderKey(providerTypeKey,
                                                  "openshift-provider-test");

        if (providerService.getProvider(providerKey) == null) {
            providerService.createProvider(new ProviderType(new ProviderTypeKey(OpenShiftProviderType.instance().getProviderTypeName(),
                                                                                OpenShiftProviderType.instance().getVersion()),
                                                            "OpenShift"),
                                           providerConfiguration);
        }

        /*
        Taken from OpenShiftRuntimeExecutorTest
        put(KUBERNETES_MASTER.inputKey(), "https://ce-os-rhel-master.usersys.redhat.com:8443");
        put(KUBERNETES_AUTH_BASIC_USERNAME.inputKey(), "admin");
        put(KUBERNETES_AUTH_BASIC_PASSWORD.inputKey(), "admin");
            / unnecessary for this test
            put(RESOURCE_TEMPLATE_NAME.inputKey(), "bpmsuite70-execserv");
            put(RESOURCE_TEMPLATE_PARAM_DELIMITER.inputKey(), ",");
            put(RESOURCE_TEMPLATE_PARAM_ASSIGNER.inputKey(), "=");
             /
        put(KUBERNETES_NAMESPACE.inputKey(), namespace);
        */
    }

    private String createNamespace() {
        return new StringBuilder()
                .append("guvnor-ala-")
                .append(System.getProperty("user.name",
                                           "anonymous").replaceAll("[^A-Za-z0-9]",
                                                                   "-"))
                .append("-test-")
                .append(new SimpleDateFormat("YYYYMMddHHmmss").format(new Date()))
                .toString();
    }
}