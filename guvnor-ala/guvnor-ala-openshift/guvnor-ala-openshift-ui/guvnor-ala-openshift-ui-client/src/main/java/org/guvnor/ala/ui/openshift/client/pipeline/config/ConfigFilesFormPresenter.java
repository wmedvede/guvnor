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

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import org.guvnor.ala.ui.client.util.AbstractHasContentChangeHandlers;
import org.guvnor.ala.ui.client.wizard.pipeline.params.PipelineParamsForm;
import org.guvnor.ala.ui.model.PipelineKey;
import org.guvnor.ala.ui.openshift.model.ConfigType;
import org.guvnor.ala.ui.openshift.model.RawConfigResponse;
import org.guvnor.ala.ui.openshift.model.TemplateConfigResponse;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.client.mvp.UberElement;

@ApplicationScoped
public class ConfigFilesFormPresenter
        extends AbstractHasContentChangeHandlers
        implements PipelineParamsForm {

    protected static final String RESOURCE_TEMPLATE_URI = "resource-template-uri";

    protected static final String RESOURCE_STREAMS_URI = "resource-streams-uri";

    protected static final String RESOURCE_SECRETS_URI = "resource-secrets-uri";

    public interface View
            extends UberElement<ConfigFilesFormPresenter> {

        void disable();

        void enable();

        void setTemplateFilePresenter(ConfigFilePresenter templateFilePresenter);

        void setImageStreamPresenter(ConfigFilePresenter imageStreamContainer);

        void setSecretsStreamPresenter(ConfigFilePresenter secretsStreamPresenter);

        void clear();

        String getWizardTitle();
    }

    private final View view;

    private final ConfigFilePresenter templateFilePresenter;

    private final ConfigFilePresenter imageStreamPresenter;

    private final ConfigFilePresenter secretsStreamPresenter;

    private TemplateConfigResponse templateConfig;

    private RawConfigResponse imageStreamConfig;

    private RawConfigResponse secretsStreamConfig;

    @Inject
    public ConfigFilesFormPresenter(final View view,
                                    final ConfigFilePresenter templateFilePresenter,
                                    final ConfigFilePresenter imageStreamPresenter,
                                    final ConfigFilePresenter secretsStreamPresenter) {
        this.view = view;
        this.templateFilePresenter = templateFilePresenter;
        this.imageStreamPresenter = imageStreamPresenter;
        this.secretsStreamPresenter = secretsStreamPresenter;
    }

    @PostConstruct
    public void init() {
        view.init(this);
        view.setTemplateFilePresenter(templateFilePresenter);
        templateFilePresenter.init(ConfigType.TEMPLATE,
                                   getBaseURL(),
                                   getDefaultFolder(),
                                   this::onTemplateFileChange);

        view.setImageStreamPresenter(imageStreamPresenter);
        imageStreamPresenter.init(ConfigType.RAW,
                                  getBaseURL(),
                                  getDefaultFolder(),
                                  this::onImageStreamChange);

        view.setSecretsStreamPresenter(secretsStreamPresenter);
        secretsStreamPresenter.init(ConfigType.RAW,
                                    getBaseURL(),
                                    getDefaultFolder(),
                                    this::onSecretsScreenChange);
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public Map<String, String> buildParams() {
        final Map<String, String> params = new HashMap<>();
        params.put(RESOURCE_TEMPLATE_URI,
                   "getServerTemplate()");
        params.put(RESOURCE_STREAMS_URI,
                   "getResourceStreams()");
        params.put(RESOURCE_SECRETS_URI,
                   "getResourceSecrets()");
        return params;
    }

    @Override
    public void clear() {
        view.clear();
    }

    @Override
    public void initialise() {

    }

    @Override
    public void prepareView() {

    }

    @Override
    public void isComplete(final Callback<Boolean> callback) {

    }

    @Override
    public String getWizardTitle() {
        return view.getWizardTitle();
    }

    private void onTemplateFileChange() {
        Window.alert("templateFileChange: " + (templateFilePresenter.getConfigResponse() != null ? templateFilePresenter.getConfigResponse().getContent() : null));
    }

    private void onImageStreamChange() {
        Window.alert("imageStreamPresenter: " + (imageStreamPresenter.getConfigResponse() != null ? imageStreamPresenter.getConfigResponse().getContent() : null));
    }

    private void onSecretsScreenChange() {
        Window.alert("secretsStreamPresenter: " + (secretsStreamPresenter.getConfigResponse() != null ? secretsStreamPresenter.getConfigResponse().getContent() : null));
    }

    private String getBaseURL() {
        return GWT.getModuleBaseURL() + "defaulteditor/upload";
    }

    private String getDefaultFolder() {
        return "default://master@system/:provisioning/tmp-files";
    }

}
