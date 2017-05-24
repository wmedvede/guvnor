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

package org.guvnor.ala.ui.client.wizard.pipeline;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import static org.jboss.errai.common.client.dom.DOMUtil.*;

@Dependent
@Templated
public class PipelineView implements IsElement,
                                     PipelinePresenter.View {

    private PipelinePresenter presenter;

    @Inject
    @DataField
    Div container;

    @Override
    public void init( final PipelinePresenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void clear() {
        for ( int i = 0; i < container.getChildNodes().getLength(); i++ ) {
            container.removeChild( container.getChildNodes().item( i ) );
        }
        removeAllChildren( container );
    }

    @Override
    public void addPipelineItem( final org.jboss.errai.common.client.api.IsElement element ) {
        container.appendChild( element.getElement() );
    }

    @Override
    public String getWizardTitle() {
        return "Select Pipeline";
    }

}