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

package org.guvnor.ala.ui.backend.service.wildfly;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.ala.ui.backend.service.handler.BackendProviderHandler;
import org.guvnor.ala.ui.backend.service.converter.ProviderConfigConverter;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.ProviderTypeKey;

@ApplicationScoped
public class WildflyProviderHandler
        implements BackendProviderHandler {

    public static final String WF_10_ICON = "images/wf.png";

    private WildflyProviderConfigConverter configConverter;

    public WildflyProviderHandler() {
        //Empty constructor for Weld proxying
    }

    @Inject
    public WildflyProviderHandler(WildflyProviderConfigConverter configConverter) {
        this.configConverter = configConverter;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public boolean acceptProviderType(ProviderTypeKey providerTypeKey) {
        return providerTypeKey != null && ProviderType.WILDFY_PROVIDER_TYPE.equals(providerTypeKey.getId());
    }

    @Override
    public ProviderConfigConverter getProviderConfigConverter(ProviderTypeKey providerTypeKey) {
        return configConverter;
    }

    @Override
    public String getProviderTypeIcon() {
        return WF_10_ICON;
    }
}
