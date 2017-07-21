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

package org.guvnor.ala.ui.openshift.client.pipeline.config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Dependent
@Templated
public class ConfigFilesFormView
        implements IsElement,
                   ConfigFilesFormPresenter.View {

    @Inject
    @DataField("template-file-container")
    private Div templateFileContainer;

    @Inject
    @DataField("image-stream-container")
    private Div imageStreamContainer;

    @Inject
    @DataField("secrets-stream-container")
    private Div sectetsStreamContainer;

    private ConfigFilesFormPresenter presenter;

    @Override
    public void init(final ConfigFilesFormPresenter presenter) {
        this.presenter = presenter;
    }

    @PostConstruct
    private void init() {

    }

    @Override
    public void disable() {
    }

    @Override
    public void enable() {
    }

    @Override
    public void clear() {

    }

    @Override
    public void setTemplateFilePresenter(ConfigFilePresenter templateFilePresenter) {
        templateFileContainer.appendChild(templateFilePresenter.getElement());
    }

    @Override
    public void setImageStreamPresenter(ConfigFilePresenter imageStreamPresenter) {
        imageStreamContainer.appendChild(imageStreamPresenter.getElement());
    }

    @Override
    public void setSecretsStreamPresenter(ConfigFilePresenter secretsStreamPresenter) {
        sectetsStreamContainer.appendChild(secretsStreamPresenter.getElement());
    }

    @Override
    public String getWizardTitle() {
        return "Template Selection";
    }
}