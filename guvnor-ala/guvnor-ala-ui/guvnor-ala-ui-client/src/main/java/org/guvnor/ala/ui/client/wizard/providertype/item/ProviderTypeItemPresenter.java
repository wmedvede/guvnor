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

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.util.ContentChangeHandler;
import org.jboss.errai.common.client.api.IsElement;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.ProviderTypeStatus;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.ext.widgets.core.client.wizards.WizardPageStatusChangeEvent;

import static org.uberfire.commons.validation.PortablePreconditions.*;

@Dependent
public class ProviderTypeItemPresenter {

    public interface View extends UberElement<ProviderTypeItemPresenter> {

        void disable( );

        boolean isSelected( );

        void addContentChangeHandler( final ContentChangeHandler contentChangeHandler );

        void setProviderTypeName( String name );

        void setImage( String imageURL );
    }

    private final View view;
    private final Event<WizardPageStatusChangeEvent> wizardPageStatusChangeEvent;
    private ContentChangeHandler changeHandler;

    private ProviderType type;

    @Inject
    public ProviderTypeItemPresenter( final View view,
                                      final Event<WizardPageStatusChangeEvent> wizardPageStatusChangeEvent ) {
        this.view = view;
        this.wizardPageStatusChangeEvent = wizardPageStatusChangeEvent;
    }

    public void addContentChangeHandler( final ContentChangeHandler contentChangeHandler ) {
        this.changeHandler = checkNotNull( "contentChangeHandler", contentChangeHandler );
        if ( view != null ) {
            view.addContentChangeHandler( changeHandler );
        }
    }

    public void setup( final ProviderType type,
                       final ProviderTypeStatus status ) {
        this.type = type;
        view.setProviderTypeName( type.getName() );
        view.setImage( type.getImageURL() );
        if ( status.equals( ProviderTypeStatus.ENABLED ) ) {
            view.disable();
        }
    }

    public ProviderType getProviderType() {
        return type;
    }

    public boolean isSelected() {
        return view.isSelected();
    }

    public IsElement getView() {
        return view;
    }
}
