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

package org.guvnor.ala.wildfly.service;

import java.util.Objects;
import javax.inject.Inject;

import org.guvnor.ala.config.ProviderConfig;
import org.guvnor.ala.exceptions.ProviderOperationException;
import org.guvnor.ala.registry.RuntimeRegistry;
import org.guvnor.ala.runtime.providers.ProviderId;
import org.guvnor.ala.runtime.providers.ProviderManager;
import org.guvnor.ala.runtime.providers.TestConnectionResult;
import org.guvnor.ala.wildfly.access.WildflyClient;
import org.guvnor.ala.wildfly.access.exceptions.WildflyClientException;
import org.guvnor.ala.wildfly.config.WildflyProviderConfig;
import org.guvnor.ala.wildfly.model.WildflyProvider;

public class WildlfyProviderManager
        implements ProviderManager {

    private RuntimeRegistry runtimeRegistry;

    public WildlfyProviderManager() {
        //Empty constructor for Weld proxying
    }

    @Inject
    public WildlfyProviderManager(final RuntimeRegistry runtimeRegistry) {
        this.runtimeRegistry = runtimeRegistry;
    }

    @Override
    public boolean supports(final ProviderId providerId) {
        return runtimeRegistry.getProvider(providerId.getId()) != null &&
                runtimeRegistry.getProvider(providerId.getId()) instanceof WildflyProvider;
    }

    @Override
    public boolean supports(final ProviderConfig providerConfig) {
        return providerConfig instanceof WildflyProviderConfig;
    }

    @Override
    public TestConnectionResult testConnection(final ProviderConfig providerConfig) throws ProviderOperationException {
        try {
            String result = newClient((WildflyProviderConfig) providerConfig).testConnection();
            return new TestConnectionResult(true,
                                            result);
        } catch (WildflyClientException e) {
            return new TestConnectionResult(false,
                                            e.getMessage());
        }
    }

    @Override
    public TestConnectionResult testConnection(final ProviderId providerId) throws ProviderOperationException {
        return testConnection(((WildflyProvider) runtimeRegistry.getProvider(providerId.getId())).getConfig());
    }

    private WildflyClient newClient(final WildflyProviderConfig providerConfig) {
        return new WildflyClient(
                providerConfig.getName(),
                providerConfig.getUser(),
                providerConfig.getPassword(),
                providerConfig.getHost(),
                Integer.valueOf(Objects.toString(providerConfig.getPort(),
                                                 "8080")),
                Integer.valueOf(Objects.toString(providerConfig.getManagementPort(),
                                                 "9990"))
        );
    }
}