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

package org.guvnor.ala.ui.client.navigation.providertype;

import java.util.Collection;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.events.AddNewProvider;
import org.guvnor.ala.ui.client.events.ProviderSelectedEvent;
import org.guvnor.ala.ui.client.events.ProviderTypeListRefreshEvent;
import org.guvnor.ala.ui.model.ProviderType;
import org.jboss.errai.common.client.api.Caller;
import org.guvnor.ala.ui.model.ProviderKey;
import org.guvnor.ala.ui.service.ProviderTypeService;
import org.slf4j.Logger;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.events.NotificationEvent;

@ApplicationScoped
public class ProviderTypePresenter {

    public interface View extends UberElement<ProviderTypePresenter> {

        void clear( );

        void setProviderType( final String id,
                              final String name );

        void selectProvider( final String id );

        void addProvider( final String providerId,
                          final String providerName,
                          final Command onSelect );

        void confirmRemove( Command command );
    }

    private final Logger logger;
    private final View view;
    private final Caller<ProviderTypeService> providerTypeService;

    private final Event<NotificationEvent> notification;
    private final Event<AddNewProvider > addNewProviderEvent;
    private final Event<ProviderTypeListRefreshEvent> providerTypeListRefreshEvent;
    private final Event<ProviderSelectedEvent> providerSelectedEvent;

    private ProviderType providerType;

    @Inject
    public ProviderTypePresenter( final Logger logger,
                                  final View view,
                                  final Caller<ProviderTypeService> providerTypeService,
                                  final Event<NotificationEvent> notification,
                                  final Event<AddNewProvider> addNewProviderEvent,
                                  final Event<ProviderTypeListRefreshEvent> providerTypeListRefreshEvent,
                                  final Event<ProviderSelectedEvent> providerSelectedEvent ) {
        this.logger = logger;
        this.view = view;
        this.providerTypeService = providerTypeService;
        this.notification = notification;
        this.addNewProviderEvent = addNewProviderEvent;
        this.providerTypeListRefreshEvent = providerTypeListRefreshEvent;
        this.providerSelectedEvent = providerSelectedEvent;
    }

    @PostConstruct
    public void init() {
        view.init( this );
    }

    public View getView() {
        return view;
    }

    public ProviderType getCurrentProviderType() {
        return providerType;
    }

    public void setup( final ProviderType providerType,
                       final Collection<ProviderKey> providers,
                       final ProviderKey firstProviderKey ) {
        view.clear();
        this.providerType = providerType;
        view.setProviderType( providerType.getKey().getId(), providerType.getName() );

        if ( firstProviderKey != null ) {
            addProvider( firstProviderKey );
            for ( final ProviderKey provider : providers ) {
                if ( !provider.equals( firstProviderKey ) ) {
                    addProvider( provider );
                }
            }
            providerSelectedEvent.fire( new ProviderSelectedEvent(firstProviderKey ) );
        }
    }

    private void addProvider( final ProviderKey provider ) {
        view.addProvider( provider.getId(), provider.getId(), () -> providerSelectedEvent.fire( new ProviderSelectedEvent(provider ) ) );
    }

    public void onProviderSelect( @Observes final ProviderSelectedEvent providerSelectedEvent) {
        if ( providerSelectedEvent != null &&
                providerSelectedEvent.getProviderKey() != null &&
                providerSelectedEvent.getProviderKey().getId() != null &&
                providerSelectedEvent.getProviderKey().getProviderTypeKey() != null &&
                providerSelectedEvent.getProviderKey().getProviderTypeKey().equals(providerType.getKey() ) ) {
            view.selectProvider(providerSelectedEvent.getProviderKey().getId() );
        } else {
            logger.warn( "Illegal event argument." );
        }
    }

    public void addNewProvider() {
        addNewProviderEvent.fire( new AddNewProvider( providerType ) );
    }

    public void removeProviderType() {
        view.confirmRemove(
                () -> providerTypeService.call(
                        none -> providerTypeListRefreshEvent.fire( new ProviderTypeListRefreshEvent() ) )
                        .disableProvider( providerType )
        );
    }

}
