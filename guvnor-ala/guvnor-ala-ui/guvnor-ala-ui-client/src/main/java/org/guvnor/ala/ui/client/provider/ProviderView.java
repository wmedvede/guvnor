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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Anchor;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Event;
import org.jboss.errai.common.client.dom.ListItem;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.ext.widgets.common.client.common.popups.YesNoCancelPopup;
import org.uberfire.mvp.Command;

import static org.jboss.errai.common.client.dom.DOMUtil.*;

@Dependent
@Templated
public class ProviderView
        implements IsElement,
                   ProviderPresenter.View {

    @Inject
    @DataField("provider-name")
    private Span providerName;

    @Inject
    @DataField("status-tab")
    private ListItem statusTab;

    @Inject
    @DataField("status-tab-link")
    private Anchor statusTabLink;

    @Inject
    @DataField("config-tab")
    private ListItem configTab;

    @Inject
    @DataField("config-tab-link")
    private Anchor configTabLink;

    @Inject
    @DataField("status-pane")
    private Div statusPane;

    @Inject
    @DataField("status-content")
    private Div statusContent;

    @Inject
    @DataField("config-pane")
    private Div rulesPane;

    @Inject
    @DataField("config-content")
    private Div configContent;

    @Inject
    private TranslationService translationService;

    private ProviderPresenter presenter;

    public ProviderView() {
    }

    @Override
    public void init(final ProviderPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void confirmRemove(final Command command) {
        //TODO i18n
        final YesNoCancelPopup result = YesNoCancelPopup.newYesNoCancelPopup("Remove Provider",
                                                                             "Are you sure you want remove the current provider?",
                                                                             command,
                                                                             () -> {
                                                                             },
                                                                             null);
        result.clearScrollHeight();
        result.show();
    }

    @Override
    public void setProviderName(final String name) {
        providerName.setTextContent(name);
    }

    @Override
    public void setStatus(final org.jboss.errai.common.client.api.IsElement view) {
        removeAllChildren(statusContent);
        statusContent.appendChild(view.getElement());
    }

    @Override
    public void setConfig(final org.jboss.errai.common.client.api.IsElement view) {
        removeAllChildren(configContent);
        configContent.appendChild(view.getElement());
    }

    @Override
    public String getRemoveProviderSuccessMessage() {
        return "Provider deleted.";
    }

    @Override
    public String getRemoveProviderErrorMessage() {
        return "Failed to delete the provider.";
    }

    @EventHandler("refresh-provider")
    public void onRefresh(@ForEvent("click") final Event event) {
        presenter.refresh();
    }

    @EventHandler("remove-provider")
    public void onRemove(@ForEvent("click") final Event event) {
        presenter.removeProvider();
    }

    @EventHandler("deploy")
    public void onDeploy(@ForEvent("click") final Event event) {
        presenter.deploy();
    }
}
