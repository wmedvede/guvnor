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

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.util.AbstractHasContentChangeHandlers;
import org.guvnor.ala.ui.client.util.HasContentChangeHandlers;
import org.guvnor.ala.ui.client.widget.FormStatus;
import org.guvnor.ala.ui.client.handler.ProviderConfigurationForm;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.Provider;
import org.guvnor.ala.ui.model.ProviderConfiguration;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.client.mvp.UberElement;

import static org.uberfire.commons.validation.PortablePreconditions.*;

@Dependent
public class OSEProviderConfigPresenter
        extends AbstractHasContentChangeHandlers
        implements ProviderConfigurationForm {

    public interface View extends UberElement<OSEProviderConfigPresenter>,
                                  HasContentChangeHandlers {

        void setContent(final String name,
                        final String masterURL,
                        final String username,
                        final String password);

        String getName();

        String getMasterURL();

        String getUsername();

        String getPassword();

        void disable();

        void enable();

        void setProviderNameStatus(final FormStatus formStatus);

        void setMasterURLStatus(FormStatus error);

        void setUsernameStatus(FormStatus error);

        void setPasswordStatus(FormStatus error);

        void clear();

        String getWizardTitle();
    }

    private final View view;

    @Inject
    public OSEProviderConfigPresenter(final View view) {
        this.view = view;
    }

    @PostConstruct
    public void init() {
        view.init(this);
        view.addContentChangeHandler(this::onContentChange);
    }

    public View getView() {
        return view;
    }

    @Override
    public ProviderConfiguration buildProviderConfiguration() {
        final Map values = new HashMap<>();
        values.put("master-url",
                   getMasterURL());
        values.put("username",
                   getUsername());
        values.put("password",
                   getPassword());
        return new ProviderConfiguration(getName(),
                                         values);
    }

    @Override
    public void clear() {
        view.clear();
    }

    public void setup(final String providerTypeId) {
        checkNotEmpty("providerTypeId",
                      providerTypeId);
        checkCondition("providerTypeId",
                       providerTypeId.equals(ProviderType.OPEN_SHIFT_PROVIDER_TYPE));
    }

    public void setup(final String providerTypeId,
                      final String name,
                      final String masterURL,
                      final String username,
                      final String password) {
        setup(providerTypeId);
        view.setContent(checkNotEmpty("name",
                                      name),
                        checkNotEmpty("masterURL",
                                      masterURL),
                        checkNotEmpty("username",
                                      username),
                        checkNotEmpty("password",
                                      password));
    }

    public String getName() {
        return view.getName();
    }

    public String getMasterURL() {
        return view.getMasterURL();
    }

    public String getUsername() {
        return view.getUsername();
    }

    public String getPassword() {
        return view.getPassword();
    }

    public void isValid(final Callback<Boolean> callback) {
        boolean isValid = true;
        if (getName().trim().isEmpty()) {
            view.setProviderNameStatus(FormStatus.ERROR);
            isValid = false;
        } else {
            view.setProviderNameStatus(FormStatus.VALID);
        }

        if (getMasterURL().trim().isEmpty()) {
            view.setMasterURLStatus(FormStatus.ERROR);
            isValid = false;
        } else {
            view.setMasterURLStatus(FormStatus.VALID);
        }

        if (getUsername().trim().isEmpty()) {
            view.setUsernameStatus(FormStatus.ERROR);
            isValid = false;
        } else {
            view.setUsernameStatus(FormStatus.VALID);
        }

        if (getPassword().trim().isEmpty()) {
            view.setPasswordStatus(FormStatus.ERROR);
            isValid = false;
        } else {
            view.setPasswordStatus(FormStatus.VALID);
        }

        callback.callback(isValid);
    }

    @Override
    public String getWizardTitle() {
        return view.getWizardTitle();
    }

    @Override
    public void disable() {
        view.disable();
    }

    @Override
    public void load(final Provider provider) {
        view.setContent(provider.getKey().getId(),
                        provider.getConfiguration().getValues().get("master-url").toString(),
                        provider.getConfiguration().getValues().get("username").toString(),
                        "***");
    }

    private void onContentChange() {
        fireChangeHandlers();
    }
}
