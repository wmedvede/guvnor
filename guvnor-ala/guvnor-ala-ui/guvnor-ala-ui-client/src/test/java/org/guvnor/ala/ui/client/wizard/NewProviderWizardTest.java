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

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.guvnor.ala.ui.client.events.ProviderTypeSelectedEvent;
import org.guvnor.ala.ui.client.handler.ClientProviderHandler;
import org.guvnor.ala.ui.client.handler.ClientProviderHandlerRegistry;
import org.guvnor.ala.ui.client.handler.FormResolver;
import org.guvnor.ala.ui.client.handler.ProviderConfigurationForm;
import org.guvnor.ala.ui.client.util.PopupsUtil;
import org.guvnor.ala.ui.client.wizard.provider.ProviderConfigurationPagePresenter;
import org.guvnor.ala.ui.model.ProviderConfiguration;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.service.ProviderService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.uberfire.ext.widgets.core.client.wizards.WizardPageStatusChangeEvent;
import org.uberfire.mocks.CallerMock;
import org.uberfire.mocks.EventSourceMock;
import org.uberfire.workbench.events.NotificationEvent;

import static org.guvnor.ala.ui.client.ProvisioningManagementTestCommons.ERROR_MESSAGE;
import static org.guvnor.ala.ui.client.ProvisioningManagementTestCommons.SUCCESS_MESSAGE;
import static org.guvnor.ala.ui.client.ProvisioningManagementTestCommons.mockProviderType;
import static org.guvnor.ala.ui.client.ProvisioningManagementTestCommons.preparePageCompletion;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.NewProviderWizard_ProviderCreateErrorMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.NewProviderWizard_ProviderCreateSuccessMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.NewProviderWizard_ProviderNotProperlyConfiguredInSystemErrorMessage;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class NewProviderWizardTest
        extends WizardBaseTest {

    @Mock
    private ProviderConfigurationPagePresenter configurationPage;

    @Mock
    private ClientProviderHandlerRegistry handlerRegistry;

    @Mock
    private PopupsUtil popupsUtil;

    @Mock
    private ProviderService providerService;

    @Mock
    private EventSourceMock<ProviderTypeSelectedEvent> providerTypeSelectedEvent;

    private NewProviderWizard wizard;

    @Mock
    private ClientProviderHandler providerHandler;

    @Mock
    private FormResolver formResolver;

    @Mock
    private ProviderConfigurationForm configurationForm;

    @Mock
    private ProviderConfiguration providerConfiguration;

    private ProviderType providerType;

    @Before
    public void setUp() {
        wizard = new NewProviderWizard(configurationPage,
                                       handlerRegistry,
                                       popupsUtil,
                                       translationService,
                                       new CallerMock<>(providerService),
                                       notification,
                                       providerTypeSelectedEvent) {
            {
                this.view = wizardView;
            }
        };
        wizard.init();

        providerType = mockProviderType("NewProviderWizardTest");
        when(handlerRegistry.isProviderEnabled(providerType.getKey())).thenReturn(true);
        when(handlerRegistry.getProviderHandler(providerType.getKey())).thenReturn(providerHandler);
        when(providerHandler.getFormResolver()).thenReturn(formResolver);
        when(formResolver.newProviderConfigurationForm()).thenReturn(configurationForm);

        when(translationService.format(NewProviderWizard_ProviderNotProperlyConfiguredInSystemErrorMessage,
                                       providerType.getName()))
                .thenReturn(ERROR_MESSAGE);
        when(translationService.getTranslation(NewProviderWizard_ProviderCreateSuccessMessage)).thenReturn(SUCCESS_MESSAGE);
        when(translationService.getTranslation(NewProviderWizard_ProviderCreateErrorMessage)).thenReturn(ERROR_MESSAGE);
    }

    @Test
    public void testSetupProviderConfigured() {
        wizard.setup(providerType);
        verify(handlerRegistry,
               times(2)).getProviderHandler(providerType.getKey());
        verify(providerHandler,
               times(2)).getFormResolver();
        verify(formResolver,
               times(1)).newProviderConfigurationForm();
        verify(configurationPage,
               times(1)).setProviderConfigurationForm(configurationForm);
    }

    @Test
    public void testSetupProviderNotConfigured() {
        //the provider is not configured
        when(handlerRegistry.isProviderEnabled(providerType.getKey())).thenReturn(false);

        wizard.setup(providerType);

        verify(handlerRegistry,
               never()).getProviderHandler(providerType.getKey());
        verify(providerHandler,
               never()).getFormResolver();
        verify(formResolver,
               never()).newProviderConfigurationForm();
        verify(configurationPage,
               never()).setProviderConfigurationForm(configurationForm);

        wizard.start();
        verify(popupsUtil,
               times(1)).showErrorPopup(ERROR_MESSAGE);
    }

    @Test
    public void testCreateProvider() {
        //initialize and start the wizard.
        wizard.setup(providerType);
        wizard.start();

        //emulate that the page was completed.
        when(configurationPage.buildProviderConfiguration()).thenReturn(providerConfiguration);

        preparePageCompletion(configurationPage);
        wizard.onStatusChange(new WizardPageStatusChangeEvent(configurationPage));

        //emulate the user pressing the finish button.
        wizard.complete();

        //verify that the provider has been created and the proper notifications were fired.
        verify(providerService,
               times(1)).createProvider(providerType,
                                        providerConfiguration);
        verify(notification,
               times(1)).fire(new NotificationEvent(SUCCESS_MESSAGE,
                                                    NotificationEvent.NotificationType.SUCCESS));
        verify(providerTypeSelectedEvent,
               times(1)).fire(new ProviderTypeSelectedEvent(providerType.getKey(),
                                                            providerConfiguration.getId()));
    }
}
