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

import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.mvp.Command;

@Dependent
public class FileUploadField
        implements IsElement {

    public interface View
            extends UberElement<FileUploadField> {

        String getFileName();

        void upload(final String baseURL,
                    final String folder,
                    final String fileName,
                    final Command successCommand,
                    final Command errorCommand);
    }

    private final View view;

    @Inject
    public FileUploadField(final View view) {
        this.view = view;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    @Override
    public HTMLElement getElement() {
        return view.getElement();
    }

    public String getFileName() {
        return view.getFileName();
    }

    public void upload(final String baseURL,
                       final String folder,
                       final String fileName,
                       final Command successCommand,
                       final Command errorCommand) {
        view.upload(baseURL,
                    folder,
                    fileName,
                    successCommand,
                    errorCommand);
    }
}
