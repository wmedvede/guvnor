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

package org.guvnor.ala.ui.client.provider;

import java.util.Collection;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.events.AddNewRuntime;
import org.guvnor.ala.ui.client.events.ProviderSelected;
import org.guvnor.ala.ui.client.events.ProviderTypeSelected;
import org.guvnor.ala.ui.client.events.RefreshRuntime;
import org.guvnor.ala.ui.client.provider.status.ProviderStatusPresenter;
import org.guvnor.ala.ui.client.provider.status.empty.ProviderStatusEmptyPresenter;
import org.guvnor.ala.ui.client.widget.provider.FormProvider;
import org.guvnor.ala.ui.client.widget.provider.FormResolver;
import org.guvnor.ala.ui.model.Provider;
import org.guvnor.ala.ui.model.ProviderKey;
import org.guvnor.ala.ui.model.RuntimeListItem;
import org.guvnor.ala.ui.service.ProviderService;
import org.guvnor.ala.ui.service.RuntimeService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.events.NotificationEvent;

@ApplicationScoped
public class ProviderPresenter {

    public interface View extends UberElement<ProviderPresenter> {

        void confirmRemove( final Command command );

        void setProviderName( String name );

        void setStatus( IsElement view );

        void setConfig( IsElement view );

        String getRemoveProviderSuccessMessage( );

        String getRemoveProviderErrorMessage( );
    }

    private final View view;
    private final Caller<ProviderService> providerService;
    private final Caller<RuntimeService> runtimeService;
    private final ProviderStatusEmptyPresenter providerStatusEmptyPresenter;
    private final ProviderStatusPresenter providerStatusPresenter;

    private final Event<NotificationEvent> notification;
    private final Event<ProviderTypeSelected > providerTypeSelected;
    private final Event<AddNewRuntime > addNewRuntimeEvent;

    public Provider provider;

    @Inject
    public ProviderPresenter( final View view,
                              final Caller<ProviderService> providerService,
                              final Caller<RuntimeService> runtimeService,
                              final ProviderStatusEmptyPresenter providerStatusEmptyPresenter,
                              final ProviderStatusPresenter providerStatusPresenter,
                              final Event<ProviderTypeSelected> providerTypeSelected,
                              final Event<NotificationEvent> notification,
                              final Event<AddNewRuntime> addNewRuntimeEvent ) {
        this.view = view;
        this.providerService = providerService;
        this.runtimeService = runtimeService;
        this.providerStatusEmptyPresenter = providerStatusEmptyPresenter;
        this.providerStatusPresenter = providerStatusPresenter;
        this.notification = notification;
        this.providerTypeSelected = providerTypeSelected;
        this.addNewRuntimeEvent = addNewRuntimeEvent;
    }

    @PostConstruct
    public void init() {
        this.view.init( this );
    }

    public void onProviderSelected( @Observes final ProviderSelected providerSelected ) {
        if ( providerSelected != null &&
                providerSelected.getProviderKey() != null ) {
            load( providerSelected.getProviderKey() );
        }
    }

    public void onRefreshRuntime( @Observes RefreshRuntime refreshRuntime ) {
        if ( refreshRuntime != null &&
                refreshRuntime.getProviderKey() != null &&
                refreshRuntime.getProviderKey().equals( provider.getKey() ) ) {
            load( refreshRuntime.getProviderKey() );
        }
    }

    private void load(final ProviderKey providerKey) {
        providerService.call((RemoteCallback<Provider>) provider -> {
                                 this.provider = provider;
                                 runtimeService.call(getLoadItemsSuccessCallback(),
                                                     new DefaultErrorCallback()).getRuntimesInfo(providerKey);
                             },
                             new DefaultErrorCallback()).getProvider(providerKey);
    }

    private RemoteCallback<Collection<RuntimeListItem>> getLoadItemsSuccessCallback() {
        return items -> {
                view.setProviderName( provider.getKey().getId() );
                if ( items.isEmpty() ) {
                    providerStatusEmptyPresenter.setup( provider.getKey() );
                    view.setStatus( providerStatusEmptyPresenter.getView() );
                } else {
                    providerStatusPresenter.setupItems( items );
                    view.setStatus( providerStatusPresenter.getView() );
                }
                final FormProvider formProvider = FormResolver.getFormProvider( provider.getKey() );
                formProvider.load( provider );
                formProvider.disable();
                view.setConfig( formProvider.getView() );
        };
    }

    public void refresh() {
        load( provider.getKey() );
    }

    public void removeProvider() {
        view.confirmRemove( () -> providerService.call( response -> {
            notification.fire( new NotificationEvent( view.getRemoveProviderSuccessMessage(), NotificationEvent.NotificationType.SUCCESS ) );

            providerTypeSelected.fire( new ProviderTypeSelected( provider.getKey().getProviderTypeKey() ) );
        }, ( o, throwable ) -> {
            notification.fire( new NotificationEvent( view.getRemoveProviderErrorMessage(), NotificationEvent.NotificationType.ERROR ) );
            providerTypeSelected.fire( new ProviderTypeSelected( provider.getKey().getProviderTypeKey() ) );
            return false;
        } ).deleteProvider( provider.getKey() ) );
    }

    public void deploy() {
        addNewRuntimeEvent.fire( new AddNewRuntime( provider ) );
    }

    public IsElement getView() {
        return view;
    }

}
