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

package org.guvnor.ala.ui.client.wizard.pipeline.item;

import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.util.ContentChangeHandler;
import org.jboss.errai.common.client.api.IsElement;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.ext.widgets.core.client.wizards.WizardPageStatusChangeEvent;

import static org.uberfire.commons.validation.PortablePreconditions.*;

@Dependent
public class PipelineItemPresenter {

    public interface View extends UberElement<PipelineItemPresenter> {

        boolean isSelected();

        void unSelect();

        void addContentChangeHandler(final ContentChangeHandler contentChangeHandler);

        void setPipelineName(String name);
    }

    private final View view;
    private final Event<WizardPageStatusChangeEvent> wizardPageStatusChangeEvent;
    private ContentChangeHandler changeHandler;
    private final Collection<PipelineItemPresenter> others = new ArrayList<>();

    private String pipeline;

    @Inject
    public PipelineItemPresenter(final View view,
                                 final Event<WizardPageStatusChangeEvent> wizardPageStatusChangeEvent) {
        this.view = view;
        this.wizardPageStatusChangeEvent = wizardPageStatusChangeEvent;
    }

    @PostConstruct
    public void init() {
        this.view.init(this);
    }

    public void addContentChangeHandler(final ContentChangeHandler contentChangeHandler) {
        this.changeHandler = checkNotNull("contentChangeHandler",
                                          contentChangeHandler);
        if (view != null) {
            view.addContentChangeHandler(changeHandler);
        }
    }

    public String getPipeline() {
        return pipeline;
    }

    public void setup(final String pipeline) {
        this.pipeline = pipeline;
        view.setPipelineName(pipeline);
    }

    public void unselectOthers() {
        for (PipelineItemPresenter other : others) {
            other.unSelect();
        }
    }

    private void unSelect() {
        view.unSelect();
    }

    public void addOthers(final Collection<PipelineItemPresenter> pipelineItemPresenters) {
        for (PipelineItemPresenter other : pipelineItemPresenters) {
            if (!other.equals(this)) {
                others.add(other);
            }
        }
    }

    public boolean isSelected() {
        return view.isSelected();
    }

    public IsElement getView() {
        return view;
    }
}
