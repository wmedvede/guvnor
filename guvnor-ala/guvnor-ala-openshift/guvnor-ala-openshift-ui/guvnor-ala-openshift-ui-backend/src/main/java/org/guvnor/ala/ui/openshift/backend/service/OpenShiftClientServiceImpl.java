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

package org.guvnor.ala.ui.openshift.backend.service;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.guvnor.ala.ui.openshift.model.ConfigRequest;
import org.guvnor.ala.ui.openshift.model.ConfigResponse;
import org.guvnor.ala.ui.openshift.model.DefaultSettings;
import org.guvnor.ala.ui.openshift.model.RawConfigResponse;
import org.guvnor.ala.ui.openshift.model.SourceType;
import org.guvnor.ala.ui.openshift.model.TemplateConfigResponse;
import org.guvnor.ala.ui.openshift.model.TemplateDescriptorModel;
import org.guvnor.ala.ui.openshift.model.TemplateParam;
import org.guvnor.ala.ui.openshift.service.OpenshiftClientService;
import org.jboss.errai.bus.server.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

@Service
@ApplicationScoped
public class OpenShiftClientServiceImpl
        implements OpenshiftClientService {

    private static final Logger logger = LoggerFactory.getLogger(OpenShiftClientServiceImpl.class);

    private IOService ioService;

    private static DefaultSettings defaultSettingsInstance;

    public OpenShiftClientServiceImpl() {
        //Empty constructor for Weld proxying
    }

    @Inject
    public OpenShiftClientServiceImpl(final @Named("ioStrategy") IOService ioService) {
        this.ioService = ioService;
    }

    @Override
    public DefaultSettings getDefaultSettings() {
        if (defaultSettingsInstance == null) {
            defaultSettingsInstance = new DefaultSettings();
            defaultSettingsInstance.setValue(DefaultSettings.DEFAULT_OPEN_SHIFT_TEMPLATE,
                                             System.getProperty(DefaultSettings.DEFAULT_OPEN_SHIFT_TEMPLATE));
            defaultSettingsInstance.setValue(DefaultSettings.DEFAULT_OPEN_SHIFT_IMAGE_STREAMS,
                                             System.getProperty(DefaultSettings.DEFAULT_OPEN_SHIFT_IMAGE_STREAMS));
            defaultSettingsInstance.setValue(DefaultSettings.DEFAULT_OPEN_SHIFT_SECRETS,
                                             System.getProperty(DefaultSettings.DEFAULT_OPEN_SHIFT_SECRETS));
            logger.debug("Openshift default settings were set to");
            logger.debug(DefaultSettings.DEFAULT_OPEN_SHIFT_TEMPLATE + " = " + defaultSettingsInstance.getValue(DefaultSettings.DEFAULT_OPEN_SHIFT_TEMPLATE));
            logger.debug(DefaultSettings.DEFAULT_OPEN_SHIFT_IMAGE_STREAMS + " = " + defaultSettingsInstance.getValue(DefaultSettings.DEFAULT_OPEN_SHIFT_IMAGE_STREAMS));
            logger.debug(DefaultSettings.DEFAULT_OPEN_SHIFT_SECRETS + " = " + defaultSettingsInstance.getValue(DefaultSettings.DEFAULT_OPEN_SHIFT_SECRETS));
        }
        return defaultSettingsInstance;
    }

    @Override
    public ConfigResponse getConfig(final ConfigRequest configRequest) {
        switch (configRequest.getConfigType()) {
            case TEMPLATE:
                return resolveTemplateConfigRequest(configRequest);
            case RAW:
                return resolveRawConfigRequest(configRequest);
        }
        throw new RuntimeException("Unknown configType: " + configRequest.getConfigType());
    }

    private TemplateConfigResponse resolveTemplateConfigRequest(final ConfigRequest request) {
        checkNotNull("request",
                     request);
        checkNotNullConfigRequest(request);

        if (request.getSourceType() == SourceType.PATH) {
            final Path path = (Path) request.getSource();
            if (!path.getFileName().endsWith(".json")) {
                throw new RuntimeException("Invalid descriptor file: " + path.getFileName());
            }

            final String content = ioService.readAllString(Paths.convert(path));
            List<TemplateParam> params = mockParams();

            final TemplateDescriptorModel descriptorModel = new TemplateDescriptorModel(params);
            return new TemplateConfigResponse(request.getSourceType(),
                                              content,
                                              descriptorModel);
        } else {

            //TODO get the content from the URL
            List<TemplateParam> params = mockParams();

            final TemplateDescriptorModel descriptorModel = new TemplateDescriptorModel(params);
            return new TemplateConfigResponse(request.getSourceType(),
                                              null,
                                              descriptorModel);
        }
    }

    private RawConfigResponse resolveRawConfigRequest(final ConfigRequest request) {
        if (request.getSourceType() == SourceType.PATH) {
            final Path path = (Path) request.getSource();
            if (!path.getFileName().endsWith(".json")) {
                throw new RuntimeException("Invalid descriptor file: " + path.getFileName());
            }

            final String content = ioService.readAllString(Paths.convert(path));
            return new RawConfigResponse(request.getSourceType(),
                                         content);
        } else {
            throw new RuntimeException("Request of source type = " + request.getSourceType() + " is not yet implemented.");
        }
    }

    private void checkNotNullConfigRequest(final ConfigRequest request) {
        checkNotNull("sourceType",
                     request.getSourceType());
        checkNotNull("source",
                     request.getSource());
        checkNotNull("configType",
                     request.getConfigType());
    }

    private List<TemplateParam> mockParams() {
        List<TemplateParam> params = new ArrayList<>();

        params.add(new TemplateParam("APPLICATION_NAME",
                                     true,
                                     null,
                                     null));

        params.add(new TemplateParam("IMAGE_STREAM_NAMESPACE",
                                     true,
                                     null,
                                     null));

        params.add(new TemplateParam("KIE_ADMIN_USER",
                                     true,
                                     null,
                                     null));

        params.add(new TemplateParam("KIE_ADMIN_PWD",
                                     true,
                                     null,
                                     null));

        params.add(new TemplateParam("KIE_SERVER_CONTROLLER_USER",
                                     false,
                                     null,
                                     null));

        params.add(new TemplateParam("KIE_SERVER_CONTROLLER_PWD",
                                     false,
                                     null,
                                     null));

        params.add(new TemplateParam("KIE_SERVER_USER",
                                     false,
                                     null,
                                     null));

        params.add(new TemplateParam("KIE_SERVER_PWD",
                                     false,
                                     null,
                                     null));

        params.add(new TemplateParam("MAVEN_REPO_URL",
                                     false,
                                     null,
                                     null));

        params.add(new TemplateParam("MAVEN_REPO_USERNAME",
                                     false,
                                     null,
                                     null));

        params.add(new TemplateParam("MAVEN_REPO_PASSWORD",
                                     false,
                                     null,
                                     null));

        params.add(new TemplateParam("KIE_SERVER_PERSISTENCE_DS",
                                     false,
                                     null,
                                     null));

        params.add(new TemplateParam("DB_JNDI",
                                     false,
                                     null,
                                     null));

        params.add(new TemplateParam("DB_DATABASE",
                                     false,
                                     null,
                                     null));

        params.add(new TemplateParam("DB_USERNAME",
                                     false,
                                     null,
                                     null));

        params.add(new TemplateParam("DB_PASSWORD",
                                     false,
                                     null,
                                     null));

        params.add(new TemplateParam("EXECUTION_SERVER_HOSTNAME_HTTP",
                                     false,
                                     null,
                                     null));

        params.add(new TemplateParam("EXECUTION_SERVER_HOSTNAME_HTTPS",
                                     false,
                                     null,
                                     null));

        params.add(new TemplateParam("HTTPS_KEYSTORE",
                                     false,
                                     null,
                                     null));

        params.add(new TemplateParam("HTTPS_NAME",
                                     false,
                                     null,
                                     null));

        params.add(new TemplateParam("HTTPS_PASSWORD",
                                     false,
                                     null,
                                     null));

        params.add(new TemplateParam("HTTPS_SECRET",
                                     false,
                                     null,
                                     "bpmsuite-app-secret"));
        return params;
    }
}
