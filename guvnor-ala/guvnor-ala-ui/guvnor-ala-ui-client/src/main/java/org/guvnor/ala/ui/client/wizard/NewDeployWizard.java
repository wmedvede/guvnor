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
import java.util.Iterator;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.events.RefreshRuntimeEvent;
import org.guvnor.ala.ui.client.util.PopupHelper;
import org.guvnor.ala.ui.client.wizard.pipeline.params.PipelineParamsPagePresenter;
import org.guvnor.ala.ui.client.wizard.pipeline.params.PipelineParamsForm;
import org.guvnor.ala.ui.client.wizard.pipeline.SelectPipelinePagePresenter;
import org.guvnor.ala.ui.client.wizard.source.SourceConfigurationPagePresenter;
import org.guvnor.ala.ui.model.PipelineKey;
import org.guvnor.ala.ui.model.Provider;
import org.guvnor.ala.ui.model.Source;
import org.guvnor.ala.ui.service.RuntimeService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.uberfire.ext.widgets.core.client.wizards.WizardPageStatusChangeEvent;
import org.uberfire.workbench.events.NotificationEvent;

import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.NewDeployWizard_PipelineStartSuccessMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.NewDeployWizard_Title;

@ApplicationScoped
public class NewDeployWizard
        extends AbstractMultiPageWizard {

    private final SelectPipelinePagePresenter selectPipelinePage;
    private final SourceConfigurationPagePresenter sourceConfigPage;
    private final PipelineParamsPagePresenter pipelineParamsPage;
    private final Instance<PipelineParamsForm> pipelineParamForms;
    private final PopupHelper popupHelper;
    private final Caller<RuntimeService> runtimeService;

    private final Event<RefreshRuntimeEvent> refreshRuntimeEvent;

    private Provider provider;

    private PipelineParamsForm paramsForm;

    @Inject
    public NewDeployWizard(final SelectPipelinePagePresenter selectPipelinePage,
                           final SourceConfigurationPagePresenter sourceConfigPage,
                           final PipelineParamsPagePresenter pipelineParamsPage,
                           final @Any Instance<PipelineParamsForm> pipelineParamForms,
                           final PopupHelper popupHelper,
                           final TranslationService translationService,
                           final Caller<RuntimeService> runtimeService,
                           final Event<NotificationEvent> notification,
                           final Event<RefreshRuntimeEvent> refreshRuntimeEvent) {
        super(translationService,
              notification);
        this.popupHelper = popupHelper;
        this.selectPipelinePage = selectPipelinePage;
        this.sourceConfigPage = sourceConfigPage;
        this.pipelineParamsPage = pipelineParamsPage;
        this.pipelineParamForms = pipelineParamForms;
        this.runtimeService = runtimeService;
        this.refreshRuntimeEvent = refreshRuntimeEvent;
    }

    @PostConstruct
    public void init() {
        setDefaultPages();
    }

    public void start(final Provider provider,
                      final Collection<PipelineKey> pipelines) {
        this.provider = provider;
        setDefaultPages();
        clear();
        selectPipelinePage.setup(pipelines);
        sourceConfigPage.setup();
        super.start();
    }

    @Override
    public String getTitle() {
        return translationService.getTranslation(NewDeployWizard_Title);
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
        final PipelineKey pipeline = selectPipelinePage.getPipeline();
        final String runtime = sourceConfigPage.getRuntime();
        final Source source = sourceConfigPage.buildSource();

        runtimeService.call((Void aVoid) -> onPipelineStartSuccess(),
                            popupHelper.getPopupErrorCallback()).createRuntime(provider.getKey(),
                                                                               runtime,
                                                                               source,
                                                                               pipeline,
                                                                               paramsForm != null ? paramsForm.buildParams() : null);
    }

    @Override
    public void onStatusChange(final @Observes WizardPageStatusChangeEvent event) {
        boolean restart = false;
        if (event.getPage() == selectPipelinePage) {
            PipelineParamsForm oldParamsForm = paramsForm;
            if (selectPipelinePage.getPipeline() != null) {
                paramsForm = getParamsForm(selectPipelinePage.getPipeline());
                if (paramsForm != null) {
                    paramsForm.clear();
                    pipelineParamsPage.setPipelineParamsForm(paramsForm);
                    setDefaultPages();
                    pages.add(pipelineParamsPage);
                    restart = true;
                } else if (oldParamsForm != null) {
                    setDefaultPages();
                    restart = true;
                }
            } else if (pages.size() > 2) {
                paramsForm = null;
                setDefaultPages();
                restart = true;
            }
            if (oldParamsForm != null) {
                oldParamsForm.clear();
            }
        }

        if (restart) {
            super.start();
        } else {
            super.onStatusChange(event);
        }
    }

    private PipelineParamsForm getParamsForm(final PipelineKey pipelineKey) {
        Iterator<PipelineParamsForm> forms = pipelineParamForms.iterator();
        while (forms.hasNext()) {
            PipelineParamsForm form = forms.next();
            if (form.accept(pipelineKey)) {
                return form;
            }
        }
        return null;
    }

    private void onPipelineStartSuccess() {
        notification.fire(new NotificationEvent(translationService.getTranslation(NewDeployWizard_PipelineStartSuccessMessage),
                                                NotificationEvent.NotificationType.SUCCESS));
        NewDeployWizard.super.complete();
        refreshRuntimeEvent.fire(new RefreshRuntimeEvent(provider.getKey()));
    }

    private void setDefaultPages() {
        pages.clear();
        pages.add(selectPipelinePage);
        pages.add(sourceConfigPage);
    }

    private void clear() {
        selectPipelinePage.clear();
        sourceConfigPage.clear();
    }
}
