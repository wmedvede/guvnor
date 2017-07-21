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

package org.guvnor.ala.ui.openshift.client.pipeline.url;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import org.guvnor.ala.ui.openshift.client.popup.BaseOkCancelPopup;
import org.jboss.errai.common.client.dom.Input;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;

@Dependent
public class URLPopup
        extends BaseOkCancelPopup {

    private ParameterizedCommand<String> onURLSuccessCommand;

    private Command onCancelCommand;

    private Input urlField;

    @Inject
    public URLPopup(final BaseOkCancelPopup.View view,
                    final Input urlField) {
        super(view);
        this.urlField = urlField;
    }

    @PostConstruct
    @Override
    public void init() {
        super.init();
        urlField.setClassName("form-control");
        view.setContent(urlField);
    }

    public void show(final String title,
                     final ParameterizedCommand<String> onURLSuccessCommand,
                     final Command onCancelCommand) {
        this.onURLSuccessCommand = onURLSuccessCommand;
        this.onCancelCommand = onCancelCommand;
        super.show(title);
    }

    @Override
    protected void onOK() {
        String url = urlField.getValue();
        if (url.isEmpty()) {
            Window.alert("No URL has been entered");
        } else {
            onURLSuccessCommand.execute(url);
            hide();
        }
    }

    @Override
    protected void onCancel() {
        super.onCancel();
        onCancelCommand.execute();
    }
}