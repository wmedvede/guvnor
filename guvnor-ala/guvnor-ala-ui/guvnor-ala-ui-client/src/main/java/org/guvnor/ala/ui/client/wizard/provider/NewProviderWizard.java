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

package org.guvnor.ala.ui.client.wizard.provider;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.events.ProviderTypeSelectedEvent;
import org.guvnor.ala.ui.client.handler.ClientProviderHandlerRegistry;
import org.guvnor.ala.ui.client.handler.ProviderConfigurationForm;
import org.guvnor.ala.ui.client.util.PopupsUtil;
import org.guvnor.ala.ui.client.wizard.AbstractMultiPageWizard;
import org.guvnor.ala.ui.model.ProviderConfiguration;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.ProviderTypeKey;
import org.guvnor.ala.ui.service.ProviderService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.uberfire.workbench.events.NotificationEvent;

import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.NewProviderWizard_ProviderCreateErrorMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.NewProviderWizard_ProviderCreateSuccessMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.NewProviderWizard_title;

@ApplicationScoped
public class NewProviderWizard
        extends AbstractMultiPageWizard {

    private ProviderConfigurationPagePresenter providerConfigurationPage;
    private ClientProviderHandlerRegistry handlerRegistry;
    private PopupsUtil popupsUtil;
    private Caller<ProviderService> providerService;
    private Event<ProviderTypeSelectedEvent> providerTypeSelectedEvent;

    private ProviderType providerType;
    private Map<ProviderTypeKey, ProviderConfigurationForm> providerConfigurationFormMap = new HashMap<>();
    private ProviderConfigurationForm providerConfigurationForm;

    public NewProviderWizard() {
    }

    @Inject
    public NewProviderWizard(final ProviderConfigurationPagePresenter providerConfigurationPage,
                             final ClientProviderHandlerRegistry handlerRegistry,
                             final PopupsUtil popupsUtil,
                             final TranslationService translationService,
                             final Caller<ProviderService> providerService,
                             final Event<NotificationEvent> notification,
                             final Event<ProviderTypeSelectedEvent> providerTypeSelectedEvent) {
        super(translationService,
              notification);
        this.providerConfigurationPage = providerConfigurationPage;
        this.handlerRegistry = handlerRegistry;
        this.popupsUtil = popupsUtil;
        this.providerService = providerService;
        this.providerTypeSelectedEvent = providerTypeSelectedEvent;
    }

    @PostConstruct
    private void init() {
        pages.add(providerConfigurationPage);
    }

    public void setup(final ProviderType providerType) {
        this.providerType = providerType;
        this.providerConfigurationForm = getProviderConfigurationForm(providerType.getKey());
        if (providerConfigurationForm != null) {
            providerConfigurationPage.setProviderConfigurationForm(providerConfigurationForm);
        }
    }

    @Override
    public void start() {
        if (providerConfigurationForm == null) {
            //uncommon case
            popupsUtil.showErrorPopup("Provider type: " + (providerType != null ? providerType.getName() : null) + " is not properly setup.");
        } else {
            clear();
            super.start();
        }
    }

    @Override
    public String getTitle() {
        return translationService.getTranslation(NewProviderWizard_title);
    }

    @Override
    public int getPreferredHeight() {
        return 550;
    }

    @Override
    public int getPreferredWidth() {
        return 800;
    }

    @Override
    public void complete() {
        final ProviderConfiguration providerConfiguration = providerConfigurationPage.buildProviderConfiguration();
        providerService.call((Void aVoid) -> onCreateProviderSuccess(providerConfiguration),
                             (message, throwable) -> onCreateProviderError()).createProvider(providerType,
                                                                                             providerConfiguration);
    }

    private void onCreateProviderSuccess(final ProviderConfiguration providerConfiguration) {
        notification.fire(new NotificationEvent(translationService.getTranslation(NewProviderWizard_ProviderCreateSuccessMessage),
                                                NotificationEvent.NotificationType.SUCCESS));
        NewProviderWizard.super.complete();
        providerTypeSelectedEvent.fire(new ProviderTypeSelectedEvent(providerType.getKey(),
                                                                     providerConfiguration.getId()));
    }

    private boolean onCreateProviderError() {
        notification.fire(new NotificationEvent(translationService.getTranslation(NewProviderWizard_ProviderCreateErrorMessage),
                                                NotificationEvent.NotificationType.ERROR));
        start();
        return false;
    }

    private void clear() {
        providerConfigurationPage.clear();
    }

    private ProviderConfigurationForm getProviderConfigurationForm(ProviderTypeKey providerTypeKey) {
        ProviderConfigurationForm form = providerConfigurationFormMap.get(providerTypeKey);
        if (form == null &&
                handlerRegistry.isProviderEnabled(providerTypeKey) &&
                handlerRegistry.getProviderHandler(providerTypeKey).getFormResolver() != null) {
            form = handlerRegistry.getProviderHandler(providerTypeKey).getFormResolver().newProviderConfigurationForm();
            providerConfigurationFormMap.put(providerTypeKey,
                                             form);
        }
        return form;
    }
}
