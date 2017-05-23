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

package org.guvnor.ala.ui.client.ose;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.handler.FormResolver;
import org.guvnor.ala.ui.client.ose.provider.OSEProviderConfigPresenter;
import org.guvnor.ala.ui.client.handler.ProviderConfigurationForm;
import org.jboss.errai.ioc.client.api.ManagedInstance;

@ApplicationScoped
public class OSEFormResolver
        implements FormResolver {

    private ManagedInstance<OSEProviderConfigPresenter> providerConfigPresenters;

    @Inject
    public OSEFormResolver(ManagedInstance<OSEProviderConfigPresenter> providerConfigPresenters) {
        this.providerConfigPresenters = providerConfigPresenters;
    }

    @Override
    public ProviderConfigurationForm newProviderConfigurationForm() {
        return providerConfigPresenters.get();
    }

    @Override
    public boolean destroyForm(Object form) {
        if (form instanceof OSEProviderConfigPresenter) {
            providerConfigPresenters.destroy((OSEProviderConfigPresenter) form);
            return true;
        }
        return false;
    }
}
