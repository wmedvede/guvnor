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
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.events.RefreshRuntime;
import org.guvnor.ala.ui.client.wizard.pipeline.SelectPipelinePagePresenter;
import org.guvnor.ala.ui.client.wizard.source.SourceFormPresenter;
import org.guvnor.ala.ui.model.Provider;
import org.guvnor.ala.ui.model.Source;
import org.guvnor.ala.ui.service.RuntimeService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.uberfire.workbench.events.NotificationEvent;

import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.NewDeployWizard_PipelineStartErrorMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.NewDeployWizard_PipelineStartSuccessMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.NewDeployWizard_title;

@ApplicationScoped
public class NewDeployWizard
        extends AbstractMultiPageWizard {

    private SelectPipelinePagePresenter selectPipelinePage;
    private SourceFormPresenter sourceFormPresenter;

    private Caller<RuntimeService> runtimeService;

    private Event<RefreshRuntime> refreshRuntimeEvent;

    private Provider provider;

    public NewDeployWizard() {
    }

    @Inject
    public NewDeployWizard(final SelectPipelinePagePresenter selectPipelinePage,
                           final SourceFormPresenter sourceFormPresenter,
                           final TranslationService translationService,
                           final Caller<RuntimeService> runtimeService,
                           final Event<NotificationEvent> notification,
                           final Event<RefreshRuntime> refreshRuntimeEvent) {
        super(translationService,
              notification);
        this.selectPipelinePage = selectPipelinePage;
        this.sourceFormPresenter = sourceFormPresenter;
        this.runtimeService = runtimeService;
        this.refreshRuntimeEvent = refreshRuntimeEvent;
    }

    @PostConstruct
    private void init() {
        pages.add(selectPipelinePage);
        pages.add(sourceFormPresenter);
    }

    public void setup(final Provider provider,
                      final Collection<String> pipelines) {
        this.provider = provider;
        clear();
        selectPipelinePage.setup(pipelines);
        sourceFormPresenter.setup(provider);
    }

    @Override
    public String getTitle() {
        return translationService.getTranslation(NewDeployWizard_title);
    }

    @Override
    public int getPreferredHeight() {
        return 550;
    }

    @Override
    public int getPreferredWidth() {
        return 800;
    }

    @Override
    public void complete() {
        final String pipeline = selectPipelinePage.getPipeline();
        final String runtime = sourceFormPresenter.getRuntime();
        final Source source = sourceFormPresenter.buildSource();

        runtimeService.call((Void aVoid) -> onPipelineStartSuccess(),
                            (message, throwable) -> onPipelineStartError()).createRuntime(provider.getKey(),
                                                                                          runtime,
                                                                                          source,
                                                                                          pipeline);
    }

    private void onPipelineStartSuccess() {
        notification.fire(new NotificationEvent(translationService.getTranslation(NewDeployWizard_PipelineStartSuccessMessage),
                                                NotificationEvent.NotificationType.SUCCESS));
        NewDeployWizard.super.complete();
        refreshRuntimeEvent.fire(new RefreshRuntime(provider.getKey()));
    }

    private boolean onPipelineStartError() {
        notification.fire(new NotificationEvent(translationService.getTranslation(NewDeployWizard_PipelineStartErrorMessage),
                                                NotificationEvent.NotificationType.ERROR));
        NewDeployWizard.this.pageSelected(0);
        NewDeployWizard.this.start();
        return false;
    }

    private void clear() {
        selectPipelinePage.clear();
        sourceFormPresenter.clear();
    }
}
