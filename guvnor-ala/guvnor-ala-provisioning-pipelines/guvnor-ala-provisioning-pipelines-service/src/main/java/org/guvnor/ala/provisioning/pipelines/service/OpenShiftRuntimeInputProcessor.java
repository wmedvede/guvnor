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

package org.guvnor.ala.provisioning.pipelines.service;

import java.net.URISyntaxException;
import java.util.Properties;

import org.guvnor.ala.openshift.config.OpenShiftParameters;
import org.guvnor.ala.openshift.config.OpenShiftProperty;
import org.guvnor.ala.pipeline.Input;
import org.guvnor.ala.pipeline.InputProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.guvnor.ala.config.RuntimeConfig.RUNTIME_NAME;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.APPLICATION_NAME;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.PROJECT_NAME;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.RESOURCE_SECRETS_URI;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.RESOURCE_STREAMS_URI;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.RESOURCE_TEMPLATE_PARAM_VALUES;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.RESOURCE_TEMPLATE_URI;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.SERVICE_NAME;

public class OpenShiftRuntimeInputProcessor
        implements InputProcessor {

    enum TemplateParam {
        APPLICATION_NAME,
        IMAGE_STREAM_NAMESPACE,
        KIE_ADMIN_USER,
        KIE_ADMIN_PWD,
        KIE_SERVER_USER,
        KIE_SERVER_PWD
    }

    private static final Logger logger = LoggerFactory.getLogger(OpenShiftRuntimeInputProcessor.class);

    private static final String DEFAULT_RESOURCE_SECRETS = "bpmsuite-app-secret.json";

    private static final String DEFAULT_RESOURCE_STREAMS = "jboss-image-streams.json";

    private static final String DEFAULT_RESOURCE_TEMPLATE = "bpmsuite70-execserv.json";

    @Override
    public Input processInput(Input input) {
        final Properties settings = OpenShiftRuntimeSettings.getInstance().getProperties();

        final String runtimeName = input.get(RUNTIME_NAME);

        String projectName;
        if (!input.containsKey(PROJECT_NAME.inputKey())) {
            projectName = runtimeName;
            input.put(PROJECT_NAME.inputKey(),
                      projectName);
        } else {
            projectName = input.get(PROJECT_NAME.inputKey());
        }

        String applicationName;
        if (!input.containsKey(APPLICATION_NAME.inputKey())) {
            applicationName = runtimeName;
            input.put(APPLICATION_NAME.inputKey(),
                      applicationName);
        } else {
            applicationName = input.get(APPLICATION_NAME.inputKey());
        }

        if (!input.containsKey(SERVICE_NAME.inputKey())) {
            input.put(SERVICE_NAME.inputKey(),
                      applicationName + "-execserv");
        }

        if (!input.containsKey(RESOURCE_SECRETS_URI.inputKey())) {
            setURIParameter(input,
                            RESOURCE_SECRETS_URI,
                            settings,
                            DEFAULT_RESOURCE_SECRETS);
        }

        if (!input.containsKey(RESOURCE_STREAMS_URI.inputKey())) {
            setURIParameter(input,
                            RESOURCE_STREAMS_URI,
                            settings,
                            DEFAULT_RESOURCE_STREAMS);
        }

        if (!input.containsKey(RESOURCE_TEMPLATE_URI.inputKey())) {
            setURIParameter(input,
                            RESOURCE_TEMPLATE_URI,
                            settings,
                            DEFAULT_RESOURCE_TEMPLATE);
        }

        if (!input.containsKey(RESOURCE_TEMPLATE_PARAM_VALUES.inputKey())) {

            OpenShiftParameters params = new OpenShiftParameters();

            setOpenShiftParam(input,
                              params,
                              TemplateParam.APPLICATION_NAME.name(),
                              applicationName);

            setOpenShiftParam(input,
                              params,
                              TemplateParam.IMAGE_STREAM_NAMESPACE.name(),
                              projectName);

            setOpenShiftParam(input,
                              params,
                              TemplateParam.KIE_ADMIN_USER.name(),
                              getManagedProperty(settings,
                                                 TemplateParam.KIE_ADMIN_USER.name(),
                                                 null));
            setOpenShiftParam(input,
                              params,
                              TemplateParam.KIE_ADMIN_PWD.name(),
                              getManagedProperty(settings,
                                                 TemplateParam.KIE_ADMIN_PWD.name(),
                                                 null));

            setOpenShiftParam(input,
                              params,
                              TemplateParam.KIE_SERVER_USER.name(),
                              getManagedProperty(settings,
                                                 TemplateParam.KIE_SERVER_USER.name(),
                                                 null));

            setOpenShiftParam(input,
                              params,
                              TemplateParam.KIE_SERVER_PWD.name(),
                              getManagedProperty(settings,
                                                 TemplateParam.KIE_SERVER_USER.name(),
                                                 null));

            String templateParams = params.toString();
            if (templateParams != null && !templateParams.isEmpty()) {
                input.put(RESOURCE_TEMPLATE_PARAM_VALUES.inputKey(),
                          templateParams);
            }
        }

        return input;
    }

    private String setOpenShiftParam(Input input,
                                     OpenShiftParameters parameters,
                                     String paramName,
                                     String defaultValue) {
        String value = input.get(paramName);
        if (value == null) {
            value = defaultValue;
        }
        if (value != null) {
            parameters.param(paramName,
                             value);
        }
        return value;
    }

    public void setURIParameter(Input input,
                                OpenShiftProperty openShiftProperty,
                                Properties properties,
                                String defaultFileName) {
        String fileName = null;
        try {
            fileName = getManagedProperty(properties,
                                          openShiftProperty.propertyKey(),
                                          defaultFileName);
            String fileURI = getUri(fileName);
            input.put(openShiftProperty.inputKey(),
                      fileURI);
        } catch (Exception e) {
            logger.warn("It was not possible to build the URI parameter for the following file: " + fileName);
        }
    }

    private String getUri(String resourcePath) throws URISyntaxException {
        if (!resourcePath.startsWith("/")) {
            resourcePath = "/" + resourcePath;
        }
        return getClass().getResource(resourcePath).toURI().toString();
    }

    public static String getManagedProperty(Properties properties,
                                            String propertyName,
                                            String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (isEmpty(propertyValue)) {
            propertyValue = properties.getProperty(propertyName);
        }
        return propertyValue != null ? propertyValue.trim() : defaultValue;
    }

    public static boolean isEmpty(final String value) {
        return value == null || value.trim().length() == 0;
    }
}
