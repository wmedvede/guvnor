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
import org.guvnor.ala.ui.client.wizard.pipeline.item.PipelineItemPresenter;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.ext.widgets.core.client.wizards.WizardPage;
import org.uberfire.ext.widgets.core.client.wizards.WizardPageStatusChangeEvent;

public class SelectPipelinePagePresenter
        implements WizardPage {

    public interface View extends UberElement<SelectPipelinePagePresenter> {

        void clear();

        void addPipelineItem(final IsElement element);

        String getTitle();
    }

    private View view;
    private Event<WizardPageStatusChangeEvent> wizardPageStatusChangeEvent;
    private ManagedInstance<PipelineItemPresenter> pipelineItemPresenterInstance;

    private Collection<PipelineItemPresenter> pipelineItemPresenters = new ArrayList<>();

    public SelectPipelinePagePresenter() {
    }

    @Inject
    public SelectPipelinePagePresenter(final View view,
                                       final Event<WizardPageStatusChangeEvent> wizardPageStatusChangeEvent,
                                       final ManagedInstance<PipelineItemPresenter> pipelineItemPresenterInstance) {
        this.view = view;
        this.wizardPageStatusChangeEvent = wizardPageStatusChangeEvent;
        this.pipelineItemPresenterInstance = pipelineItemPresenterInstance;
        this.view.init(this);
    }

    public void setup(final Collection<String> pipelines) {
        clear();
        final ContentChangeHandler contentChangeHandler = this::onContentChange;
        for (String pipeline : pipelines) {
            final PipelineItemPresenter presenter = newItemPresenter();
            presenter.setup(pipeline);
            presenter.addContentChangeHandler(contentChangeHandler);

            pipelineItemPresenters.add(presenter);
            view.addPipelineItem(presenter.getView());
        }
        for (PipelineItemPresenter pipelineItemPresenter : pipelineItemPresenters) {
            pipelineItemPresenter.addOthers(pipelineItemPresenters);
        }
    }

    @Override
    public void initialise() {
    }

    @Override
    public void prepareView() {

    }

    @Override
    public void isComplete(final Callback<Boolean> callback) {
        for (PipelineItemPresenter providerType : pipelineItemPresenters) {
            if (providerType.isSelected()) {
                callback.callback(true);
                return;
            }
        }
        callback.callback(false);
    }

    public void clear() {
        view.clear();
        clearItemPresenters();
    }

    @Override
    public String getTitle() {
        return view.getTitle();
    }

    @Override
    public Widget asWidget() {
        return ElementWrapperWidget.getWidget(view.getElement());
    }

    public String getPipeline() {
        for (PipelineItemPresenter itemPresenter : pipelineItemPresenters) {
            if (itemPresenter.isSelected()) {
                return itemPresenter.getPipeline();
            }
        }
        return null;
    }

    private void onContentChange() {
        wizardPageStatusChangeEvent.fire(new WizardPageStatusChangeEvent(SelectPipelinePagePresenter.this));
    }

    private PipelineItemPresenter newItemPresenter() {
        return pipelineItemPresenterInstance.get();
    }

    private void clearItemPresenters() {
        pipelineItemPresenters.forEach(pipelineItemPresenterInstance::destroy);
        pipelineItemPresenters.clear();
    }
}
