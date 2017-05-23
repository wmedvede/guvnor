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

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants;
import org.guvnor.ala.ui.client.widget.CustomGroupItem;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Event;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.ext.widgets.common.client.common.popups.YesNoCancelPopup;
import org.uberfire.mvp.Command;

import static org.guvnor.ala.ui.client.widget.CustomGroupItem.createAnchor;
import static org.jboss.errai.common.client.dom.DOMUtil.removeAllChildren;
import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

@Dependent
@Templated
public class ProviderTypeView implements org.jboss.errai.ui.client.local.api.IsElement,
                                         ProviderTypePresenter.View {

    private ProviderTypePresenter presenter;

    private TranslationService translationService;

    @DataField("current-provider-type-name")
    Element providerType = DOM.createElement("strong");

    @Inject
    @DataField("provider-list-group")
    Div providersListGroup;

    private Map<String, CustomGroupItem> providerItems = new HashMap<>();

    private CustomGroupItem selected = null;

    @Inject
    public ProviderTypeView(final TranslationService translationService) {
        super();
        this.translationService = translationService;
    }

    @Override
    public void init(final ProviderTypePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void clear() {
        removeAllChildren(providersListGroup);
        providerItems.clear();
        selected = null;
        providerType.setInnerText("");
    }

    @Override
    public void setProviderType(final String templateId,
                                final String templateName) {
        providerType.setInnerText(templateName);
        providerItems.clear();
        removeAllChildren(providersListGroup);
    }

    @Override
    public void selectProvider(final String id) {
        select(id,
               providerItems);
    }

    private void select(final Object id,
                        final Map<?, CustomGroupItem> map) {
        checkNotNull("id",
                     id);

        if (selected != null) {
            selected.setActive(false);
            selected.getClassList().remove("active");
        }

        selected = map.get(id);
        selected.setActive(true);
    }

    @Override
    public void addProvider(final String providerId,
                            final String providerName,
                            final Command onSelect) {
        if (providerItems.containsKey(providerId)) {
            return;
        }

        final CustomGroupItem groupItem = createAnchor(providerId,
                                                       IconType.FOLDER_O,
                                                       onSelect);

        providerItems.put(providerId,
                          groupItem);

        providersListGroup.appendChild(groupItem);
    }

    @Override
    public void confirmRemove(final Command command) {
        final YesNoCancelPopup result = YesNoCancelPopup.newYesNoCancelPopup(getTemplateRemovePopupTitle(),
                                                                             getTemplateRemovePopupText(),
                                                                             command,
                                                                             () -> {
                                                                             },
                                                                             null);
        result.clearScrollHeight();
        result.show();
    }

    @EventHandler("add-new-provider")
    public void onAddNewProvider(@ForEvent("click") final Event event) {
        presenter.addNewProvider();
    }

    @EventHandler("remove-current-provider-type")
    public void onRemoveProviderType(@ForEvent("click") final Event event) {
        presenter.removeProviderType();
    }

    private String getTemplateRemovePopupText() {
        return translationService.format(GuvnorAlaUIConstants.ProviderTypeView_ProviderTypeRemovePopupText);
    }

    private String getTemplateRemovePopupTitle() {
        return translationService.format(GuvnorAlaUIConstants.ProviderTypeView_ProviderTypeRemovePopupTitle);
    }
}
