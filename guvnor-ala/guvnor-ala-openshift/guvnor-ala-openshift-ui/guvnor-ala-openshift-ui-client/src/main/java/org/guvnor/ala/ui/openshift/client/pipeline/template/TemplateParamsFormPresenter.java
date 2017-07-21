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

package org.guvnor.ala.ui.openshift.client.pipeline.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.util.AbstractHasContentChangeHandlers;
import org.guvnor.ala.ui.client.util.PopupHelper;
import org.guvnor.ala.ui.client.widget.FormStatus;
import org.guvnor.ala.ui.client.wizard.NewDeployWizard;
import org.guvnor.ala.ui.client.wizard.pipeline.params.PipelineParamsForm;
import org.guvnor.ala.ui.client.wizard.project.GAVConfigurationChangeEvent;
import org.guvnor.ala.ui.openshift.client.pipeline.template.table.TemplateParamsTablePresenter;
import org.guvnor.ala.ui.openshift.model.ConfigResponse;
import org.guvnor.ala.ui.openshift.model.ConfigType;
import org.guvnor.ala.ui.openshift.model.DefaultSettings;
import org.guvnor.ala.ui.openshift.model.TemplateConfigResponse;
import org.guvnor.ala.ui.openshift.model.TemplateParam;
import org.guvnor.ala.ui.openshift.model.URLConfigRequest;
import org.guvnor.ala.ui.openshift.service.OpenshiftClientService;
import org.guvnor.common.services.project.model.GAV;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.client.mvp.UberElement;

import static org.guvnor.ala.ui.openshift.client.resources.i18n.GuvnorAlaOpenShiftUIConstants.TemplateParamsFormPresenter_GetTemplateFileConfigError;
import static org.guvnor.ala.ui.openshift.client.resources.i18n.GuvnorAlaOpenShiftUIConstants.TemplateParamsFormPresenter_RequiredParamsNotCompletedMessage;

