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

package org.guvnor.ala.services.rest.factories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.guvnor.ala.config.ProviderConfig;
import org.guvnor.ala.exceptions.ProviderOperationException;
import org.guvnor.ala.runtime.providers.ProviderId;
import org.guvnor.ala.runtime.providers.ProviderManager;
import org.guvnor.ala.runtime.providers.TestConnectionResult;

public class ProviderManagerFactory {

    private final Collection<ProviderManager> managers = new ArrayList<>();

    public ProviderManagerFactory() {
        //Empty constructor for Weld proxying
    }

    @Inject
    public ProviderManagerFactory(final Instance<ProviderManager> managers) {
        managers.iterator().forEachRemaining(this.managers::add);
    }

    public TestConnectionResult testConnection(final ProviderConfig providerConfig) throws ProviderOperationException {

        Optional<ProviderManager> providerManager = managers.stream()
                .filter(manager -> manager.supports(providerConfig))
                .findFirst();
        return providerManager.orElseThrow(() -> new ProviderOperationException("No provider manager was found for providerConfig: "
                                                                                        + providerConfig))
                .testConnection(providerConfig);
    }

    public TestConnectionResult testConnection(final ProviderId providerId) throws ProviderOperationException {
        Optional<ProviderManager> providerManager = managers.stream()
                .filter(manager -> manager.supports(providerId))
                .findFirst();
        return providerManager.orElseThrow(() -> new ProviderOperationException("No provider manager was found for providerId: "
                                                                                        + providerId))
                .testConnection(providerId);
    }
}
