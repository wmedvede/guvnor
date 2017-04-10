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

package org.guvnor.ala.ui.client.wizard.provider;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import static org.jboss.errai.common.client.dom.DOMUtil.*;

@Dependent
@Templated
public class NewProviderFormView implements IsElement,
                                            NewProviderFormPresenter.View {

    private NewProviderFormPresenter presenter;

    @Inject
    @DataField("content")
    Div content;

    @Override
    public void init( final NewProviderFormPresenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void setForm( final org.jboss.errai.common.client.api.IsElement element ) {
        removeAllChildren( content );
        content.appendChild( element.getElement() );
    }

    @Override
    public String getNewPoviderCreateErrorMessage() {
        return "Error to create new Provider.";
    }

    @Override
    public String getNewProviderWizardSuccessMessage() {
        return "Provider created successfully!";
    }
}