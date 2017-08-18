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

import org.guvnor.ala.ui.openshift.client.pipeline.template.old.TemplateParamsEditorPresenter;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Dependent
@Templated
public class TemplateParamsEditorView
        implements IsElement,
                   TemplateParamsEditorPresenter.View {

    @Inject
    @DataField("editor-rows-container")
    private Div rowsContaitner;

    private TemplateParamsEditorPresenter presenter;

    @Override
    public void init(final TemplateParamsEditorPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void addRow(final HTMLElement row) {
        rowsContaitner.appendChild(row);
    }
}