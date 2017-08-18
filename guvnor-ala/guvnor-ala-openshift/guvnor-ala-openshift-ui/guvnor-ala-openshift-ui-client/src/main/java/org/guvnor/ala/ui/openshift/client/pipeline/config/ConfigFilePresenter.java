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

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import org.guvnor.ala.ui.client.util.AbstractHasContentChangeHandlers;
import org.guvnor.ala.ui.client.util.ContentChangeHandler;
import org.guvnor.ala.ui.openshift.client.pipeline.file.FileUploadPopup;
import org.guvnor.ala.ui.openshift.client.pipeline.url.URLPopup;
import org.guvnor.ala.ui.openshift.model.ConfigResponse;
import org.guvnor.ala.ui.openshift.model.ConfigType;
import org.guvnor.ala.ui.openshift.model.PathConfigRequest;
import org.guvnor.ala.ui.openshift.model.TemplateConfigResponse;
import org.guvnor.ala.ui.openshift.model.URLConfigRequest;
import org.guvnor.ala.ui.openshift.service.OpenshiftClientService;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.client.mvp.UberElement;

@Dependent
public class ConfigFilePresenter
        extends AbstractHasContentChangeHandlers
        implements IsElement {

    public interface View
            extends UberElement<ConfigFilePresenter> {

        void setFieldValue(String fieldValue);
    }

    private final View view;

    private final FileUploadPopup uploadPopup;

    private final URLPopup urlPopup;

    private final Caller<OpenshiftClientService> openshiftService;

    private ConfigType configType;

    private String baseURL;

    private String folder;

    private ConfigResponse configResponse;

    @Inject
    public ConfigFilePresenter(final View view,
                               final FileUploadPopup uploadPopup,
                               final URLPopup urlPopup,
                               final Caller<OpenshiftClientService> openshiftService) {
        this.view = view;
        this.uploadPopup = uploadPopup;
        this.urlPopup = urlPopup;
        this.openshiftService = openshiftService;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    @Override
    public HTMLElement getElement() {
        return view.getElement();
    }

    public void init(final ConfigType configType,
                     final String baseURL,
                     final String folder,
                     final ContentChangeHandler contentChangeHandler) {
        this.configType = configType;
        this.baseURL = baseURL;
        this.folder = folder;
        addContentChangeHandler(contentChangeHandler);
    }

    public ConfigResponse getConfigResponse() {
        return configResponse;
    }

    protected void onUploadFile() {
        uploadPopup.show("Upload the config file",
                         baseURL,
                         folder,
                         (fileName) -> onFileUploaded(fileName),
                         () -> {
                         });
    }

    protected void onFileUploaded(final String fileName) {
        view.setFieldValue("file:" + fileName);
        openshiftService.call(getConfigSuccessCallback(),
                              getConfigErrorCallback()).getConfig(createPathRequest(fileName,
                                                                                    configType));
    }

    protected void onGetFromURL() {
        urlPopup.show("Enter the config file url location",
                      (url) -> onURLEntered(url),
                      () -> {
                      });
    }

    protected void onURLEntered(final String url) {
        view.setFieldValue("url: " + url);
        openshiftService.call(getConfigSuccessCallback(),
                              getConfigErrorCallback()).getConfig(createURLRequest(url,
                                                                                   configType));
    }

    protected void onClear() {
        view.setFieldValue("");
        configResponse = null;
        fireChangeHandlers();
    }

    private RemoteCallback<TemplateConfigResponse> getConfigSuccessCallback() {
        return (response) -> {
            this.configResponse = response;
            fireChangeHandlers();
        };
    }

    private ErrorCallback<Message> getConfigErrorCallback() {
        return (message, throwable) -> {
            Window.alert("Something failed while getting server file: " + throwable.getMessage());
            this.configResponse = null;
            fireChangeHandlers();
            return false;
        };
    }

    private PathConfigRequest createPathRequest(String fileName,
                                                ConfigType configType) {
        Path path = buildTargetPath(getDefaultFolder(),
                                    fileName);
        return new PathConfigRequest(path,
                                     configType);
    }

    private URLConfigRequest createURLRequest(String url,
                                              ConfigType configType) {
        return new URLConfigRequest(url,
                                    configType);
    }

    private String getDefaultFolder() {
        return "default://master@system/:provisioning/tmp-files";
    }

    private Path buildTargetPath(String uri,
                                 String fileName) {
        return PathFactory.newPath(fileName,
                                   encode(uri + "/" + fileName));
    }

    private String encode(final String uri) {
        return URL.encode(uri);
    }
}