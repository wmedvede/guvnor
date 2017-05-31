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

package org.guvnor.ala.ui.client;

import java.util.Collection;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.events.AddNewProviderEvent;
import org.guvnor.ala.ui.client.events.AddNewProviderTypeEvent;
import org.guvnor.ala.ui.client.events.AddNewRuntimeEvent;
import org.guvnor.ala.ui.client.wizard.NewProviderWizard;
import org.guvnor.ala.ui.client.wizard.EnableProviderTypeWizard;
import org.guvnor.ala.ui.client.wizard.NewDeployWizard;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.ProviderTypeStatus;
import org.guvnor.ala.ui.service.ProviderTypeService;
import org.guvnor.ala.ui.service.RuntimeService;
import org.jboss.errai.common.client.api.Caller;
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.workbench.panels.impl.StaticWorkbenchPanelPresenter;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;

import static org.guvnor.ala.ui.client.ProvisioningManagementPerspective.IDENTIFIER;

@ApplicationScoped
@WorkbenchPerspective(identifier = IDENTIFIER)
public class ProvisioningManagementPerspective {

    public static final String IDENTIFIER = "ProvisioningManagementPerspective";

    private final Caller<ProviderTypeService> providerTypeService;
    private final Caller<RuntimeService> runtimeService;
    private final EnableProviderTypeWizard enableProviderTypeWizard;
    private final NewProviderWizard newProviderWizard;
    private final NewDeployWizard newDeployWizard;

    @Inject
    public ProvisioningManagementPerspective(final Caller<ProviderTypeService> providerTypeService,
                                             final Caller<RuntimeService> runtimeService,
                                             final EnableProviderTypeWizard enableProviderTypeWizard,
                                             final NewProviderWizard newProviderWizard,
                                             final NewDeployWizard newDeployWizard) {
        this.providerTypeService = providerTypeService;
        this.runtimeService = runtimeService;
        this.enableProviderTypeWizard = enableProviderTypeWizard;
        this.newProviderWizard = newProviderWizard;
        this.newDeployWizard = newDeployWizard;
    }

    @Perspective
    public PerspectiveDefinition buildPerspective() {
        final PerspectiveDefinition perspective = new PerspectiveDefinitionImpl(StaticWorkbenchPanelPresenter.class.getName());
        perspective.setName(IDENTIFIER);
        perspective.getRoot().addPart(new PartDefinitionImpl(new DefaultPlaceRequest(ProvisioningManagementBrowserPresenter.IDENTIFIER)));
        return perspective;
    }

    protected void onAddNewProviderType(@Observes final AddNewProviderTypeEvent event) {
        providerTypeService.call((Map<ProviderType, ProviderTypeStatus> result) -> {
                                     enableProviderTypeWizard.setup(result);
                                     enableProviderTypeWizard.start();
                                 },
                                 new DefaultErrorCallback()).getProviderTypesStatus();
    }

    public void onNewProvider(@Observes final AddNewProviderEvent event) {
        if (event.getProviderType() != null && event.getProviderType().getKey() != null) {
            newProviderWizard.setup(event.getProviderType());
            newProviderWizard.start();
        }
    }

    protected void onAddNewRuntime(@Observes final AddNewRuntimeEvent event) {
        runtimeService.call((Collection<String> result) -> {
                                newDeployWizard.setup(event.getProvider(),
                                                      result);
                                newDeployWizard.start();
                            },
                            new DefaultErrorCallback()).getPipelines(event.getProvider().getKey().getProviderTypeKey());
    }
}
