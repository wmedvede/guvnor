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

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.guvnor.ala.build.maven.config.MavenProjectConfig;
import org.guvnor.ala.config.ProviderConfig;
import org.guvnor.ala.config.RuntimeConfig;
import org.guvnor.ala.openshift.config.OpenShiftParameters;
import org.guvnor.ala.pipeline.Input;
import org.guvnor.ala.runtime.Runtime;
import org.guvnor.ala.source.git.config.GitConfig;
import org.guvnor.ala.ui.model.InternalGitSource;
import org.guvnor.ala.ui.model.ProviderKey;
import org.guvnor.ala.ui.model.Source;

import static org.guvnor.ala.openshift.config.OpenShiftProperty.APPLICATION_NAME;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.KUBERNETES_AUTH_BASIC_PASSWORD;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.KUBERNETES_AUTH_BASIC_USERNAME;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.KUBERNETES_MASTER;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.KUBERNETES_NAMESPACE;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.RESOURCE_SECRETS_URI;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.RESOURCE_STREAMS_URI;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.RESOURCE_TEMPLATE_PARAM_VALUES;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.RESOURCE_TEMPLATE_URI;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.SERVICE_NAME;

/**
 * Helper class for building the pipeline input parameters given a runtime name, a provider and the sources.
 */
public class PipelineInputBuilder {

    private String runtimeName;

    private ProviderKey providerKey;

    private Source source;

    public static PipelineInputBuilder newInstance() {
        return new PipelineInputBuilder();
    }

    private PipelineInputBuilder() {
    }

    public PipelineInputBuilder withRuntimeName(final String runtimeName) {
        this.runtimeName = runtimeName;
        return this;
    }

    public PipelineInputBuilder withProvider(final ProviderKey providerKey) {
        this.providerKey = providerKey;
        return this;
    }

    public PipelineInputBuilder withSource(final Source source) {
        this.source = source;
        return this;
    }

    public Input build() {
        final Input input = new Input();

        if (runtimeName != null) {
            input.put(RuntimeConfig.RUNTIME_NAME,
                      runtimeName);
        }
        if (providerKey != null) {
            input.put(ProviderConfig.PROVIDER_NAME,
                      providerKey.getId());
        }
        if (source instanceof InternalGitSource) {
            input.put(GitConfig.REPO_NAME,
                      ((InternalGitSource) source).getRepository());
            input.put(GitConfig.BRANCH,
                      ((InternalGitSource) source).getBranch());
            if (((InternalGitSource) source).getProject() != null) {
                input.put(MavenProjectConfig.PROJECT_DIR,
                          ((InternalGitSource) source).getProject().getProjectName());
            }
        }

        hackOpenShiftParameters(input);
        return input;
    }

    private void hackOpenShiftParameters(Input input) {

        final String application = input.get(RuntimeConfig.RUNTIME_NAME);
        final String namespace = application;

        String templateParams = new OpenShiftParameters()
                .param("APPLICATION_NAME",
                       application)
                .param("IMAGE_STREAM_NAMESPACE",
                       namespace)
                .param("KIE_ADMIN_PWD",
                       "admin1!")
                .param("KIE_SERVER_PWD",
                       "execution1!")
                .toString();

        /*

        This parameters goes only when the pipeline also will create the provider
        in out case we are using an already existing provider.

        input.put(KUBERNETES_MASTER.inputKey(),
            "https://ce-os-rhel-master.usersys.redhat.com:8443");
        input.put(KUBERNETES_AUTH_BASIC_USERNAME.inputKey(),
            "admin");
        input.put(KUBERNETES_AUTH_BASIC_PASSWORD.inputKey(),
            "admin");

         */


            /* unnecessary for this test
            put(RESOURCE_TEMPLATE_NAME.inputKey(), "bpmsuite70-execserv");
            put(RESOURCE_TEMPLATE_PARAM_DELIMITER.inputKey(), ",");
            put(RESOURCE_TEMPLATE_PARAM_ASSIGNER.inputKey(), "=");
             */
            try {

                //This namespace here is for my experiments, David added the project name to the Runtime configuration
                input.put(KUBERNETES_NAMESPACE.inputKey(),
                          namespace);

                input.put(RESOURCE_SECRETS_URI.inputKey(),
                          getUri("bpmsuite-app-secret.json"));
                input.put(RESOURCE_STREAMS_URI.inputKey(),
                          getUri("jboss-image-streams.json"));
                input.put(RESOURCE_TEMPLATE_PARAM_VALUES.inputKey(),
                          templateParams);

                /*
                comentado para ver si la configuracion de valores por defecto va bien.
                input.put(RESOURCE_TEMPLATE_URI.inputKey(),
                          getUri("bpmsuite70-execserv.json"));
                */

                input.put(APPLICATION_NAME.inputKey(),
                          application);
                input.put(SERVICE_NAME.inputKey(),
                          application + "-execserv");
            } catch (Exception e) {
                System.out.println("INPUT BUILDING EXCEPTION: " + e.getMessage());
                e.printStackTrace();
            }
    }

    private String getUri(String resourcePath) throws URISyntaxException {
        if (!resourcePath.startsWith("/")) {
            resourcePath = "/" + resourcePath;
        }
        return getClass().getResource(resourcePath).toURI().toString();
    }
}
