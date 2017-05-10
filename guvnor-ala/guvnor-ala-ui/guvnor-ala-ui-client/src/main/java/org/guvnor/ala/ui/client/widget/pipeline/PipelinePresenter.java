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

package org.guvnor.ala.ui.client.widget.pipeline;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.widget.pipeline.step.StepPresenter;
import org.guvnor.ala.ui.client.widget.pipeline.transition.TransitionPresenter;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.ioc.client.container.IOC;
import org.guvnor.ala.ui.events.NewPipelineStep;
import org.guvnor.ala.ui.model.Runtime;
import org.uberfire.client.mvp.UberElement;

@Dependent
public class PipelinePresenter {

    private final View view;
    private Runtime runtime;

    public interface View extends UberElement<PipelinePresenter> {

        void addStage( final IsElement element );
    }

    @Inject
    public PipelinePresenter( final View view ) {
        this.view = view;
    }

    public View getView() {
        return view;
    }

    public void addStage( final IsElement stage ) {
        view.addStage( stage );
    }

    public void setup( final Runtime runtime ) {
        this.runtime = runtime;
    }

    public void onNewStep( @Observes NewPipelineStep newStep ) {
        if ( newStep.getStep().getPipelineKey().getRuntimeKey().equals( runtime.getKey() ) ) {
            addStage( IOC.getBeanManager().lookupBean( TransitionPresenter.class ).getInstance().getView() );
            final StepPresenter step = IOC.getBeanManager().lookupBean( StepPresenter.class ).getInstance();
            step.setup( newStep.getStep() );
            addStage( step.getView() );
        }
    }

}
