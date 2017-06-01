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

package org.guvnor.ala.ui.client.wizard;

import java.util.ArrayList;
import java.util.List;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.guvnor.ala.ui.client.events.ProviderTypeListRefreshEvent;
import org.guvnor.ala.ui.client.wizard.providertype.EnableProviderTypePagePresenter;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.ProviderTypeStatus;
import org.guvnor.ala.ui.service.ProviderTypeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.uberfire.commons.data.Pair;
import org.uberfire.ext.widgets.core.client.wizards.WizardPageStatusChangeEvent;
import org.uberfire.mocks.CallerMock;
import org.uberfire.mocks.EventSourceMock;
import org.uberfire.workbench.events.NotificationEvent;

import static org.guvnor.ala.ui.client.ProvisioningManagementTestCommons.ERROR_MESSAGE;
import static org.guvnor.ala.ui.client.ProvisioningManagementTestCommons.SUCCESS_MESSAGE;
import static org.guvnor.ala.ui.client.ProvisioningManagementTestCommons.buildProviderTypeStatusList;
import static org.guvnor.ala.ui.client.ProvisioningManagementTestCommons.mockProviderTypeList;
import static org.guvnor.ala.ui.client.ProvisioningManagementTestCommons.preparePageCompletion;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.EnableProviderTypeWizard_ProviderTypeEnableErrorMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.EnableProviderTypeWizard_ProviderTypeEnableSuccessMessage;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class EnableProviderTypeWizardTest
        extends WizardBaseTest {

    @Mock
    private EnableProviderTypePagePresenter enableProviderTypePage;

    @Mock
    private ProviderTypeService providerTypeService;

    @Mock
    private EventSourceMock<ProviderTypeListRefreshEvent> providerTypeListRefreshEvent;

    private EnableProviderTypeWizard wizard;

    private List<ProviderType> providerTypes;

    private List<Pair<ProviderType, ProviderTypeStatus>> providerTypeStatus;

    @Before
    public void setUp() {
        //mock an arbitrary set of provider types.
        providerTypes = mockProviderTypeList(3);
        providerTypeStatus = buildProviderTypeStatusList(providerTypes,
                                                         ProviderTypeStatus.DISABLED);

        //setup translationService messages.
        when(translationService.getTranslation(EnableProviderTypeWizard_ProviderTypeEnableSuccessMessage))
                .thenReturn(SUCCESS_MESSAGE);
        when(translationService.getTranslation(EnableProviderTypeWizard_ProviderTypeEnableErrorMessage))
                .thenReturn(ERROR_MESSAGE);

        wizard = new EnableProviderTypeWizard(enableProviderTypePage,
                                              translationService,
                                              new CallerMock<>(providerTypeService),
                                              notification,
                                              providerTypeListRefreshEvent) {
            {
                this.view = wizardView;
            }
        };
        wizard.init();
    }

    @Test
    public void testEnableProvider() {
        //initialize and start the wizard.
        wizard.setup(providerTypeStatus);
        wizard.start();

        //select a couple of providers and emulate page completion.
        int selectedIndex1 = 1;
        int selectedIndex2 = 2;
        List<ProviderType> selectedProviders = new ArrayList<>();
        selectedProviders.add(providerTypes.get(selectedIndex1));
        selectedProviders.add(providerTypes.get(selectedIndex2));
        when(enableProviderTypePage.getSelectedProviderTypes()).thenReturn(selectedProviders);

        preparePageCompletion(enableProviderTypePage);
        wizard.onStatusChange(new WizardPageStatusChangeEvent(enableProviderTypePage));

        //emulates the user pressing the finish button
        wizard.complete();

        //verify the provider types has been enabled and the proper notifications were fired.
        verify(providerTypeService,
               times(1)).enableProviderTypes(selectedProviders);
        verify(notification,
               times(1)).fire(new NotificationEvent(SUCCESS_MESSAGE,
                                                    NotificationEvent.NotificationType.SUCCESS));

        verify(providerTypeListRefreshEvent,
               times(1)).fire(new ProviderTypeListRefreshEvent(selectedProviders.get(0).getKey()));
    }
}
