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

package org.guvnor.ala.ui.client.wizard.providertype.item;

import java.util.ArrayList;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.user.client.Event;
import org.guvnor.ala.ui.client.util.ContentChangeHandler;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Heading;
import org.jboss.errai.common.client.dom.Image;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.SinkNative;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Dependent
@Templated
public class ProviderTypeItemView implements IsElement,
                                             ProviderTypeItemPresenter.View {

    private ProviderTypeItemPresenter presenter;

    @Inject
    @DataField("accented-area")
    Div accentedArea;

    @Inject
    @Named("h2")
    @DataField("type-name")
    Heading typeName;

    @Inject
    @DataField
    Div body;

    @Inject
    @DataField
    Image image;

    private final ArrayList<ContentChangeHandler > changeHandlers = new ArrayList<ContentChangeHandler>();

    @Override
    public void init( final ProviderTypeItemPresenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void addContentChangeHandler( final ContentChangeHandler contentChangeHandler ) {
        changeHandlers.add( contentChangeHandler );
    }

    @Override
    public void setProviderTypeName( final String name ) {
        this.typeName.setTextContent( name );
    }

    @Override
    public void setImage( final String imageURL ) {
        this.image.setSrc( imageURL );
    }

    @Override
    public void disable() {
        accentedArea.getClassList().add( "remove-option" );
    }

    @SinkNative(Event.ONCLICK)
    @EventHandler("image")
    public void onClick( final Event event ) {
        if ( !accentedArea.getClassList().contains( "remove-option" ) ) {
            accentedArea.getClassList().toggle( "card-pf-accented" );
            if ( accentedArea.getClassList().contains( "card-pf-accented" ) ) {
                removeOpacity();
            } else {
                addOpacity();
            }
            fireChangeHandlers();
        }
    }

    @Override
    public boolean isSelected() {
        return accentedArea.getClassList().contains( "card-pf-accented" );
    }

    private void addOpacity() {
        body.getStyle().setProperty( "opacity", "0.3" );
    }

    private void removeOpacity() {
        body.getStyle().removeProperty( "opacity" );
    }

    private void fireChangeHandlers() {
        for ( final ContentChangeHandler changeHandler : changeHandlers ) {
            changeHandler.onContentChange();
        }
    }

}