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

import java.util.Collection;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.events.ProviderTypeListRefresh;
import org.guvnor.ala.ui.client.util.ContentChangeHandler;
import org.guvnor.ala.ui.model.ProviderType;
import org.jboss.errai.common.client.api.Caller;
import org.guvnor.ala.ui.client.wizard.providertype.EnableProviderTypePresenter;
import org.guvnor.ala.ui.model.ProviderTypeStatus;
import org.guvnor.ala.ui.service.ProviderTypeService;
import org.uberfire.workbench.events.NotificationEvent;

@ApplicationScoped
public class EnableProviderTypeWizard extends AbstractMultiPageWizard {

    private EnableProviderTypePresenter enableProviderTypePresenter;
    //    private final ProcessConfigPagePresenter processConfigPagePresenter;
    private Caller<ProviderTypeService> providerTypeService;
    private Event<NotificationEvent> notification;
    private Event<ProviderTypeListRefresh > providerTypeListRefresh;

    public EnableProviderTypeWizard() {
    }

    @Inject
    public EnableProviderTypeWizard( final EnableProviderTypePresenter enableProviderTypePresenter,
                                     final Caller<ProviderTypeService> providerTypeService,
                                     final Event<NotificationEvent> notification,
                                     Event<ProviderTypeListRefresh> providerTypeListRefresh ) {
        this.enableProviderTypePresenter = enableProviderTypePresenter;
        this.providerTypeService = providerTypeService;
        this.notification = notification;
        this.providerTypeListRefresh = providerTypeListRefresh;

        final ContentChangeHandler changePages = () -> {
        };

        this.enableProviderTypePresenter.addContentChangeHandler( changePages );
    }

    @Override
    public void start() {
        enableProviderTypePresenter.initialise();
        super.start();
    }

    public void setup( final Map<ProviderType, ProviderTypeStatus> providerTypeStatus ) {
        enableProviderTypePresenter.setup( providerTypeStatus );
        pages.clear();
        pages.add( enableProviderTypePresenter );
    }

    @Override
    public String getTitle() {
        return "Provider Type Enablement";
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
        final Collection<ProviderType > providerTypes = enableProviderTypePresenter.getSelectedProviderTypes();

        providerTypeService.call( o -> {
            notification.fire( new NotificationEvent( enableProviderTypePresenter.getEnableProviderTypeWizardSuccessMessage(), NotificationEvent.NotificationType.SUCCESS ) );
            EnableProviderTypeWizard.super.complete();
            providerTypeListRefresh.fire( new ProviderTypeListRefresh( providerTypes.iterator().next().getKey() ) );
        }, ( o, throwable ) -> {
            notification.fire( new NotificationEvent( enableProviderTypePresenter.getEnableProviderTypeWizardErrorMessage(), NotificationEvent.NotificationType.ERROR ) );
            EnableProviderTypeWizard.this.pageSelected( 0 );
            EnableProviderTypeWizard.this.start();
            return false;
        } ).enableProviderTypes( providerTypes );
    }
}
