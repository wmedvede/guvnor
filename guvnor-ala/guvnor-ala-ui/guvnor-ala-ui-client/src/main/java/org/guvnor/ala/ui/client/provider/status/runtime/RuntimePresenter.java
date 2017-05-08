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

package org.guvnor.ala.ui.client.provider.status.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.widget.pipeline.PipelinePresenter;
import org.guvnor.ala.ui.client.widget.pipeline.step.State;
import org.guvnor.ala.ui.client.widget.pipeline.step.StepPresenter;
import org.guvnor.ala.ui.client.widget.pipeline.transition.TransitionPresenter;
import org.guvnor.ala.ui.events.StageStatusChange;
import org.guvnor.ala.ui.model.StageStatus;
import org.guvnor.ala.ui.model.Step;
import org.guvnor.ala.ui.service.PipelineConstants;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.ioc.client.container.IOC;
import org.guvnor.ala.ui.events.RuntimeStatusChange;
import org.guvnor.ala.ui.model.Runtime;
import org.guvnor.ala.ui.model.RuntimeStatus;
import org.guvnor.ala.ui.service.RuntimeService;
import org.uberfire.client.mvp.UberElement;

@Dependent
public class RuntimePresenter {

    public interface View extends UberElement<RuntimePresenter> {

        void setup( final String name,
                    final String date,
                    final String pipeline );

        void setEndpoint( String endpoint );

        void disableStart( );

        void enableStart( );

        void disableStop( );

        void enableStop( );

        void setStatus( final Collection< String > strings );

        void addExpandedContent( final IsElement element );
    }

    private final View view;
    private final PipelinePresenter pipelinePresenter;
    private final Caller<RuntimeService> runtimeService;
    private final List<Step> currentSteps = new ArrayList<>();
    private final Map<Step, StepPresenter> stepPresenters = new HashMap<>();

    private Runtime runtime;

    @Inject
    public RuntimePresenter( final View view,
                             final PipelinePresenter pipelinePresenter,
                             final Caller<RuntimeService> runtimeService ) {
        this.view = view;
        this.pipelinePresenter = pipelinePresenter;
        this.runtimeService = runtimeService;
    }

    @PostConstruct
    public void init() {
        this.view.init( this );
    }

    public void setup( final Runtime runtime ) {
        this.runtime = runtime;

        view.setup(runtime.getId(),
                   runtime.createDate(),
                   runtime.getPipeline() != null ? runtime.getPipeline().getKey().getId() : PipelineConstants.WILDFLY_PROVISIONING_PIPELINE );

        processStatus( runtime );

        currentSteps.clear();
        if (runtime.getPipeline() != null) {
            boolean showStep = true;
            for (int i = 0; showStep && i < runtime.getPipeline().getSteps().size(); i++) {
                Step step = runtime.getPipeline().getSteps().get(i);
                showStep = showStep(step);
                if (showStep) {
                    if (i > 0) {
                        pipelinePresenter.addStage(newTransitionPresenter().getView());
                    }
                    final StepPresenter stepPresenter = newStepPresenter();
                    stepPresenter.setup(step);
                    stepPresenter.setState(calculateState(step.getStatus()));
                    pipelinePresenter.addStage(stepPresenter.getView());
                }
            }
        }
        view.addExpandedContent( pipelinePresenter.getView() );
    }

    private boolean showStep(Step step) {
        return step.getStatus() == StageStatus.RUNNING ||
                step.getStatus() == StageStatus.FINISHED ||
                step.getStatus() == StageStatus.ERROR;

    }

    private State calculateState( StageStatus stageStatus ) {
        if (stageStatus == StageStatus.RUNNING) {
            return State.EXECUTING;
        } else {
            return State.DONE;
        }
    }

    private void processStatus( final Runtime runtime ) {
        switch ( runtime.getStatus() ) {
            case STARTED:
            case LOADING:
            case WARN:
                view.setEndpoint( runtime.getEndpoint() );
                view.enableStop();
                view.disableStart();
                break;
            case STOPPED:
            case ERROR:
                view.disableStop();
                view.enableStart();
                break;
        }
        view.setStatus( buildStyle( runtime.getStatus() ) );
    }

    public void onStatusChange( @Observes RuntimeStatusChange statusChange ) {
        if ( statusChange.getRuntime().equals( runtime ) ) {
            if ( statusChange.getRuntime().getStatus() == RuntimeStatus.STARTED ) {
                view.setEndpoint( statusChange.getRuntime().getEndpoint() );
            }
            processStatus( statusChange.getRuntime() );
        }
    }

    public void onStageStatusChange(@Observes StageStatusChange statusChange) {
        if ( statusChange.getRuntime().equals(runtime) ) {

            Step currentStep = currentSteps.stream().
                    filter(step -> statusChange.getStage().equals(step.getMessage()))
                    .findFirst()
                    .orElse(null);

            if (currentStep != null) {
                StepPresenter stepPresenter = stepPresenters.get(currentStep);
                stepPresenter.setState(calculateState(statusChange.getStatus()));
            } else {
                Step step = new Step(runtime.getPipeline().getKey(),
                                     statusChange.getStage(),
                                     statusChange.getStatus());
                StepPresenter stepPresenter = newStepPresenter();
                stepPresenter.setup(step);
                stepPresenter.setState(calculateState(step.getStatus()));
                if (!currentSteps.isEmpty()) {
                    pipelinePresenter.addStage(newTransitionPresenter().getView());
                }
                pipelinePresenter.addStage(stepPresenter.getView());

                currentSteps.add(step);
                stepPresenters.put(step,
                                   stepPresenter);
            }
        }
    }

    private Collection<String> buildStyle( final RuntimeStatus status ) {
        switch ( status ) {
            case STARTED:
                return Arrays.asList( "pficon", "list-view-pf-icon-md", "pficon-ok", "list-view-pf-icon-success" );
            case LOADING:
                return Arrays.asList( "fa", "list-view-pf-icon-md", "fa-circle-o-notch", "fa-spin" );
            case WARN:
                return Arrays.asList( "pficon", "list-view-pf-icon-md", "pficon-warning-triangle-o", "list-view-pf-icon-warning" );
            case STOPPED:
                return Arrays.asList( "pficon", "list-view-pf-icon-md", "pficon-close", "list-view-pf-icon-info" );
            case ERROR:
                return Arrays.asList( "pficon", "list-view-pf-icon-md", "pficon-error-circle-o", "list-view-pf-icon-danger" );
        }

        return Collections.emptyList();
    }

    public void start() {
        runtimeService.call().start( runtime );
    }

    public void stop() {
        runtimeService.call().stop( runtime );
    }

    public void rebuild() {
        runtimeService.call().rebuild( runtime );
    }

    public void delete() {
        runtimeService.call().delete( runtime );
    }

    public View getView() {
        return view;
    }

    private StepPresenter newStepPresenter() {
        return IOC.getBeanManager().lookupBean(StepPresenter.class).getInstance();
    }

    private TransitionPresenter newTransitionPresenter() {
        return IOC.getBeanManager().lookupBean(TransitionPresenter.class).getInstance();
    }
}
