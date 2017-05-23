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

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

import org.guvnor.ala.ui.backend.service.converter.ProviderConfigConverter;
import org.guvnor.ala.ui.model.ProviderConfiguration;
import org.guvnor.ala.ui.model.WF10ProviderConfigParams;
import org.guvnor.ala.wildfly.config.WildflyProviderConfig;
import org.guvnor.ala.wildfly.config.impl.WildflyProviderConfigImpl;

import static org.guvnor.ala.ui.backend.service.util.ServiceUtil.getStringValue;

@ApplicationScoped
public class WildflyProviderConfigConverter
        implements ProviderConfigConverter<ProviderConfiguration, WildflyProviderConfig> {

    public WildflyProviderConfigConverter() {
        //Empty constructor for Weld proxying
    }

    @Override
    public Class<ProviderConfiguration> getModelType() {
        return ProviderConfiguration.class;
    }

    @Override
    public Class<WildflyProviderConfig> getDomainType() {
        return WildflyProviderConfig.class;
    }

    @Override
    public WildflyProviderConfig toDomain(ProviderConfiguration modelValue) {
        if (modelValue == null) {
            return null;
        }

        return new WildflyProviderConfigImpl(modelValue.getName(),
                                             getStringValue(modelValue.getValues(),
                                                            WF10ProviderConfigParams.HOST),
                                             getStringValue(modelValue.getValues(),
                                                            WF10ProviderConfigParams.PORT),
                                             getStringValue(modelValue.getValues(),
                                                            WF10ProviderConfigParams.MANAGEMENT_PORT),
                                             getStringValue(modelValue.getValues(),
                                                            WF10ProviderConfigParams.USER),
                                             getStringValue(modelValue.getValues(),
                                                            WF10ProviderConfigParams.PASSWORD));
    }

    @Override
    public ProviderConfiguration toModel(WildflyProviderConfig domainValue) {
        if (domainValue == null) {
            return null;
        }

        final Map<String, String> values = new HashMap<>();
        values.put(WF10ProviderConfigParams.PROVIDER_NAME,
                   domainValue.getName());
        values.put(WF10ProviderConfigParams.HOST,
                   domainValue.getHostIp());
        values.put(WF10ProviderConfigParams.PORT,
                   domainValue.getPort());
        values.put(WF10ProviderConfigParams.MANAGEMENT_PORT,
                   domainValue.getManagementPort());
        values.put(WF10ProviderConfigParams.USER,
                   domainValue.getUser());
        values.put(WF10ProviderConfigParams.PASSWORD,
                   domainValue.getPassword());
        return new ProviderConfiguration(domainValue.getName(),
                                         domainValue.getName(),
                                         values);
    }
}
