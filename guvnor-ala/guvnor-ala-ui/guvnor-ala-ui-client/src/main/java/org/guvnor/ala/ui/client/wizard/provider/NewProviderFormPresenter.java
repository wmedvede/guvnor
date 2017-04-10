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

import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Widget;
import org.guvnor.ala.ui.client.util.ContentChangeHandler;
import org.guvnor.ala.ui.client.widget.provider.FormProvider;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.guvnor.ala.ui.model.ProviderConfiguration;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.ext.widgets.core.client.wizards.WizardPage;
import org.uberfire.ext.widgets.core.client.wizards.WizardPageStatusChangeEvent;

import static org.uberfire.commons.validation.PortablePreconditions.*;

public class NewProviderFormPresenter implements WizardPage {

    public interface View extends UberElement<NewProviderFormPresenter> {

        void setForm( final IsElement element );

        String getNewPoviderCreateErrorMessage( );

        String getNewProviderWizardSuccessMessage( );
    }

    private final View view;
    private final Event<WizardPageStatusChangeEvent> wizardPageStatusChangeEvent;
    private ContentChangeHandler changeHandler;

    private FormProvider formProvider;

    @Inject
    public NewProviderFormPresenter( final View view,
                                     final Event<WizardPageStatusChangeEvent> wizardPageStatusChangeEvent ) {
        this.view = view;
        this.wizardPageStatusChangeEvent = wizardPageStatusChangeEvent;
    }

    public void setFormProvider( final FormProvider formProvider ) {
        this.formProvider = formProvider;
        this.view.setForm( formProvider.getView() );
        formProvider.addContentChangeHandler( () -> {
            changeHandler.onContentChange();
            wizardPageStatusChangeEvent.fire( new WizardPageStatusChangeEvent( NewProviderFormPresenter.this ) );
        } );
    }

    @Override
    public void initialise() {
    }

    @Override
    public void prepareView() {

    }

    @Override
    public void isComplete( final Callback<Boolean> callback ) {
        formProvider.isValid( callback );
    }

    @Override
    public String getTitle() {
        return formProvider.getWizardTitle();
    }

    @Override
    public Widget asWidget() {
        return ElementWrapperWidget.getWidget( view.getElement() );
    }

    public void clear() {
        if ( formProvider != null ) {
            formProvider.clear();
        }
    }

    public void addContentChangeHandler( final ContentChangeHandler contentChangeHandler ) {
        this.changeHandler = checkNotNull( "contentChangeHandler", contentChangeHandler );
        if ( formProvider != null ) {
            formProvider.addContentChangeHandler( () -> {
                changeHandler.onContentChange();
                wizardPageStatusChangeEvent.fire( new WizardPageStatusChangeEvent( NewProviderFormPresenter.this ) );
            } );
        }
    }

    public ProviderConfiguration buildProviderConfiguration() {
        return formProvider.buildProviderConfiguration();
    }

    public String getNewProviderWizardSuccessMessage() {
        return view.getNewProviderWizardSuccessMessage();
    }

    public String getNewPoviderCreateErrorMessage() {
        return view.getNewPoviderCreateErrorMessage();
    }

}