@ApplicationScoped
public class TemplateParamsFormPresenter
        extends AbstractHasContentChangeHandlers
        implements PipelineParamsForm {

    //Pipeline params
    public static final String PARAM_DELIMITER = ",";

    public static final String PARAM_ASSIGNER = "=";

    public static final String RESOURCE_TEMPLATE_PARAM_VALUES = "resource-template-param-values";

    public static final String RESOURCE_TEMPLATE_URI = "resource-template-uri";

    public static final String RESOURCE_SECRETS_URI = "resource-secrets-uri";

    public static final String RESOURCE_STREAMS_URI = "resource-streams-uri";

    public static final String APPLICATION_NAME = "application-name";

    public static final String PROJECT_NAME = "project-name";

    public static final String SERVICE_NAME = "service-name";

    public static final String KIE_SERVER_CONTAINER_DEPLOYMENT = "org-kie-server-container-deployment";

    //Template params
    /**
     * The IMAGE_STREAM_NAMESPACE must be defaulted to the project name.
     */
    public static final String IMAGE_STREAM_NAMESPACE_TEMPLATE_PARAM = "IMAGE_STREAM_NAMESPACE";

    public static final String APPLICATION_NAME_TEMPLATE_PARAM = "APPLICATION_NAME";

    public static final String CONTAINER_SUFFIX = "Container";

    public static final String SERVICE_NAME_SUFFIX = "-execserv";

    private static final Set<String> bannedParameters = new HashSet<>();

    static {
        bannedParameters.add(IMAGE_STREAM_NAMESPACE_TEMPLATE_PARAM);
        bannedParameters.add(APPLICATION_NAME_TEMPLATE_PARAM);
    }

    public interface View
            extends UberElement<TemplateParamsFormPresenter> {

        void setTemplateURL(final String templateURL);

        String getTemplateURL();

        void setImageStreamsURL(final String imageStreamsURL);

        String getImageStreamsURL();

        void setSecretsFileURL(final String secretsFileURL);

        String getSecretsFileURL();

        void setRuntimeName(final String runtimeName);

        String getRuntimeName();

        void setRuntimeNameStatus(final FormStatus status);

        void setTemplateURLStatus(final FormStatus status);

        void setImageStreamsURLStatus(final FormStatus status);

        void setSecretsFileURLStatus(final FormStatus status);

        void setRequiredParamsHelpText(final String requiredParamsHelpText);

        void clearRequiredParamsHelpText();

        void clear();

        String getWizardTitle();

        void setParamsEditorPresenter(HTMLElement paramsEditorPresenter);
    }

    private final View view;

    private TemplateParamsTablePresenter paramsEditorPresenter;

    private final TranslationService translationService;

    private final PopupHelper popupHelper;

    private final Caller<OpenshiftClientService> openshiftClientService;

    private List<TemplateParam> params = new ArrayList<>();

    private boolean templateLoaded = false;

    private GAV selectedGAV = null;

    @Inject
    public TemplateParamsFormPresenter(final View view,
                                       final TemplateParamsTablePresenter paramsEditorPresenter,
                                       final TranslationService translationService,
                                       final PopupHelper popupHelper,
                                       final Caller<OpenshiftClientService> openshiftClientService) {
        this.view = view;
        this.paramsEditorPresenter = paramsEditorPresenter;
        this.translationService = translationService;
        this.popupHelper = popupHelper;
        this.openshiftClientService = openshiftClientService;
    }

    @PostConstruct
    public void init() {
        view.init(this);
        view.setParamsEditorPresenter(paramsEditorPresenter.getView().getElement());
        paramsEditorPresenter.setParamChangeHandler((paramName, newValue, oldValue) -> {
            updateRequiredParamsHelpText();
            onContentChange();
        });
    }

    @Override
    public String getWizardTitle() {
        return view.getWizardTitle();
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public Map<String, String> buildParams() {
        final Map<String, String> pipelineParams = new HashMap<>();

        String runtimeName = getRuntimeName();
        String projectName = runtimeName;
        String applicationName = runtimeName;
        String serviceName = runtimeName + SERVICE_NAME_SUFFIX;
        String imageStreamNamespace = projectName;
        String container = runtimeName;

        pipelineParams.put(NewDeployWizard.RUNTIME_NAME,
                           runtimeName);
        pipelineParams.put(PROJECT_NAME,
                           projectName);
        pipelineParams.put(APPLICATION_NAME,
                           applicationName);
        pipelineParams.put(SERVICE_NAME,
                           serviceName);

        pipelineParams.put(RESOURCE_TEMPLATE_URI,
                           getTemplateURL());
        pipelineParams.put(RESOURCE_STREAMS_URI,
                           getImageStreamsURL());
        pipelineParams.put(RESOURCE_SECRETS_URI,
                           getSecretsFileURL());

        StringBuilder builder = new StringBuilder();
        params.forEach(param -> {
            if (!isBanned(param.getName()) && !isEmpty(param.getValue())) {
                addTemplateParam(builder,
                                 param.getName(),
                                 param.getValue());
            }
        });

        //set the IMAGE_STREAM_NAMESPACE param with the project name value.
        addTemplateParam(builder,
                         IMAGE_STREAM_NAMESPACE_TEMPLATE_PARAM,
                         imageStreamNamespace);

        addTemplateParam(builder,
                         APPLICATION_NAME_TEMPLATE_PARAM,
                         applicationName);

        //if the GAV is selected, add it to the template params.
        if (selectedGAV != null) {
            String containerParamName = container + CONTAINER_SUFFIX;
            addTemplateParam(builder,
                             containerParamName,
                             buildContainerParamValue(container,
                                                      selectedGAV));
        }

        pipelineParams.put(RESOURCE_TEMPLATE_PARAM_VALUES,
                           builder.toString());
        return pipelineParams;
    }

    @Override
    public void initialise() {
        templateLoaded = false;
        openshiftClientService.call(getDefaultSettingsSuccessCallback(),
                                    popupHelper.getPopupErrorCallback()).getDefaultSettings();
    }

    @Override
    public void prepareView() {
        paramsEditorPresenter.getView().redraw();
    }

    @Override
    public void isComplete(final Callback<Boolean> callback) {
        boolean isValid = isRuntimeValid()
                && isTemplateValid() && isParamsValid() &&
                isImageStreamsURLValid() &&
                isSecretsFileURLValid();
        callback.callback(isValid);
    }

    @Override
    public void clear() {
        clearParams();
        view.clear();
    }

    private RemoteCallback<DefaultSettings> getDefaultSettingsSuccessCallback() {
        return defaultSettings -> {
            String templateURL = (String) defaultSettings.getValue(DefaultSettings.DEFAULT_OPEN_SHIFT_TEMPLATE);
            view.setTemplateURL(templateURL);
            view.setImageStreamsURL((String) defaultSettings.getValue(DefaultSettings.DEFAULT_OPEN_SHIFT_IMAGE_STREAMS));
            view.setSecretsFileURL((String) defaultSettings.getValue(DefaultSettings.DEFAULT_OPEN_SHIFT_SECRETS));
            if (templateURL != null) {
                loadTemplate(templateURL);
            }
        };
    }

    private void loadTemplate(String templateURL) {
        templateLoaded = false;
        clearParams();
        openshiftClientService.call(getServerTemplateConfigSuccessCallback(),
                                    getServerTemplateConfigErrorCallback()).getConfig(new URLConfigRequest(templateURL,
                                                                                                           ConfigType.TEMPLATE));
    }

    private RemoteCallback<ConfigResponse> getServerTemplateConfigSuccessCallback() {
        return configResponse -> {
            templateLoaded = true;
            view.setTemplateURLStatus(FormStatus.VALID);
            TemplateConfigResponse response = (TemplateConfigResponse) configResponse;
            setup(response.getModel().getParams());
            onContentChange();
        };
    }

    private ErrorCallback<Message> getServerTemplateConfigErrorCallback() {
        return (message, throwable) -> {
            templateLoaded = false;
            view.setTemplateURLStatus(FormStatus.ERROR);
            popupHelper.showErrorPopup(translationService.getTranslation(TemplateParamsFormPresenter_GetTemplateFileConfigError) +
                                               ": " + throwable.getMessage());
            onContentChange();
            return false;
        };
    }

    private void setup(final List<TemplateParam> templateParams) {
        this.params = templateParams.stream().filter(param -> !bannedParameters.contains(param.getName())).collect(Collectors.toList());
        paramsEditorPresenter.setItems(params);
        updateRequiredParamsHelpText();
    }

    protected void onRuntimeNameChange() {
        if (!getRuntimeName().isEmpty()) {
            view.setRuntimeNameStatus(FormStatus.VALID);
        } else {
            view.setRuntimeNameStatus(FormStatus.ERROR);
        }
        onContentChange();
    }

    protected void onTemplateURLChange() {
        if (getTemplateURL().isEmpty()) {
            templateLoaded = false;
            view.setTemplateURLStatus(FormStatus.ERROR);
            clearParams();
            onContentChange();
        } else {
            loadTemplate(getTemplateURL());
        }
    }

    protected void onImageStreamsURLChange() {
        if (getImageStreamsURL().isEmpty()) {
            view.setImageStreamsURLStatus(FormStatus.ERROR);
        } else {
            view.setImageStreamsURLStatus(FormStatus.VALID);
        }
    }

    protected void onSecretsFileURLChange() {
        if (getSecretsFileURL().isEmpty()) {
            view.setSecretsFileURLStatus(FormStatus.ERROR);
        } else {
            view.setSecretsFileURLStatus(FormStatus.VALID);
        }
    }

    protected void onGAVConfigurationChange(@Observes final GAVConfigurationChangeEvent event) {
        selectedGAV = event.getGav();
    }

    private void onContentChange() {
        fireChangeHandlers();
    }

    private void clearParams() {
        params.clear();
        paramsEditorPresenter.clear();
        updateRequiredParamsHelpText();
    }

    private String getRuntimeName() {
        return view.getRuntimeName().trim();
    }

    private String getTemplateURL() {
        return view.getTemplateURL().trim();
    }

    private String getImageStreamsURL() {
        return view.getImageStreamsURL().trim();
    }

    private String getSecretsFileURL() {
        return view.getSecretsFileURL().trim();
    }

    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private boolean isRuntimeValid() {
        return !getRuntimeName().isEmpty();
    }

    private boolean isTemplateValid() {
        return templateLoaded;
    }

    private boolean isImageStreamsURLValid() {
        return !getImageStreamsURL().isEmpty();
    }

    private boolean isSecretsFileURLValid() {
        return !getSecretsFileURL().isEmpty();
    }

    private boolean isParamsValid() {
        if (params != null) {
            return !params.stream().filter(param -> param.isRequired() && isEmpty(param.getValue())).findFirst().isPresent();
        }
        return true;
    }

    private void updateRequiredParamsHelpText() {
        view.clearRequiredParamsHelpText();
        if (!isParamsValid()) {
            view.setRequiredParamsHelpText(translationService.getTranslation(TemplateParamsFormPresenter_RequiredParamsNotCompletedMessage));
        }
    }

    private boolean isBanned(String paramName) {
        return bannedParameters.contains(paramName);
    }

    private void addTemplateParam(StringBuilder builder,
                                  String paramName,
                                  String paramValue) {
        if (builder.length() > 0) {
            builder.append(PARAM_DELIMITER);
        }
        builder.append(paramName);
        builder.append(PARAM_ASSIGNER);
        builder.append(paramValue);
    }

    private String buildContainerParamValue(final String container,
                                            final GAV gav) {
        return container + gav.getGroupId() +
                ":" + container + gav.getArtifactId() +
                ":" + container + gav.getVersion();
    }
}
