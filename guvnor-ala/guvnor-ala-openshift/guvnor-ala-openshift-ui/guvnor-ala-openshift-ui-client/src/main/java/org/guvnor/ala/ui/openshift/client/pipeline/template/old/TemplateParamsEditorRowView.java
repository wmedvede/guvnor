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

package org.guvnor.ala.ui.openshift.client.pipeline.template.old;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.guvnor.ala.ui.openshift.client.pipeline.template.old.TemplateParamsEditorRowPresenter;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Event;
import org.jboss.errai.common.client.dom.Label;
import org.jboss.errai.common.client.dom.TextInput;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Dependent
@Templated
public class TemplateParamsEditorRowView
        implements IsElement,
        TemplateParamsEditorRowPresenter.View {

    @Inject
    @DataField("editor-row-form")
    private Div editorRowForm;

    @Inject
    @DataField("editor-row-label")
    private Label editorRowLabel;

    @Inject
    @DataField("editor-row-input")
    private TextInput editorRowInput;

    private TemplateParamsEditorRowPresenter presenter;

    @Override
    public void init(final TemplateParamsEditorRowPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setLabel(final String label) {
        editorRowLabel.setTextContent(label);
    }

    @Override
    public void setValue(final String value) {
        editorRowInput.setValue(value);
    }

    @Override
    public String getValue() {
        return editorRowInput.getValue();
    }

    @EventHandler("editor-row-input")
    private void onValueChange(@ForEvent("change") final Event event) {
        presenter.onValueChange();
    }
}