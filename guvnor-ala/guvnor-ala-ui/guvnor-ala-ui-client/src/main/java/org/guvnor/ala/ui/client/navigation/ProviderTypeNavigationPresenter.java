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

package org.guvnor.ala.ui.client.navigation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.events.AddNewProviderType;
import org.guvnor.ala.ui.client.events.ProviderTypeListRefresh;
import org.guvnor.ala.ui.client.events.ProviderTypeSelected;
import org.guvnor.ala.ui.model.ProviderType;
import org.slf4j.Logger;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.mvp.Command;

import static org.uberfire.commons.validation.PortablePreconditions.*;

@ApplicationScoped
public class ProviderTypeNavigationPresenter {

    public interface View extends UberElement<ProviderTypeNavigationPresenter> {

        void addProviderType( final String id,
                              final String name,
                              final Command select );

        void select( final String id );

        void clean( );
    }

    private final Logger logger;
    private final View view;

    private final Event<AddNewProviderType > addNewProviderTypeEvent;
    private final Event<ProviderTypeListRefresh > providerTypeListRefreshEvent;
    private final Event<ProviderTypeSelected > providerTypeSelectedEvent;

    private Set<ProviderType> providerTypes = new HashSet<>();

    @Inject
    public ProviderTypeNavigationPresenter( final Logger logger,
                                            final View view,
                                            final Event<AddNewProviderType> addNewProviderTypeEvent,
                                            final Event<ProviderTypeListRefresh> providerTypeListRefreshEvent,
                                            final Event<ProviderTypeSelected> providerTypeSelectedEvent ) {
        this.logger = logger;
        this.view = view;
        this.addNewProviderTypeEvent = addNewProviderTypeEvent;
        this.providerTypeListRefreshEvent = providerTypeListRefreshEvent;
        this.providerTypeSelectedEvent = providerTypeSelectedEvent;
    }

    @PostConstruct
    public void init() {
        view.init( this );
    }

    public View getView() {
        return view;
    }

    public void setup( final ProviderType firstProvider,
                       final Collection<ProviderType> providerTypes ) {
        view.clean();
        this.providerTypes.clear();
        addProviderType( checkNotNull( "firstProvider", firstProvider ) );
        for ( final ProviderType providerType : providerTypes ) {
            if ( !providerType.equals( firstProvider ) ) {
                addProviderType( providerType );
            }
        }
    }

    private void addProviderType( final ProviderType providerType ) {
        checkNotNull( "providerType", providerType );
        providerTypes.add( providerType );
        this.view.addProviderType( providerType.getId(), providerType.getName(), () -> select( providerType ) );
    }

    public void onSelect( @Observes final ProviderTypeSelected providerTypeSelected ) {
        if ( providerTypeSelected != null &&
                providerTypeSelected.getProviderTypeKey() != null &&
                providerTypeSelected.getProviderTypeKey().getId() != null ) {
            view.select( providerTypeSelected.getProviderTypeKey().getId() );
        } else {
            logger.warn( "Illegal event argument." );
        }
    }

    public void select( final ProviderType providerType ) {
        providerTypeSelectedEvent.fire( new ProviderTypeSelected( providerType ) );
    }

    public void clear() {
        view.clean();
    }

    public void refresh() {
        providerTypeListRefreshEvent.fire( new ProviderTypeListRefresh() );
    }

    public void newProviderType() {
        addNewProviderTypeEvent.fire( new AddNewProviderType() );
    }

}
