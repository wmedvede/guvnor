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

import java.util.Collection;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.events.AddNewProviderType;
import org.guvnor.ala.ui.client.events.AddNewRuntime;
import org.guvnor.ala.ui.client.events.ProviderTypeListRefresh;
import org.guvnor.ala.ui.client.wizard.EnableProviderTypeWizard;
import org.guvnor.ala.ui.client.wizard.NewDeployWizard;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.ProviderTypeStatus;
import org.guvnor.ala.ui.service.ProviderTypeService;
import org.guvnor.ala.ui.service.RuntimeService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.slf4j.Logger;
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.workbench.panels.impl.StaticWorkbenchPanelPresenter;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;

@ApplicationScoped
@WorkbenchPerspective(identifier = "ProvisioningManagementPerspective")
public class ProvisioningManagementPerspective {

    private final Logger logger;
    private final Caller<ProviderTypeService> providerTypeService;
    private final Caller<RuntimeService> runtimeService;
    private final Event<ProviderTypeListRefresh > providerTypeListRefreshEvent;
    private final EnableProviderTypeWizard enableProviderTypeWizard;
    private final NewDeployWizard newDeployWizard;

    @Inject
    public ProvisioningManagementPerspective( final Logger logger,
                                              final Caller<ProviderTypeService> providerTypeService,
                                              final Caller<RuntimeService> runtimeService,
                                              final Event<ProviderTypeListRefresh> providerTypeListRefreshEvent,
                                              final EnableProviderTypeWizard enableProviderTypeWizard,
                                              final NewDeployWizard newDeployWizard ) {
        this.logger = logger;
        this.providerTypeService = providerTypeService;
        this.runtimeService = runtimeService;
        this.providerTypeListRefreshEvent = providerTypeListRefreshEvent;
        this.enableProviderTypeWizard = enableProviderTypeWizard;
        this.newDeployWizard = newDeployWizard;
    }

    @Perspective
    public PerspectiveDefinition buildPerspective() {
        final PerspectiveDefinition perspective = new PerspectiveDefinitionImpl( StaticWorkbenchPanelPresenter.class.getName() );
        perspective.setName( "ProvisioningManagementPerspective" );

        perspective.getRoot().addPart( new PartDefinitionImpl( new DefaultPlaceRequest( "ProvisioningManagementBrowser" ) ) );

        return perspective;
    }

    public void onAddNewProviderType( @Observes final AddNewProviderType addNewProviderTypeEvent ) {
        providerTypeService.call( new RemoteCallback<Map<ProviderType, ProviderTypeStatus>>() {
            @Override
            public void callback( final Map<ProviderType, ProviderTypeStatus> result ) {
                enableProviderTypeWizard.setup( result );
                enableProviderTypeWizard.start();
            }
        } ).getProviderTypesStatus();
    }

    public void onAddNewRuntime( @Observes final AddNewRuntime addNewRuntime ) {

        runtimeService.call( new RemoteCallback<Collection<String>>() {
            @Override
            public void callback( final Collection<String> result ) {
                newDeployWizard.setup( addNewRuntime.getProvider(), result );
                newDeployWizard.start();
            }
        } ).getPipelines( addNewRuntime.getProvider().getKey() );
    }

}
