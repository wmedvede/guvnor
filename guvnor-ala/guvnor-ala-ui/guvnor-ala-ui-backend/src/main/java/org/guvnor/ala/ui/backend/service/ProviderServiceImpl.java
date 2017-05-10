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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.ala.config.ProviderConfig;
import org.guvnor.ala.docker.config.DockerProviderConfig;
import org.guvnor.ala.services.api.backend.RuntimeProvisioningServiceBackend;
import org.guvnor.ala.ui.model.ProviderTypeKey;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.Provider;
import org.guvnor.ala.ui.model.ProviderConfiguration;
import org.guvnor.ala.ui.model.ProviderKey;
import org.guvnor.ala.ui.model.WF10ProviderConfigParams;
import org.guvnor.ala.ui.service.ProviderService;
import org.guvnor.ala.wildfly.config.WildflyProviderConfig;
import org.guvnor.ala.wildfly.config.impl.WildflyProviderConfigImpl;
import org.jboss.errai.bus.server.annotations.Service;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.uberfire.commons.validation.PortablePreconditions.checkNotEmpty;
import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;
import static org.guvnor.ala.ui.backend.service.util.ServiceUtil.*;

@Service
@ApplicationScoped
public class ProviderServiceImpl
        implements ProviderService {

    private RuntimeProvisioningServiceBackend runtimeProvisioningService;

    public ProviderServiceImpl() {
        //Empty constructor for Weld proxying
    }

    @Inject
    public ProviderServiceImpl(RuntimeProvisioningServiceBackend runtimeProvisioningService) {
        this.runtimeProvisioningService = runtimeProvisioningService;
    }

    @Override
    public Collection< Provider > getProviders(final ProviderType providerType) {

        Collection< Provider > result = new ArrayList<>();
        List< org.guvnor.ala.runtime.providers.Provider > providers =
                runtimeProvisioningService.getProviders(0,
                                                        10,
                                                        "providerTypeName",
                                                        true);

        if (providers != null) {
            result = providers.stream()
                    .filter(provider -> provider.getProviderType().getProviderTypeName().equals(providerType.getKey().getId()))
                    .map(this::translateProvider)
                    .collect(toList());
        }
        return result;
    }

    @Override
    public Collection< ProviderKey > getProvidersKey(final ProviderType providerType) {
        return print("getProvidersKey",
                     getProviders(providerType).stream()
                             .map(p -> new ProviderKey(p.getKey().getProviderTypeKey(),
                                                       p.getKey().getId(),
                                                       p.getKey().getName()))
                             .collect(toCollection(ArrayList::new)));
    }

    @Override
    public boolean isValidProvider(final ProviderType providerType,
                                   final String id,
                                   final String name) {
        checkNotNull("providerType",
                     providerType);
        checkNotEmpty("id",
                      id);
        checkNotEmpty("name",
                      name);

        for (final Provider provider : getProviders(providerType)) {
            if (name.equals(provider.getKey().getName()) ||
                    id.equals(provider.getKey().getId())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Translates an internal Provider format into a guvnor-ala-ui Provider
     * TODO We need to review this idea of translation from internal representation to an ala-ui representation.
     */
    private Provider translateProvider(org.guvnor.ala.runtime.providers.Provider provider) {
        Provider result = null;
        if (provider != null) {
            //TODO the ProviderType at server side has name but not an id. Likely we should make the server side provider type to also have an id?
            //the server side Provider does not have a name, but only id. Likely we should make it have a name too. The name can be passed in the ProviderConfig when
            //created but If we add a name in the Provider interface then we'll be able to manage all provider sin a seamless way.
            result = new Provider(new ProviderKey(new ProviderTypeKey(provider.getProviderType().getProviderTypeName() /*use the name by now*/),
                                                  provider.getId(),
                                                  provider.getId()), /*use the id as the name by now*/
                                  translateProviderConfig(provider.getConfig()));
        }
        return result;
    }

    /**
     * Translates an internal ProviderConfig into a guvnor-ala-ui configuration format that is a Map by now.
     * TODO We need to review this idea of translation from internal representation to an ala-ui representation.
     */
    private Map translateProviderConfig(org.guvnor.ala.config.ProviderConfig providerConfig) {
        //TODO review this translation
        Map result = new HashMap();
        if (providerConfig == null) {
            return null;
        } else if (providerConfig instanceof WildflyProviderConfig) {
            WildflyProviderConfig wfConfig = (WildflyProviderConfig) providerConfig;
            result.put(WF10ProviderConfigParams.PROVIDER_NAME,
                       wfConfig.getName());
            result.put(WF10ProviderConfigParams.HOST,
                       wfConfig.getHostIp());
            result.put(WF10ProviderConfigParams.PORT,
                       wfConfig.getPort());
            result.put(WF10ProviderConfigParams.MANAGEMENT_PORT,
                       wfConfig.getManagementPort());
            result.put(WF10ProviderConfigParams.USER,
                       wfConfig.getUser());
            result.put(WF10ProviderConfigParams.PASSWORD,
                       wfConfig.getPassword());
        } else if (providerConfig instanceof DockerProviderConfig) {
            DockerProviderConfig dockerConfig = (DockerProviderConfig) providerConfig;
            result.put("name",
                       dockerConfig.getName());
            result.put("host",
                       dockerConfig.getHostIp());
        }
        return result;
    }

    private ProviderConfig createProviderConfig(ProviderType providerType,
                                                ProviderConfiguration providerConfiguration) {

        ProviderConfig result;
        Map values = providerConfiguration.getValues();
        if (ProviderType.OPEN_SHIFT_PROVIDER_TYPE.equals(providerType.getKey().getId())) {
            //not yet implemented.
            throw new RuntimeException("Openshift provider is not yet implemented");
        } else if (ProviderType.WILDFY_PROVIDER_TYPE.equals(providerType.getKey().getId())) {
            result = new WildflyProviderConfigImpl(providerConfiguration.getName(),
                                                   getStringValue(values,
                                                                  WF10ProviderConfigParams.HOST),
                                                   getStringValue(values,
                                                                  WF10ProviderConfigParams.PORT),
                                                   getStringValue(values,
                                                                  WF10ProviderConfigParams.MANAGEMENT_PORT),
                                                   getStringValue(values,
                                                                  WF10ProviderConfigParams.USER),
                                                   getStringValue(values,
                                                                  WF10ProviderConfigParams.PASSWORD));
        } else if (ProviderType.DOCKER_PROVIDER_TYPE.equals(providerType.getKey().getId())) {
            throw new RuntimeException("Docker not yet implemented at client side");
            /*
            result = new DockerProviderConfigImpl( providerConfiguration.getName(),
                    getStringValue( values,  ));

            }
            */
            /*
            final String name,
                                      final String hostIp,
                                      final String port,
                                      final String managementPort,
                                      final String user,
                                      final String password

             */
        } else {
            throw new RuntimeException("Unknown provider type: " + providerType);
        }
        return result;
    }

    @Override
    public void createProvider(final ProviderType providerType,
                               final ProviderConfiguration configuration) {
        checkNotNull("providerType",
                     providerType);
        checkNotEmpty("ProviderConfiguration",
                      configuration.getValues());

        if (!isValidProvider(providerType,
                             configuration.getId(),
                             configuration.getName())) {
            throw new RuntimeException();
        }
        runtimeProvisioningService.registerProvider(createProviderConfig(providerType,
                                                                         configuration));
    }

    @Override
    public void deleteProvider(final ProviderKey providerKey) {
        //providerKey.getName() or providerKey.getId() for teh deleteion?
        runtimeProvisioningService.unregisterProvider(providerKey.getId());
    }

    @Override
    public Provider getProvider(final ProviderKey providerKey) {
        List< org.guvnor.ala.runtime.providers.Provider > providers =
                runtimeProvisioningService.getProviders(0,
                                                        10,
                                                        "providerTypeName",
                                                        true);
        Optional< Provider > result = Optional.empty();
        if (providers != null) {
            result = providers.stream()
                    .filter(provider -> provider.getId().equals(providerKey.getId()))
                    .map(this::translateProvider)
                    .findFirst();
        }
        return result.orElse(null);
    }

    public < T > Collection< T > print(final String ref,
                                       final Collection< T > input) {

        System.out.println(ref + " [" + input.toString() + "]");

        return input;
    }
}
