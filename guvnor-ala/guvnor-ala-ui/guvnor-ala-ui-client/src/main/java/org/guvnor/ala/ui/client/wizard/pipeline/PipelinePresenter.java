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

import java.util.ArrayList;
import java.util.Collection;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Widget;
import org.guvnor.ala.ui.client.util.ContentChangeHandler;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.jboss.errai.ioc.client.container.IOC;
import org.guvnor.ala.ui.client.wizard.pipeline.item.PipelineItemPresenter;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.ext.widgets.core.client.wizards.WizardPage;
import org.uberfire.ext.widgets.core.client.wizards.WizardPageStatusChangeEvent;

import static org.uberfire.commons.validation.PortablePreconditions.*;

public class PipelinePresenter implements WizardPage {

    public interface View extends UberElement<PipelinePresenter> {

        void clear( );

        void addPipelineItem( final IsElement element );

        String getWizardTitle( );
    }

    private View view;
    private Event<WizardPageStatusChangeEvent> wizardPageStatusChangeEvent;
    private ContentChangeHandler changeHandler;

    private Collection<PipelineItemPresenter> pipelineItemPresenters = new ArrayList<>();

    public PipelinePresenter() {
    }

    @Inject
    public PipelinePresenter( final View view,
                              final Event<WizardPageStatusChangeEvent> wizardPageStatusChangeEvent ) {
        this.view = view;
        this.wizardPageStatusChangeEvent = wizardPageStatusChangeEvent;
    }

    public void setup( final Collection<String> pipelines ) {
        pipelineItemPresenters.clear();
        view.clear();

        final ContentChangeHandler contentChangeHandler = () -> {
            changeHandler.onContentChange();
            wizardPageStatusChangeEvent.fire( new WizardPageStatusChangeEvent( PipelinePresenter.this ) );
        };
        for ( String pipeline : pipelines ) {
            final PipelineItemPresenter presenter = IOC.getBeanManager().lookupBean( PipelineItemPresenter.class ).getInstance();
            presenter.setup( pipeline );
            presenter.addContentChangeHandler( contentChangeHandler );

            pipelineItemPresenters.add( presenter );
            view.addPipelineItem( presenter.getView() );
        }
        for ( PipelineItemPresenter pipelineItemPresenter : pipelineItemPresenters ) {
            pipelineItemPresenter.addOthers( pipelineItemPresenters );
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
        for ( PipelineItemPresenter providerType : pipelineItemPresenters ) {
            if ( providerType.isSelected() ) {
                callback.callback( true );
                return;
            }
        }
        callback.callback( false );
    }

    public void clear() {

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

    public String getPipeline() {
        for ( PipelineItemPresenter itemPresenter : pipelineItemPresenters ) {
            if ( itemPresenter.isSelected() ) {
                return itemPresenter.getPipeline();
            }
        }
        return null;
    }

}
