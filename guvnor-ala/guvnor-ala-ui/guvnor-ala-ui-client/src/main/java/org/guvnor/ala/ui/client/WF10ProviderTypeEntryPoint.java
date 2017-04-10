/*
 * Copyright ${year} Red Hat, Inc. and/or its affiliates.
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

package org.guvnor.ala.ui.client;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.events.AddNewProvider;
import org.guvnor.ala.ui.client.widget.provider.wf10.WF10ProviderConfigPresenter;
import org.guvnor.ala.ui.model.ProviderType;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.guvnor.ala.ui.client.wizard.NewProviderWizard;

@EntryPoint
public class WF10ProviderTypeEntryPoint {

    private final WF10ProviderConfigPresenter providerConfigPresenter;
    private final NewProviderWizard newProviderWizard;

    @Inject
    public WF10ProviderTypeEntryPoint( final WF10ProviderConfigPresenter providerConfigPresenter,
                                       final NewProviderWizard newProviderWizard ) {
        this.providerConfigPresenter = providerConfigPresenter;
        this.newProviderWizard = newProviderWizard;
    }

    public void onNewProvider( @Observes final AddNewProvider addNewProvider ) {
        if ( addNewProvider != null &&
                addNewProvider.getProviderType() != null &&
                addNewProvider.getProviderType().getId() != null &&
                addNewProvider.getProviderType().getId().equals( ProviderType.WILDFY_PROVIDER_TYPE ) ) {
            newProviderWizard.clear();
            newProviderWizard.setup( addNewProvider.getProviderType(), providerConfigPresenter );
            newProviderWizard.start();
        }
    }
}
