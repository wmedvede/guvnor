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

package org.guvnor.ala.ui.client.wizard;

import java.util.Collection;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.events.RefreshRuntime;
import org.guvnor.ala.ui.client.util.ContentChangeHandler;
import org.jboss.errai.common.client.api.Caller;
import org.guvnor.ala.ui.client.wizard.pipeline.PipelinePresenter;
import org.guvnor.ala.ui.client.wizard.source.SourceFormPresenter;
import org.guvnor.ala.ui.model.Provider;
import org.guvnor.ala.ui.model.Source;
import org.guvnor.ala.ui.service.RuntimeService;
import org.uberfire.workbench.events.NotificationEvent;

@ApplicationScoped
public class NewDeployWizard extends AbstractMultiPageWizard {

    private PipelinePresenter pipelinePresenter;
    private SourceFormPresenter sourceFormPresenter;

    private Caller<RuntimeService> runtimeService;

    private Event<NotificationEvent> notification;
    private Event<RefreshRuntime > refreshRuntimeEvent;

    private Provider provider;

    public NewDeployWizard() {
    }

    @Inject
    public NewDeployWizard( final PipelinePresenter pipelinePresenter,
                            final SourceFormPresenter sourceFormPresenter,
                            final Caller<RuntimeService> runtimeService,
                            final Event<NotificationEvent> notification,
                            final Event<RefreshRuntime> refreshRuntimeEvent ) {
        this.pipelinePresenter = pipelinePresenter;
        this.sourceFormPresenter = sourceFormPresenter;
        this.runtimeService = runtimeService;
        this.notification = notification;
        this.refreshRuntimeEvent = refreshRuntimeEvent;

        final ContentChangeHandler changePages = () -> {
        };

        this.pipelinePresenter.addContentChangeHandler( changePages );
        this.sourceFormPresenter.addContentChangeHandler( changePages );
    }

    @Override
    public void start() {
        pipelinePresenter.initialise();
        super.start();
    }

    @Override
    public String getTitle() {
        return "New Deploy Wizard";
    }

    @Override
    public int getPreferredHeight() {
        return 550;
    }

    @Override
    public int getPreferredWidth() {
        return 800;
    }

    public void setup( final Provider provider,
                       final Collection<String> pipelines ) {
        this.provider = provider;
        sourceFormPresenter.setup(provider);
        pipelinePresenter.setup( pipelines );
        pages.clear();
        pages.add( pipelinePresenter );
        pages.add( sourceFormPresenter );
    }

    public void clear() {
        pipelinePresenter.clear();
        sourceFormPresenter.clear();
        pages.clear();
        pages.add( pipelinePresenter );
        pages.add( sourceFormPresenter );
    }

    @Override
    public void close() {
        super.close();
        clear();
    }

    @Override
    public void complete() {
        final Source source = sourceFormPresenter.buildSource();
        final String pipeline = pipelinePresenter.getPipeline();
        final String runtime = sourceFormPresenter.getRuntime();

        runtimeService.call( o -> {
            notification.fire( new NotificationEvent( "Deployment successfully started.", NotificationEvent.NotificationType.SUCCESS ) );
            clear();
            NewDeployWizard.super.complete();
            refreshRuntimeEvent.fire( new RefreshRuntime( provider.getKey() ) );
        }, ( o, throwable ) -> {
            notification.fire( new NotificationEvent( "Failed to create a Deploy.", NotificationEvent.NotificationType.ERROR ) );
            NewDeployWizard.this.pageSelected( 0 );
            NewDeployWizard.this.start();
            return false;
        } ).createRuntime( provider.getKey(), runtime, source, pipeline );
    }
}
