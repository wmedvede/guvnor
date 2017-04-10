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

import com.google.gwt.user.client.Event;
import org.jboss.errai.common.client.dom.Anchor;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.ListItem;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.SinkNative;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.ext.widgets.common.client.common.popups.YesNoCancelPopup;
import org.uberfire.mvp.Command;

import static org.jboss.errai.common.client.dom.DOMUtil.*;

@Dependent
@Templated
public class ProviderView implements IsElement,
                                     ProviderPresenter.View {

    private ProviderPresenter presenter;

    private TranslationService translationService;

    @Inject
    @DataField("provider-name")
    Span providerName;

    @Inject
    @DataField("status-tab")
    ListItem statusTab;

    @Inject
    @DataField("status-tab-link")
    Anchor statusTabLink;

    @Inject
    @DataField("config-tab")
    ListItem configTab;

    @Inject
    @DataField("config-tab-link")
    Anchor configTabLink;

    @Inject
    @DataField("status-pane")
    Div statusPane;

    @Inject
    @DataField("status-content")
    Div statusContent;

    @Inject
    @DataField("config-pane")
    Div rulesPane;

    @Inject
    @DataField("config-content")
    Div configContent;

    @Override
    public void init( final ProviderPresenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void confirmRemove( final Command command ) {
        final YesNoCancelPopup result = YesNoCancelPopup.newYesNoCancelPopup( "Remove Provider",
                                                                              "Are you sure you want remove the current provider?",
                                                                              command,
                                                                              () -> {
                                                                              }, null );
        result.clearScrollHeight();
        result.show();
    }

    @Override
    public void setProviderName( final String name ) {
        providerName.setTextContent( name );
    }

    @Override
    public void setStatus( final org.jboss.errai.common.client.api.IsElement view ) {
        removeAllChildren( statusContent );
        statusContent.appendChild( view.getElement() );
    }

    @Override
    public void setConfig( final org.jboss.errai.common.client.api.IsElement view ) {
        removeAllChildren( configContent );
        configContent.appendChild( view.getElement() );
    }

    @Override
    public String getRemoveProviderSuccessMessage() {
        return "Provider deleted.";
    }

    @Override
    public String getRemoveProviderErrorMessage() {
        return "Failed to delete the provider.";
    }

    @SinkNative(Event.ONCLICK)
    @EventHandler("refresh-provider")
    public void onRefresh( final Event event ) {
        presenter.refresh();
    }

    @SinkNative(Event.ONCLICK)
    @EventHandler("remove-provider")
    public void onRemove( final Event event ) {
        presenter.removeProvider();
    }

    @SinkNative(Event.ONCLICK)
    @EventHandler("deploy")
    public void onDeploy( final Event event ) {
        presenter.deploy();
    }

}
