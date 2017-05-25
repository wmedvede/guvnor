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
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.events.AddNewProvider;
import org.guvnor.ala.ui.client.events.ProviderSelectedEvent;
import org.guvnor.ala.ui.client.events.ProviderTypeListRefreshEvent;
import org.guvnor.ala.ui.model.ProviderKey;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.service.ProviderTypeService;
import org.jboss.errai.common.client.api.Caller;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.mvp.Command;

@ApplicationScoped
public class ProviderTypePresenter {

    public interface View
            extends UberElement<ProviderTypePresenter> {

        void clear();

        void setProviderType(final String providerTypeId,
                             final String providerTypeName);

        void select(final String providerId);

        void addProvider(final String providerId,
                         final String providerName,
                         final Command onSelect);

        void confirmRemove(Command command);
    }

    private View view;
    private Caller<ProviderTypeService> providerTypeService;

    private Event<AddNewProvider> addNewProviderEvent;
    private Event<ProviderTypeListRefreshEvent> providerTypeListRefreshEvent;
    private Event<ProviderSelectedEvent> providerSelectedEvent;

    private ProviderType providerType;

    public ProviderTypePresenter() {
    }

    @Inject
    public ProviderTypePresenter(final View view,
                                 final Caller<ProviderTypeService> providerTypeService,
                                 final Event<AddNewProvider> addNewProviderEvent,
                                 final Event<ProviderTypeListRefreshEvent> providerTypeListRefreshEvent,
                                 final Event<ProviderSelectedEvent> providerSelectedEvent) {
        this.view = view;
        this.providerTypeService = providerTypeService;
        this.addNewProviderEvent = addNewProviderEvent;
        this.providerTypeListRefreshEvent = providerTypeListRefreshEvent;
        this.providerSelectedEvent = providerSelectedEvent;
        this.view.init(this);
    }

    public View getView() {
        return view;
    }

    public void setup(final ProviderType providerType,
                      final Collection<ProviderKey> providers,
                      final ProviderKey firstProviderKey) {
        view.clear();
        this.providerType = providerType;
        view.setProviderType(providerType.getKey().getId(),
                             providerType.getName());

        if (firstProviderKey != null) {
            addProvider(firstProviderKey);
            providers.stream()
                    .filter(providerKey -> !providerKey.equals(firstProviderKey))
                    .forEach(this::addProvider);
            providerSelectedEvent.fire(new ProviderSelectedEvent(firstProviderKey));
        }
    }

    private void addProvider(final ProviderKey provider) {
        view.addProvider(provider.getId(),
                         provider.getId(),
                         () -> providerSelectedEvent.fire(new ProviderSelectedEvent(provider)));
    }

    public void onProviderSelect(@Observes final ProviderSelectedEvent providerSelectedEvent) {
        if (providerSelectedEvent.getProviderKey() != null &&
                providerSelectedEvent.getProviderKey().getId() != null &&
                providerSelectedEvent.getProviderKey().getProviderTypeKey() != null &&
                providerSelectedEvent.getProviderKey().getProviderTypeKey().equals(providerType.getKey())) {
            view.select(providerSelectedEvent.getProviderKey().getId());
        }
    }

    public void onAddNewProvider() {
        addNewProviderEvent.fire(new AddNewProvider(providerType));
    }

    public void onRemoveProviderType() {
        view.confirmRemove(
                () -> providerTypeService.call(
                        none -> providerTypeListRefreshEvent.fire(new ProviderTypeListRefreshEvent()))
                        .disableProvider(providerType)
        );
    }
}
