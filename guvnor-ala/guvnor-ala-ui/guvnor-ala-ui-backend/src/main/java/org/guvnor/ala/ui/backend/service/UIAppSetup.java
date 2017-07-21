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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.ala.openshift.config.impl.OpenShiftProviderConfigImpl;
import org.guvnor.ala.openshift.model.OpenShiftProviderType;
import org.guvnor.ala.registry.PipelineRegistry;
import org.guvnor.ala.services.api.backend.PipelineServiceBackend;
import org.guvnor.ala.ui.model.ProviderConfiguration;
import org.guvnor.ala.ui.model.ProviderKey;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.ProviderTypeKey;
import org.guvnor.ala.ui.service.ProviderService;
import org.uberfire.commons.services.cdi.Startup;
import org.uberfire.commons.services.cdi.StartupType;

import static org.guvnor.ala.openshift.config.OpenShiftProperty.KUBERNETES_AUTH_BASIC_PASSWORD;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.KUBERNETES_AUTH_BASIC_USERNAME;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.KUBERNETES_MASTER;

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
        initOpenshiftProvider();
    }


    private void initOpenshiftProvider() {

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