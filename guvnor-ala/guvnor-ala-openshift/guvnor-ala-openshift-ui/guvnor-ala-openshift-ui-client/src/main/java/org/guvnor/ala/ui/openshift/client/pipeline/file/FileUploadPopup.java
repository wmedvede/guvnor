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

package org.guvnor.ala.ui.openshift.client.pipeline.file;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import org.guvnor.ala.ui.openshift.client.popup.BaseOkCancelPopup;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;

@Dependent
public class FileUploadPopup
        extends BaseOkCancelPopup {

    private ParameterizedCommand<String> onUploadSuccessCommand;

    private Command onCancelCommand;

    private FileUploadField fileUploadField;

    private String baseURL;

    private String folder;

    @Inject
    public FileUploadPopup(final View view,
                           final FileUploadField fileUploadField) {
        super(view);
        this.fileUploadField = fileUploadField;
    }

    @PostConstruct
    @Override
    public void init() {
        super.init();
        view.setContent(fileUploadField.getElement());
    }

    public void show(final String title,
                     final String baseURL,
                     final String folder,
                     final ParameterizedCommand<String> onUploadSuccessCommand,
                     final Command onCancelCommand) {
        this.onUploadSuccessCommand = onUploadSuccessCommand;
        this.onCancelCommand = onCancelCommand;
        this.baseURL = baseURL;
        this.folder = folder;
        view.show(title);
    }

    @Override
    protected void onOK() {
        doUpload();
    }

    @Override
    protected void onCancel() {
        super.onCancel();
        onCancelCommand.execute();
    }

    protected void onUploadSuccess() {
        hide();
        onUploadSuccessCommand.execute(fileUploadField.getFileName());
    }

    protected void onUploadError() {
        //TODO show a translated message
        Window.alert("File upload failed.");
        hide();
    }

    private void doUpload() {
        String fileName = fileUploadField.getFileName();
        if (fileName == null || "".equals(fileName)) {
            //TODO show a translated message
            Window.alert("No file has been selected");
        } else {
            //TODO show a translated message
            view.showBusyIndicator("Uploading file");
            fileUploadField.upload(baseURL,
                                   folder,
                                   fileName,
                                   this::onUploadSuccess,
                                   this::onUploadError);
        }
    }
}