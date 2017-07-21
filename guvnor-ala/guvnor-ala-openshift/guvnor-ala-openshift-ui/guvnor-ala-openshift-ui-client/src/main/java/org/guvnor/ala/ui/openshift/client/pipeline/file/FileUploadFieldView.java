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

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.FormPanel;
import org.gwtbootstrap3.client.ui.Form;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.ext.widgets.common.client.common.FileUpload;
import org.uberfire.ext.widgets.common.client.common.FileUploadFormEncoder;
import org.uberfire.mvp.Command;

@Dependent
@Templated
public class FileUploadFieldView
        implements IsElement,
                   FileUploadField.View {

    /**
     * Chrome and other browsers adds this fakepath to the file name for security reasons. If present should be
     * removed to get the real file name.
     */
    private static final String FAKEPATH = "c:\\fakepath\\";

    @Inject
    @DataField("file-upload-form")
    private Form form;

    private FileUploadFormEncoder formEncoder;

    private FileUpload fileUpload;

    private FileUploadField presenter;

    private Command successCommand;

    private Command errorCommand;

    @Override
    public void init(final FileUploadField presenter) {
        this.presenter = presenter;
    }

    @PostConstruct
    private void init() {
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);
        form.addSubmitCompleteHandler(event -> {
            if ("OK".equalsIgnoreCase(event.getResults())) {
                executeCallback(successCommand);
            } else if ("FAIL".equalsIgnoreCase(event.getResults())) {
                executeCallback(errorCommand);
            }
        });
        formEncoder = new FileUploadFormEncoder();
        formEncoder.addUtf8Charset(form);

        fileUpload = new FileUpload();
        fileUpload.setName("file-upload");
        form.add(fileUpload);
    }

    public String getFileName() {
        String fileName = fileUpload.getFilename();
        if (fileName != null && fileName.toLowerCase().startsWith(FAKEPATH)) {
            fileName = fileName.substring(FAKEPATH.length());
        }
        return fileName;
    }

    public void upload(final String baseURL,
                       final String folder,
                       final String fileName,
                       final Command successCommand,
                       final Command errorCommand) {
        this.successCommand = successCommand;
        this.errorCommand = errorCommand;
        form.setAction(createTargetURL(baseURL,
                                       folder,
                                       fileName));
        form.submit();
    }

    private void executeCallback(final Command callback) {
        if (callback == null) {
            return;
        }
        callback.execute();
    }

    private String createTargetURL(final String baseURL,
                                   final String folder,
                                   final String fileName) {
        String encodedFolder = URL.encode(folder);
        String encodedFileName = URL.encode(fileName);
        return baseURL + "?folder=" + encodedFolder + "&fileName=" + encodedFileName;
    }
}