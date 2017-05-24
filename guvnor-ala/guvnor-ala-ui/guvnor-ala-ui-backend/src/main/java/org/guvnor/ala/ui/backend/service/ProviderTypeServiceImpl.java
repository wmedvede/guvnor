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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.ala.services.api.backend.RuntimeProvisioningServiceBackend;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.ProviderTypeKey;
import org.guvnor.ala.ui.model.ProviderTypeStatus;
import org.guvnor.ala.ui.service.ProviderTypeService;
import org.jboss.errai.bus.server.annotations.Service;

@Service
@ApplicationScoped
public class ProviderTypeServiceImpl
        implements ProviderTypeService {

    //TODO, remove this temporal provider type definition.
    private static final ProviderType OSE = new ProviderType(new ProviderTypeKey(ProviderType.OPEN_SHIFT_PROVIDER_TYPE),
                                                             "OpenShift");

    private Map<String, ProviderType> allProviders = new HashMap<>();

    private Map<String, ProviderType> enabledProviders = new HashMap<>();

    private RuntimeProvisioningServiceBackend runtimeProvisioningService;

    public ProviderTypeServiceImpl() {
        //Empty constructor for Weld proxying
    }

    @Inject
    public ProviderTypeServiceImpl(RuntimeProvisioningServiceBackend runtimeProvisioningService) {
        this.runtimeProvisioningService = runtimeProvisioningService;
    }

    @PostConstruct
    private void init() {
        getAvialableProviderTypes().stream()
                .forEach(providerType -> allProviders.put(providerType.getKey().getId(),
                                                          providerType));
        //TODO The open shift provider don't exists, let's add it manually
        allProviders.put(OSE.getKey().getId(),
                         OSE);
    }

    @Override
    public Collection<ProviderType> getAvialableProviderTypes() {
        List<ProviderType> result = new ArrayList<>();
        List<org.guvnor.ala.runtime.providers.ProviderType> providers =
                runtimeProvisioningService.getProviderTypes(0,
                                                            100,
                                                            "providerTypeName",
                                                            true);

        if (providers != null) {
            for (org.guvnor.ala.runtime.providers.ProviderType provider : providers) {
                result.add(new ProviderType(new ProviderTypeKey(provider.getProviderTypeName()),
                                            provider.getProviderTypeName()));
            }
        }
        return result;
    }

    @Override
    public void enableProviderType(final ProviderType providerType) {
        enabledProviders.put(providerType.getKey().getId(),
                             providerType);
    }

    @Override
    public void enableProviderTypes(final Collection<ProviderType> providerTypes) {
        for (ProviderType providerType : providerTypes) {
            enableProviderType(providerType);
        }
    }

    @Override
    public void disableProvider(final ProviderType providerType) {
        enabledProviders.remove(providerType.getKey().getId());
    }

    @Override
    public Map<ProviderType, ProviderTypeStatus> getProviderTypesStatus() {
        final Map<ProviderType, ProviderTypeStatus> result = new HashMap<>(allProviders.size());
        final Set<ProviderType> providers = new HashSet<>(allProviders.values());
        for (final ProviderType providerType : enabledProviders.values()) {
            result.put(providerType,
                       ProviderTypeStatus.ENABLED);
            providers.remove(providerType);
        }
        for (ProviderType provider : providers) {
            result.put(provider,
                       ProviderTypeStatus.DISABLED);
        }
        return result;
    }

    @Override
    public ProviderType getProviderType(final ProviderTypeKey providerTypeKey) {
        return allProviders.get(providerTypeKey.getId());
    }

    public Collection<ProviderType> getEnabledProviderTypes() {
        return new ArrayList<>(enabledProviders.values());
    }
}
