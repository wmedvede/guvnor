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

package org.guvnor.ala.ui.client.wizard.providertype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Widget;
import org.guvnor.ala.ui.client.util.ContentChangeHandler;
import org.guvnor.ala.ui.model.ProviderType;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.jboss.errai.ioc.client.container.IOC;
import org.guvnor.ala.ui.client.wizard.providertype.item.ProviderTypeItemPresenter;
import org.guvnor.ala.ui.model.ProviderTypeStatus;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.ext.widgets.core.client.wizards.WizardPage;
import org.uberfire.ext.widgets.core.client.wizards.WizardPageStatusChangeEvent;

import static org.uberfire.commons.validation.PortablePreconditions.*;

public class EnableProviderTypePresenter implements WizardPage {

    public interface View extends UberElement<EnableProviderTypePresenter> {

        void clear( );

        void addProviderType( final IsElement element );

        String getWizardTitle( );

        String getEnableProviderTypeWizardErrorMessage( );

        String getEnableProviderTypeWizardSuccessMessage( );

    }

    private final View view;
    private final Event<WizardPageStatusChangeEvent> wizardPageStatusChangeEvent;
    private ContentChangeHandler changeHandler;

    private Collection<ProviderTypeItemPresenter> providerTypes = new ArrayList<>();

    @Inject
    public EnableProviderTypePresenter( final View view,
                                        final Event<WizardPageStatusChangeEvent> wizardPageStatusChangeEvent ) {
        this.view = view;
        this.wizardPageStatusChangeEvent = wizardPageStatusChangeEvent;
    }

    public void setup( final Map<ProviderType, ProviderTypeStatus> providerTypeStatus ) {
        providerTypes.clear();
        view.clear();

        final ContentChangeHandler contentChangeHandler = () -> {
            changeHandler.onContentChange();
            wizardPageStatusChangeEvent.fire( new WizardPageStatusChangeEvent( EnableProviderTypePresenter.this ) );
        };
        for ( final Map.Entry<ProviderType, ProviderTypeStatus> entry : providerTypeStatus.entrySet() ) {
            final ProviderTypeItemPresenter presenter = IOC.getBeanManager().lookupBean( ProviderTypeItemPresenter.class ).getInstance();
            presenter.setup( entry.getKey(), entry.getValue() );
            presenter.addContentChangeHandler( contentChangeHandler );

            providerTypes.add( presenter );
            view.addProviderType( presenter.getView() );
        }
    }

    @Override
    public void initialise() {
    }

    @Override
    public void prepareView() {

    }

    @Override
    public void isComplete( final Callback<Boolean> callback ) {
        for ( ProviderTypeItemPresenter providerType : providerTypes ) {
            if ( providerType.isSelected() ) {
                callback.callback( true );
                return;
            }
        }
        callback.callback( false );
    }

    @Override
    public String getTitle() {
        return view.getWizardTitle();
    }

    @Override
    public Widget asWidget() {
        return ElementWrapperWidget.getWidget( view.getElement() );
    }

    public void addContentChangeHandler( final ContentChangeHandler contentChangeHandler ) {
        this.changeHandler = checkNotNull( "contentChangeHandler", contentChangeHandler );
    }

    public String getEnableProviderTypeWizardSuccessMessage() {
        return view.getEnableProviderTypeWizardSuccessMessage();
    }

    public String getEnableProviderTypeWizardErrorMessage() {
        return view.getEnableProviderTypeWizardErrorMessage();
    }

    public Collection<ProviderType > getSelectedProviderTypes() {
        final Collection<ProviderType > result = new ArrayList<>();
        for ( ProviderTypeItemPresenter providerType : providerTypes ) {
            if ( providerType.isSelected() ) {
                result.add( providerType.getProviderType() );
            }
        }
        return result;
    }

}
