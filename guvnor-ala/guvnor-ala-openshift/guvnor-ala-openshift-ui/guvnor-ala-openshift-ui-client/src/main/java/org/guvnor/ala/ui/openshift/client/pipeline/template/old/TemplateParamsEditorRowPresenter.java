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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.guvnor.ala.ui.openshift.model.TemplateParam;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.uberfire.client.mvp.UberElement;

public class TemplateParamsEditorRowPresenter
        implements IsElement {

    public interface View
            extends UberElement<TemplateParamsEditorRowPresenter> {

        void setLabel(final String label);

        void setValue(final String value);

        String getValue();
    }

    private final View view;

    private TemplateParam templateParam;

    private TemplateParamValueChangeHandler valueChangeHandler;

    private String oldValue;

    @Inject
    public TemplateParamsEditorRowPresenter(View view) {
        this.view = view;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    public void init(final TemplateParam templateParam) {
        this.templateParam = templateParam;
        view.setLabel(templateParam.getName());
        if (templateParam.getValue() != null) {
            view.setValue(templateParam.getValue());
            oldValue = templateParam.getValue();
        } else if (templateParam.getDefaultValue() != null) {
            view.setValue(templateParam.getDefaultValue());
            oldValue = templateParam.getDefaultValue();
        }
        oldValue = null;
    }

    public void setValueChangeHandler(TemplateParamValueChangeHandler valueChangeHandler) {
        this.valueChangeHandler = valueChangeHandler;
    }

    @Override
    public HTMLElement getElement() {
        return view.getElement();
    }

    protected void onValueChange() {
        valueChangeHandler.onValueChange(templateParam.getName(),
                                         view.getValue(),
                                         oldValue);
        oldValue = view.getValue();
    }
}
