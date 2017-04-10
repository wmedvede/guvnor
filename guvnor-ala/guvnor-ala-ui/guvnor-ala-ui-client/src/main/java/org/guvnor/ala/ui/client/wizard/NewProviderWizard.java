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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.events.ProviderTypeSelected;
import org.guvnor.ala.ui.client.util.ContentChangeHandler;
import org.guvnor.ala.ui.client.widget.provider.FormProvider;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.guvnor.ala.ui.client.wizard.provider.NewProviderFormPresenter;
import org.guvnor.ala.ui.model.ProviderConfiguration;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.service.ProviderService;
import org.uberfire.workbench.events.NotificationEvent;

@ApplicationScoped
public class NewProviderWizard extends AbstractMultiPageWizard {

    private NewProviderFormPresenter newProviderFormPresenter;
    //    private final ProcessConfigPagePresenter processConfigPagePresenter;
    private Caller<ProviderService> providerService;
    private Event<NotificationEvent> notification;
    private Event<ProviderTypeSelected > providerTypeSelectedEvent;

    private ProviderType providerType;

    public NewProviderWizard() {
    }

    @Inject
    public NewProviderWizard( final NewProviderFormPresenter newProviderFormPresenter,
                              //final ProcessConfigPagePresenter processConfigPagePresenter,
                              final Caller<ProviderService> providerService,
                              final Event<NotificationEvent> notification,
                              final Event<ProviderTypeSelected> providerTypeSelectedEvent ) {
        this.newProviderFormPresenter = newProviderFormPresenter;
//        this.processConfigPagePresenter = processConfigPagePresenter;
        this.providerService = providerService;
        this.notification = notification;
        this.providerTypeSelectedEvent = providerTypeSelectedEvent;

        final ContentChangeHandler changePages = () -> {
        };

        this.newProviderFormPresenter.addContentChangeHandler( changePages );
    }

    @Override
    public void start() {
        newProviderFormPresenter.initialise();
        super.start();
    }

    @Override
    public String getTitle() {
        return "New Provider Wizard";
    }

    @Override
    public int getPreferredHeight() {
        return 550;
    }

    @Override
    public int getPreferredWidth() {
        return 800;
    }

    public void setup( final ProviderType providerType,
                       final FormProvider formProvider ) {
        this.providerType = providerType;
        newProviderFormPresenter.setFormProvider( formProvider );
        pages.clear();
        pages.add( newProviderFormPresenter );
//        if ( providerType.getCapabilities().contains( Capability.PROCESS.toString() ) ) {
//            pages.add( processConfigPagePresenter );
//        }
    }

    public void clear() {
        newProviderFormPresenter.clear();
//        processConfigPagePresenter.clear();
        pages.clear();
        pages.add( newProviderFormPresenter );
    }

    @Override
    public void close() {
        super.close();
        clear();
    }

    @Override
    public void complete() {
        final ProviderConfiguration providerConfiguration = newProviderFormPresenter.buildProviderConfiguration();

        providerService.call( (RemoteCallback<Void>) o -> {
            notification.fire( new NotificationEvent( newProviderFormPresenter.getNewProviderWizardSuccessMessage(), NotificationEvent.NotificationType.SUCCESS ) );
            clear();
            NewProviderWizard.super.complete();
            providerTypeSelectedEvent.fire( new ProviderTypeSelected( providerType, providerConfiguration.getId() ) );
        }, ( o, throwable ) -> {
            notification.fire( new NotificationEvent( newProviderFormPresenter.getNewPoviderCreateErrorMessage(), NotificationEvent.NotificationType.ERROR ) );
            NewProviderWizard.this.pageSelected( 0 );
            NewProviderWizard.this.start();
            return false;
        } ).createProvider( providerType, providerConfiguration );
    }
}
