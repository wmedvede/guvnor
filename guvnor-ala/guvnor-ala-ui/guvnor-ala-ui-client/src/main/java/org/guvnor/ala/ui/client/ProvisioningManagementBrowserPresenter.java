/*
 * Copyright ${year} Red Hat, Inc. and/or its affiliates.
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
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.empty.ProviderTypeEmptyPresenter;
import org.guvnor.ala.ui.client.events.ProviderSelected;
import org.guvnor.ala.ui.client.events.ProviderTypeDeleted;
import org.guvnor.ala.ui.client.events.ProviderTypeListRefresh;
import org.guvnor.ala.ui.client.events.ProviderTypeSelected;
import org.guvnor.ala.ui.client.navigation.ProviderTypeNavigationPresenter;
import org.guvnor.ala.ui.client.navigation.providertype.ProviderTypePresenter;
import org.guvnor.ala.ui.client.provider.ProviderPresenter;
import org.guvnor.ala.ui.client.provider.empty.ProviderEmptyPresenter;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.ProviderTypeKey;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.guvnor.ala.ui.model.ProviderKey;
import org.guvnor.ala.ui.service.ProviderService;
import org.guvnor.ala.ui.service.ProviderTypeService;
import org.slf4j.Logger;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.lifecycle.OnOpen;

@ApplicationScoped
@WorkbenchScreen(identifier = "ProvisioningManagementBrowser")
public class ProvisioningManagementBrowserPresenter {

    public interface View extends IsElement {

        void setNavigation( final ProviderTypeNavigationPresenter.View view );

        void setProviderType( final ProviderTypePresenter.View view );

        void setEmptyView( final ProviderTypeEmptyPresenter.View view );

        void setContent( final IsElement view );
    }

    private final Logger logger;

    private final ProvisioningManagementBrowserPresenter.View view;

    private final ProviderTypeNavigationPresenter providerTypeNavigationPresenter;

    private final ProviderTypePresenter providerTypePresenter;

    private final ProviderTypeEmptyPresenter providerTypeEmptyPresenter;

    private final ProviderEmptyPresenter providerEmptyPresenter;

    private final ProviderPresenter providerPresenter;

    private final Caller<ProviderTypeService> providerTypeService;

    private final Caller<ProviderService> providerService;

    private final Event<ProviderTypeSelected > providerTypeSelectedEvent;

    private boolean isEmpty = true;

    @Inject
    public ProvisioningManagementBrowserPresenter( final Logger logger,
                                                   final ProvisioningManagementBrowserPresenter.View view,
                                                   final ProviderTypeNavigationPresenter providerTypeNavigationPresenter,
                                                   final ProviderTypePresenter providerTypePresenter,
                                                   final ProviderTypeEmptyPresenter providerTypeEmptyPresenter,
                                                   final ProviderEmptyPresenter providerEmptyPresenter,
                                                   final ProviderPresenter providerPresenter,
                                                   final Caller<ProviderTypeService> providerTypeService,
                                                   final Caller<ProviderService> providerService,
                                                   final Event<ProviderTypeSelected> providerTypeSelectedEvent ) {
        this.logger = logger;
        this.view = view;
        this.providerTypeNavigationPresenter = providerTypeNavigationPresenter;
        this.providerTypePresenter = providerTypePresenter;
        this.providerTypeEmptyPresenter = providerTypeEmptyPresenter;
        this.providerEmptyPresenter = providerEmptyPresenter;
        this.providerPresenter = providerPresenter;
        this.providerTypeService = providerTypeService;
        this.providerService = providerService;
        this.providerTypeSelectedEvent = providerTypeSelectedEvent;
    }

    @PostConstruct
    public void init() {
        this.view.setNavigation( providerTypeNavigationPresenter.getView() );
    }

    @OnOpen
    public void onOpen() {
        refreshList( new ProviderTypeListRefresh() );
    }

    public void onProviderTypeDeleted( @Observes final ProviderTypeDeleted providerTypeDeleted ) {
        if ( providerTypeDeleted != null ) {
            refreshList( new ProviderTypeListRefresh() );
        } else {
            logger.warn( "Illegal event argument." );
        }
    }

    private void refreshList( @Observes final ProviderTypeListRefresh refresh ) {
        providerTypeService.call( (RemoteCallback<Collection<ProviderType >>) providerTypes ->
                setup( providerTypes, refresh.getProviderTypeKey() )
        ).getEnabledProviderTypes();
    }

    public void onSelected( @Observes final ProviderTypeSelected providerTypeSelected ) {
        if ( providerTypeSelected != null &&
                providerTypeSelected.getProviderTypeKey() != null ) {
            selectProviderType( providerTypeSelected.getProviderTypeKey(), providerTypeSelected.getProviderId() );
        } else {
            logger.warn( "Illegal event argument." );
        }
    }

    public void onSelected( @Observes final ProviderSelected providerSelected ) {
        if ( providerSelected != null &&
                providerSelected.getProviderKey() != null ) {
            this.view.setContent( providerPresenter.getView() );
        } else {
            logger.warn( "Illegal event argument." );
        }
    }

    private void selectProviderType( final ProviderTypeKey providerTypeKey,
                                     final String providerId ) {
        providerTypeService.call( (RemoteCallback<ProviderType >) providerType -> {
            providerService.call( (RemoteCallback<Collection<ProviderKey>>) providers ->
                    setup( providerType, providers, providerId )
            ).getProvidersKey( providerType );
        } ).getProviderType( providerTypeKey );
    }

    public void setup( final Collection<ProviderType > providerTypes,
                       final ProviderTypeKey selectProviderKey ) {
        if ( providerTypes.isEmpty() ) {
            isEmpty = true;
            this.view.setEmptyView( providerTypeEmptyPresenter.getView() );
            providerTypeNavigationPresenter.clear();
        } else {
            isEmpty = false;
            ProviderType providerType2BeSelected = null;
            if ( selectProviderKey != null ) {
                for ( final ProviderType providerType : providerTypes ) {
                    if ( providerType.getKey().equals( selectProviderKey ) ) {
                        providerType2BeSelected = providerType;
                        break;
                    }
                }
            }
            if ( providerType2BeSelected == null ) {
                providerType2BeSelected = providerTypes.iterator().next();
            }
            providerTypeNavigationPresenter.setup( providerType2BeSelected, providerTypes );
            providerTypeSelectedEvent.fire( new ProviderTypeSelected( providerType2BeSelected.getKey() ) );
        }
    }

    private void setup( final ProviderType providerType,
                        final Collection<ProviderKey> providers,
                        final String selectProviderId ) {
        ProviderKey provider2BeSelected = null;
        this.view.setProviderType( providerTypePresenter.getView() );
        if ( providers.isEmpty() ) {
            providerEmptyPresenter.setProviderType( providerType );
            this.view.setContent( providerEmptyPresenter.getView() );
        } else {
            if ( selectProviderId != null ) {
                for ( ProviderKey provider : providers ) {
                    if ( provider.getId().equals( selectProviderId ) ) {
                        provider2BeSelected = provider;
                        break;
                    }
                }
            }
            if ( provider2BeSelected == null ) {
                provider2BeSelected = providers.iterator().next();
            }

        }
        providerTypePresenter.setup( providerType, providers, provider2BeSelected );
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "Server Management Browser";
    }

    @WorkbenchPartView
    public IsElement getView() {
        return view;
    }

}
