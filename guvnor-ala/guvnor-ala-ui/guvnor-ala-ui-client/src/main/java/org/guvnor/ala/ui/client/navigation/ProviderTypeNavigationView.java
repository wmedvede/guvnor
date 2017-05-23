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

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants;
import org.guvnor.ala.ui.client.widget.CustomGroupItem;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Event;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.mvp.Command;

import static org.jboss.errai.common.client.dom.DOMUtil.removeAllChildren;

@Dependent
@Templated
public class ProviderTypeNavigationView implements IsElement,
                                                   ProviderTypeNavigationPresenter.View {

    private ProviderTypeNavigationPresenter presenter;

    private TranslationService translationService;

    @DataField
    private Element title = DOM.createElement("strong");

    @Inject
    @DataField("provider-type-list-group")
    private Div providerTypeItems;

    private final Map<String, CustomGroupItem> idItem = new HashMap<>();

    private CustomGroupItem selected = null;

    @Inject
    public ProviderTypeNavigationView(final TranslationService translationService) {
        super();
        this.translationService = translationService;
    }

    @Override
    public void init(final ProviderTypeNavigationPresenter presenter) {
        this.presenter = presenter;
    }

    @PostConstruct
    public void init() {
        title.setInnerText(getTitleText());
    }

    @EventHandler("enable-provider-type-button")
    public void onAddProviderType(@ForEvent("click") final Event event) {
        presenter.newProviderType();
    }

    @EventHandler("refresh-provider-type-list-icon")
    public void onRefresh(@ForEvent("click") final Event event) {
        presenter.refresh();
    }

    @Override
    public void addProviderType(final String id,
                                final String name,
                                final Command select) {
        final CustomGroupItem providerTypeItem = CustomGroupItem.createAnchor(name,
                                                                              IconType.FOLDER_O,
                                                                              select);

        idItem.put(id,
                   providerTypeItem);

        providerTypeItems.appendChild(providerTypeItem);
    }

    @Override
    public void select(final String id) {
        if (selected != null) {
            selected.setActive(false);
            selected.getClassList().remove("active");
        }
        selected = idItem.get(id);
        selected.setActive(true);
    }

    @Override
    public void clean() {
        removeAllChildren(providerTypeItems);
        selected = null;
        removeAllChildren(providerTypeItems);
    }

    private String getTitleText() {
        return translationService.format(GuvnorAlaUIConstants.ProviderTypeNavigationView_TitleText);
    }
}
