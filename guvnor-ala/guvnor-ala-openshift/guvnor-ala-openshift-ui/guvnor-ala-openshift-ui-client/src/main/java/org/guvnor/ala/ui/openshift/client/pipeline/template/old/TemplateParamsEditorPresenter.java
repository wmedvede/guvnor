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

import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.guvnor.ala.ui.openshift.model.TemplateParam;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.uberfire.client.mvp.UberElement;

@Dependent
public class TemplateParamsEditorPresenter
        implements IsElement {

    public interface View
            extends UberElement<TemplateParamsEditorPresenter> {

        void addRow(HTMLElement row);
    }

    final private View view;

    final private ManagedInstance<TemplateParamsEditorRowPresenter> paramsEditorRowInstance;

    private TemplateParamValueChangeHandler valueChangeHandler;

    @Inject
    public TemplateParamsEditorPresenter(final View view,
                                         final ManagedInstance<TemplateParamsEditorRowPresenter> paramsEditorRowInstance) {
        this.view = view;
        this.paramsEditorRowInstance = paramsEditorRowInstance;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    public void setup(final List<TemplateParam> params) {
        params.forEach(param -> {
            TemplateParamsEditorRowPresenter rowPresenter = newRow();
            rowPresenter.init(param);
            rowPresenter.setValueChangeHandler(this::onValueChange);
            view.addRow(rowPresenter.getElement());
        });
    }

    public void setValueChangeHandler(final TemplateParamValueChangeHandler valueChangeHandler) {
        this.valueChangeHandler = valueChangeHandler;
    }

    @Override
    public HTMLElement getElement() {
        return view.getElement();
    }

    protected TemplateParamsEditorRowPresenter newRow() {
        return paramsEditorRowInstance.get();
    }

    protected void onValueChange(final String paramName,
                                 final String newValue,
                                 final String oldValue) {
        if (valueChangeHandler != null) {
            valueChangeHandler.onValueChange(paramName,
                                             newValue,
                                             oldValue);
        }
    }
}
