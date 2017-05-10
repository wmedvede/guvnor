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

package org.guvnor.ala.ui.client.events;

import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.Provider;

/**
 * TODO: update me
 */
public class AddNewRuntime {

    private final ProviderType providerType;
    private final Provider provider;

    public AddNewRuntime( final Provider provider ) {
        this( null, provider );
    }

    public AddNewRuntime( final ProviderType providerType ) {
        this( providerType, null );
    }

    public AddNewRuntime( final ProviderType providerType,
                          final Provider provider ) {
        this.providerType = providerType;
        this.provider = provider;
    }

    public ProviderType getProviderType() {
        return providerType;
    }

    public Provider getProvider() {
        return provider;
    }
}
