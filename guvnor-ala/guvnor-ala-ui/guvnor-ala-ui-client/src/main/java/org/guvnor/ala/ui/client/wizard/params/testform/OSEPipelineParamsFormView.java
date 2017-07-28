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

package org.guvnor.ala.ui.client.wizard.params.testform;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.widget.FormStatus;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Event;
import org.jboss.errai.common.client.dom.TextInput;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import static org.guvnor.ala.ui.client.widget.StyleHelper.setFormStatus;
import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

@Dependent
@Templated
public class OSEPipelineParamsFormView
        implements IsElement,
                   OSEPipelineParamsFormPresenter.View {

    @Inject
    @DataField("kie-server-user-form")
    private Div kieServerUserForm;

    @Inject
    @DataField("kie-server-user")
    private TextInput kieServerUser;

    @Inject
    @DataField("kie-server-user-password-form")
    private Div kieServerUserPasswordForm;

    @Inject
    @DataField("kie-server-user-password")
    private TextInput kieServerUserPassword;

    private OSEPipelineParamsFormPresenter presenter;

    @Override
    public void init(final OSEPipelineParamsFormPresenter presenter) {
        this.presenter = presenter;
    }

    @PostConstruct
    private void init() {
    }

    @Override
    public String getKieServerUser() {
        return kieServerUser.getValue();
    }

    @Override
    public void setKieServerUser(String kieServerUser) {
        this.kieServerUser.setValue(kieServerUser);
    }

    @Override
    public String getKieServerUserPassword() {
        return kieServerUserPassword.getValue();
    }

    @Override
    public void setKieServerUserPassword(String kieServerUserPassword) {
        this.kieServerUserPassword.setValue(kieServerUserPassword);
    }

    @Override
    public void disable() {
        resetFormState();
        enable(false);
    }

    @Override
    public void enable() {
        resetFormState();
        enable(true);
    }

    @Override
    public void setKieServerUserStatus(final FormStatus status) {
        checkNotNull("status",
                     status);
        setFormStatus(kieServerUserForm,
                      status);
    }

    @Override
    public void setKieServerUserPasswordStatus(final FormStatus status) {
        checkNotNull("status",
                     status);
        setFormStatus(kieServerUserPasswordForm,
                      status);
    }

    @Override
    public void clear() {
        resetFormState();
        this.kieServerUser.setValue("");
        this.kieServerUserPassword.setValue("");
    }

    @Override
    public String getWizardTitle() {
        //do not internationalize this value.
        return "Kie Server Params";
    }

    @EventHandler("kie-server-user")
    private void onKieServerUserChange(@ForEvent("change") final Event event) {
        presenter.onKieServerUserChange();
    }

    @EventHandler("kie-server-user-password-form")
    private void onKieServerUserPasswordChange(@ForEvent("change") final Event event) {
        presenter.onKieServerUserPasswordChange();
    }

    private void resetFormState() {
        setFormStatus(kieServerUserForm,
                      FormStatus.VALID);
        setFormStatus(kieServerUserPasswordForm,
                      FormStatus.VALID);
    }

    private void enable(boolean enabled) {
        this.kieServerUser.setDisabled(!enabled);
        this.kieServerUserPassword.setDisabled(!enabled);
    }
}