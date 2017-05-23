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

package org.guvnor.ala.ui.client.wizard.providertype;

import java.util.Collection;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.events.ProviderTypeListRefreshEvent;
import org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants;
import org.guvnor.ala.ui.client.wizard.AbstractMultiPageWizard;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.ProviderTypeStatus;
import org.guvnor.ala.ui.service.ProviderTypeService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.uberfire.workbench.events.NotificationEvent;

@ApplicationScoped
public class EnableProviderTypeWizard
        extends AbstractMultiPageWizard {

    private EnableProviderTypePagePresenter enableProviderTypePagePresenter;
    private Caller<ProviderTypeService> providerTypeService;
    private Event<ProviderTypeListRefreshEvent> providerTypeListRefresh;

    public EnableProviderTypeWizard() {
    }

    @Inject
    public EnableProviderTypeWizard(final EnableProviderTypePagePresenter enableProviderTypePage,
                                    final TranslationService translationService,
                                    final Caller<ProviderTypeService> providerTypeService,
                                    final Event<NotificationEvent> notification,
                                    final Event<ProviderTypeListRefreshEvent> providerTypeListRefresh) {
        super(translationService,
              notification);
        this.enableProviderTypePagePresenter = enableProviderTypePage;
        this.providerTypeService = providerTypeService;
        this.notification = notification;
        this.providerTypeListRefresh = providerTypeListRefresh;
    }

    @Override
    public void start() {
        enableProviderTypePagePresenter.initialise();
        super.start();
    }

    public void setup(final Map<ProviderType, ProviderTypeStatus> providerTypeStatus) {
        enableProviderTypePagePresenter.setup(providerTypeStatus);
        pages.clear();
        pages.add(enableProviderTypePagePresenter);
    }

    @Override
    public String getTitle() {
        return translationService.getTranslation(GuvnorAlaUIConstants.EnableProviderTypeWizard_title);
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
    public void close() {
        super.close();
    }

    @Override
    public void complete() {
        final Collection<ProviderType> providerTypes = enableProviderTypePagePresenter.getSelectedProviderTypes();

        providerTypeService.call((Void aVoid) -> onEnableTypesSuccess(providerTypes),
                                 (message, throwable) -> onEnableTypesError()).enableProviderTypes(providerTypes);
    }

    private void onEnableTypesSuccess(final Collection<ProviderType> providerTypes) {
        notification.fire(new NotificationEvent(enableProviderTypePagePresenter.getEnableProviderTypeWizardSuccessMessage(),
                                                NotificationEvent.NotificationType.SUCCESS));
        EnableProviderTypeWizard.super.complete();
        providerTypeListRefresh.fire(new ProviderTypeListRefreshEvent(providerTypes.iterator().next().getKey()));
    }

    private boolean onEnableTypesError() {
        notification.fire(new NotificationEvent(enableProviderTypePagePresenter.getEnableProviderTypeWizardErrorMessage(),
                                                NotificationEvent.NotificationType.ERROR));
        EnableProviderTypeWizard.this.pageSelected(0);
        EnableProviderTypeWizard.this.start();
        return false;
    }
}
