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

package org.guvnor.ala.ui.service;

import java.util.Collection;
import java.util.Map;

import org.jboss.errai.bus.server.annotations.Remote;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.ProviderTypeKey;
import org.guvnor.ala.ui.model.ProviderTypeStatus;

/**
 * Client side service for getting information regarding the existing provider types.
 */
@Remote
public interface ProviderTypeService {

    Collection< ProviderType > getEnabledProviderTypes( );

    Collection< ProviderType > getAvialableProviderTypes( );

    void enableProviderType( final ProviderType providerType );

    void enableProviderTypes( final Collection< ProviderType > providerTypes );

    void disableProvider( final ProviderType providerType );

    Map< ProviderType, ProviderTypeStatus > getProviderTypesStatus( );

    ProviderType getProviderType( final ProviderTypeKey providerTypeKey );

}