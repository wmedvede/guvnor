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

package org.guvnor.ala.openshift.config.impl;

import java.net.URISyntaxException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.guvnor.ala.openshift.config.OpenShiftParameters;
import org.guvnor.ala.pipeline.Input;
import org.guvnor.ala.runtime.providers.ProviderId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.guvnor.ala.openshift.config.OpenShiftProperty.APPLICATION_NAME;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.RESOURCE_SECRETS_URI;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.RESOURCE_STREAMS_URI;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.RESOURCE_TEMPLATE_PARAM_VALUES;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.RESOURCE_TEMPLATE_URI;
import static org.guvnor.ala.openshift.config.OpenShiftProperty.SERVICE_NAME;

public class KieServerContextAwareOpenShiftRuntimeExecConfig
        extends ContextAwareOpenShiftRuntimeExecConfig {

    @JsonIgnore
    private static final Logger logger = LoggerFactory.getLogger(KieServerContextAwareOpenShiftRuntimeExecConfig.class);

    public KieServerContextAwareOpenShiftRuntimeExecConfig() {
        super();
    }

    public KieServerContextAwareOpenShiftRuntimeExecConfig(String runtimeName,
                                                           ProviderId providerId,
                                                           String applicationName,
                                                           String resourceSecretsUri,
                                                           String resourceStreamsUri,
                                                           String resourceTemplateName,
                                                           String resourceTemplateParamDelimiter,
                                                           String resourceTemplateParamAssigner,
                                                           String resourceTemplateParamValues,
                                                           String resourceTemplateUri,
                                                           String serviceName) {
        super(runtimeName,
              providerId,
              applicationName,
              resourceSecretsUri,
              resourceStreamsUri,
              resourceTemplateName,
              resourceTemplateParamDelimiter,
              resourceTemplateParamAssigner,
              resourceTemplateParamValues,
              resourceTemplateUri,
              serviceName);
    }

    @Override
    public void setContext(Map<String, ?> context) {
        super.setContext(context);

        Input input = (Input) context.get("input");
        if (input != null) {

            try {
                if (!input.containsKey(RESOURCE_SECRETS_URI.inputKey())) {
                    setResourceSecretsUri(getUri("bpmsuite-app-secret.json"));
                }
            } catch (Exception e) {
                logger.warn("It was not possible to get the " + RESOURCE_SECRETS_URI.inputKey());
            }

            try {
                if (!input.containsKey(RESOURCE_STREAMS_URI.inputKey())) {
                    setResourceStreamsUri(getUri("jboss-image-streams.json"));
                }
            } catch (Exception e) {
                logger.warn("It was not possible to get the " + RESOURCE_STREAMS_URI.inputKey());
            }

            try {
                if (!input.containsKey(RESOURCE_TEMPLATE_URI.inputKey())) {
                    setResourceTemplateUri(getUri("bpmsuite70-execserv.json"));
                }
            } catch (Exception e) {
                logger.warn("It was not possible to get the " + RESOURCE_TEMPLATE_URI.inputKey());
            }

            if (!input.containsKey(APPLICATION_NAME.inputKey())) {
                setApplicationName(getRuntimeName());
            }

            if (!input.containsKey(SERVICE_NAME.inputKey())) {
                setServiceName(getRuntimeName() + "-execserv");
            }

            if (!input.containsKey(RESOURCE_TEMPLATE_PARAM_VALUES.inputKey())) {
                String templateParams = new OpenShiftParameters()
                        .param("APPLICATION_NAME",
                               getApplicationName())
                        .param("IMAGE_STREAM_NAMESPACE",
                               getApplicationName())
                        .param("KIE_ADMIN_PWD",
                               "admin1!")
                        .param("KIE_SERVER_PWD",
                               "execution1!")
                        .toString();
                setResourceTemplateParamValues(templateParams);
            }
        }
    }

    private String getUri(String resourcePath) throws URISyntaxException {
        if (!resourcePath.startsWith("/")) {
            resourcePath = "/" + resourcePath;
        }
        return getClass().getResource(resourcePath).toURI().toString();
    }
}