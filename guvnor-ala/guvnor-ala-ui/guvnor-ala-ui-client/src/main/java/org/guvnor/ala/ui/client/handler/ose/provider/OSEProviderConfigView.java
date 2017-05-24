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

package org.guvnor.ala.ui.client.handler.ose.provider;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.util.AbstractHasContentChangeHandlers;
import org.guvnor.ala.ui.client.widget.FormStatus;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Event;
import org.jboss.errai.common.client.dom.TextInput;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import static org.guvnor.ala.ui.client.widget.StyleHelper.addUniqueEnumStyleName;
import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

@Dependent
@Templated
public class OSEProviderConfigView
        extends AbstractHasContentChangeHandlers
        implements IsElement,
                   OSEProviderConfigPresenter.View {

    @Inject
    @DataField("provider-name-form")
    private Div providerNameForm;

    @Inject
    @DataField("provider-name")
    private TextInput name;

    @Inject
    @DataField("master-url-form")
    private Div masterURLForm;

    @Inject
    @DataField("master-url")
    private TextInput masterURL;

    @Inject
    @DataField("username-form")
    private Div usernameForm;

    @Inject
    @DataField("username")
    private TextInput username;

    @Inject
    @DataField("password-form")
    private Div passwordForm;

    @Inject
    @DataField("password")
    private TextInput password;

    private OSEProviderConfigPresenter presenter;

    @Override
    public void init(final OSEProviderConfigPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setContent(final String name,
                           final String masterURL,
                           final String username,
                           final String password) {
        resetFormState();
        this.name.setValue(name);
        this.masterURL.setValue(masterURL);
        this.username.setValue(username);
        this.password.setValue(password);
    }

    @Override
    public String getName() {
        return name.getValue();
    }

    @Override
    public String getMasterURL() {
        return masterURL.getValue();
    }

    @Override
    public String getUsername() {
        return username.getValue();
    }

    @Override
    public String getPassword() {
        return password.getValue();
    }

    @EventHandler("provider-name")
    public void onProviderNameChange(@ForEvent("change") final Event event) {
        if (!name.getValue().trim().isEmpty()) {
            addUniqueEnumStyleName(providerNameForm,
                                   ValidationState.class,
                                   ValidationState.NONE);
        }
        fireChangeHandlers();
    }

    @EventHandler("master-url")
    public void onMasterURLChange(@ForEvent("change") final Event event) {
        if (!masterURL.getValue().trim().isEmpty()) {
            addUniqueEnumStyleName(masterURLForm,
                                   ValidationState.class,
                                   ValidationState.NONE);
        }
        fireChangeHandlers();
    }

    @EventHandler("username")
    public void onUsernameChange(@ForEvent("change") final Event event) {
        if (!username.getValue().trim().isEmpty()) {
            addUniqueEnumStyleName(usernameForm,
                                   ValidationState.class,
                                   ValidationState.NONE);
        }
        fireChangeHandlers();
    }

    @EventHandler("password")
    public void onPasswordChange(@ForEvent("change") final Event event) {
        if (!password.getValue().trim().isEmpty()) {
            addUniqueEnumStyleName(passwordForm,
                                   ValidationState.class,
                                   ValidationState.NONE);
        }
        fireChangeHandlers();
    }

    @Override
    public void disable() {
        resetFormState();
        this.name.setDisabled(true);
        this.masterURL.setDisabled(true);
        this.username.setDisabled(true);
        this.password.setDisabled(true);
    }

    @Override
    public void enable() {
        resetFormState();
        this.name.setDisabled(false);
        this.masterURL.setDisabled(false);
        this.username.setDisabled(false);
        this.password.setDisabled(false);
    }

    @Override
    public void setProviderNameStatus(final FormStatus status) {
        checkNotNull("status",
                     status);
        if (status.equals(FormStatus.ERROR)) {
            addUniqueEnumStyleName(providerNameForm,
                                   ValidationState.class,
                                   ValidationState.ERROR);
        } else {
            addUniqueEnumStyleName(providerNameForm,
                                   ValidationState.class,
                                   ValidationState.NONE);
        }
    }

    @Override
    public void setMasterURLStatus(final FormStatus status) {
        checkNotNull("status",
                     status);
        if (status.equals(FormStatus.ERROR)) {
            addUniqueEnumStyleName(masterURLForm,
                                   ValidationState.class,
                                   ValidationState.ERROR);
        } else {
            addUniqueEnumStyleName(masterURLForm,
                                   ValidationState.class,
                                   ValidationState.NONE);
        }
    }

    @Override
    public void setUsernameStatus(final FormStatus status) {
        checkNotNull("status",
                     status);
        if (status.equals(FormStatus.ERROR)) {
            addUniqueEnumStyleName(usernameForm,
                                   ValidationState.class,
                                   ValidationState.ERROR);
        } else {
            addUniqueEnumStyleName(usernameForm,
                                   ValidationState.class,
                                   ValidationState.NONE);
        }
    }

    @Override
    public void setPasswordStatus(final FormStatus status) {
        checkNotNull("status",
                     status);
        if (status.equals(FormStatus.ERROR)) {
            addUniqueEnumStyleName(passwordForm,
                                   ValidationState.class,
                                   ValidationState.ERROR);
        } else {
            addUniqueEnumStyleName(passwordForm,
                                   ValidationState.class,
                                   ValidationState.NONE);
        }
    }

    @Override
    public void clear() {
        resetFormState();
        this.name.setValue("");
        this.masterURL.setValue("");
        this.username.setValue("");
        this.password.setValue("");
    }

    @Override
    public String getWizardTitle() {
        return "OpenShift";
    }

    private void resetFormState() {
        addUniqueEnumStyleName(providerNameForm,
                               ValidationState.class,
                               ValidationState.NONE);
        addUniqueEnumStyleName(masterURLForm,
                               ValidationState.class,
                               ValidationState.NONE);
        addUniqueEnumStyleName(usernameForm,
                               ValidationState.class,
                               ValidationState.NONE);
        addUniqueEnumStyleName(passwordForm,
                               ValidationState.class,
                               ValidationState.NONE);
    }
}