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

package org.guvnor.ala.ui.wildfly.client.handler.provider;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.handler.ProviderConfigurationForm;
import org.guvnor.ala.ui.client.util.AbstractHasContentChangeHandlers;
import org.guvnor.ala.ui.client.widget.FormStatus;
import org.guvnor.ala.ui.model.ITestConnectionResult;
import org.guvnor.ala.ui.model.Provider;
import org.guvnor.ala.ui.model.ProviderConfiguration;
import org.guvnor.ala.ui.wildlfy.service.WildflyClientService;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.ext.widgets.common.client.common.popups.YesNoCancelPopup;
import org.uberfire.ext.widgets.common.client.common.popups.errors.ErrorPopup;

import static org.guvnor.ala.ui.client.util.UIUtil.getStringValue;

@Dependent
public class WF10ProviderConfigPresenter
        extends AbstractHasContentChangeHandlers
        implements ProviderConfigurationForm {

    private static final String HOST = "host";

    private static final String PORT = "port";

    private static final String MANAGEMENT_PORT = "management-port";

    private static final String USER = "wildfly-user";

    private static final String PASSWORD = "wildfly-password";

    private static final String MANAGEMENT_REALM = "ManagementRealm";

    public interface View
            extends UberElement<WF10ProviderConfigPresenter> {

        String getProviderName();

        String getHost();

        String getPort();

        String getManagementPort();

        String getUsername();

        String getPassword();

        void setProviderName(String name);

        void setHost(String host);

        void setPort(String port);

        void setManagementPort(String managementPort);

        void setUsername(String username);

        void setPassword(String password);

        void disable();

        void enable();

        void setProviderNameStatus(final FormStatus formStatus);

        void setHostStatus(final FormStatus formStatus);

        void setPortStatus(final FormStatus formStatus);

        void setManagementPortStatus(final FormStatus formStatus);

        void setUsernameStatus(final FormStatus formStatus);

        void setPasswordStatus(final FormStatus formStatus);

        void clear();

        String getWizardTitle();
    }

    private final View view;
    private final Caller<WildflyClientService> wildflyClientService;

    @Inject
    public WF10ProviderConfigPresenter(final View view,
                                       final Caller<WildflyClientService> wildflyClientService) {
        this.view = view;
        this.wildflyClientService = wildflyClientService;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    public View getView() {
        return view;
    }

    @Override
    public ProviderConfiguration buildProviderConfiguration() {
        final Map<String, Object> values = new HashMap<>();
        values.put(HOST,
                   getHost());
        values.put(PORT,
                   getPort());
        values.put(MANAGEMENT_PORT,
                   getManagementPort());
        values.put(USER,
                   getUsername());
        values.put(PASSWORD,
                   getPassword());
        return new ProviderConfiguration(getProviderName(),
                                         values);
    }

    @Override
    public void clear() {
        view.clear();
    }

    @Override
    public void load(final Provider provider) {
        clear();
        view.setProviderName(provider.getKey().getId());
        view.setHost(getStringValue(provider.getConfiguration().getValues(),
                                    HOST));
        view.setPort(getStringValue(provider.getConfiguration().getValues(),
                                    PORT));
        view.setManagementPort(getStringValue(provider.getConfiguration().getValues(),
                                              MANAGEMENT_PORT));
        view.setUsername(getStringValue(provider.getConfiguration().getValues(),
                                        USER));
        view.setPassword("****");
    }

    public String getProviderName() {
        return view.getProviderName();
    }

    public String getHost() {
        return view.getHost();
    }

    public String getPort() {
        return view.getPort();
    }

    public String getManagementPort() {
        return view.getManagementPort();
    }

    public String getUsername() {
        return view.getUsername();
    }

    public String getPassword() {
        return view.getPassword();
    }

    public void isValid(final Callback<Boolean> callback) {
        boolean isValid = !isEmpty(view.getProviderName()) &&
                !isEmpty(view.getHost()) &&
                isValidPort(view.getPort()) &&
                isValidPort(view.getManagementPort()) &&
                !isEmpty(view.getUsername()) &&
                !isEmpty(view.getPassword());
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

    private void onContentChange() {
        fireChangeHandlers();
    }

    protected void onProviderNameChange() {
        if (!isEmpty(view.getProviderName())) {
            view.setProviderNameStatus(FormStatus.VALID);
        } else {
            view.setProviderNameStatus(FormStatus.ERROR);
        }
        onContentChange();
    }

    protected void onHostChange() {
        if (!isEmpty(view.getHost())) {
            view.setHostStatus(FormStatus.VALID);
        } else {
            view.setHostStatus(FormStatus.ERROR);
        }
        onContentChange();
    }

    protected void onPortChange() {
        if (isValidPort(view.getPort())) {
            view.setPortStatus(FormStatus.VALID);
        } else {
            view.setPortStatus(FormStatus.ERROR);
        }
        onContentChange();
    }

    protected void onManagementPortChange() {
        if (isValidPort(view.getManagementPort())) {
            view.setManagementPortStatus(FormStatus.VALID);
        } else {
            view.setManagementPortStatus(FormStatus.ERROR);
        }
        onContentChange();
    }

    protected void onUserNameChange() {
        if (!isEmpty(view.getUsername())) {
            view.setUsernameStatus(FormStatus.VALID);
        } else {
            view.setUsernameStatus(FormStatus.ERROR);
        }
        onContentChange();
    }

    protected void onPasswordChange() {
        if (!isEmpty(view.getPassword())) {
            view.setPasswordStatus(FormStatus.VALID);
        } else {
            view.setPasswordStatus(FormStatus.ERROR);
        }
        onContentChange();
    }

    protected void onTestConnection() {
        if (validateRemoteParams()) {
            wildflyClientService.call(getTestConnectionSuccessCallback(),
                                      getTestConnectionErrorCallback()).testConnection(view.getHost(),
                                                                                       getInt(view.getPort()),
                                                                                       getInt(view.getManagementPort()),
                                                                                       view.getUsername(),
                                                                                       view.getPassword(),
                                                                                       MANAGEMENT_REALM);
        }
    }

    private RemoteCallback<ITestConnectionResult> getTestConnectionSuccessCallback() {
        return response -> {
            String message = response.getManagementConnectionError() ?
                    translateMessage(TestConnectionFailMessage,
                                     response.getManagementConnectionMessage()) :
                    translateMessage(TestConnectionSuccessfulMessage,
                                     response.getManagementConnectionMessage());
            YesNoCancelPopup.newYesNoCancelPopup("Information",
                                                 message,
                                                 null,
                                                 null,
                                                 (org.uberfire.mvp.Command) () -> {
                                                     //do nothing.
                                                 }).show();
        };
    }

    private ErrorCallback<Message> getTestConnectionErrorCallback() {
        return (message, throwable) -> {
            ErrorPopup.showMessage("An error was produced during connection test: " + throwable.getMessage());
            return false;
        };
    }

    private String translateMessage(String msg,
                                    String content) {
        return msg + content;
    }

    private static final String TestConnectionSuccessfulMessage = "<strong>Management connection test successful:</strong><BR>";
    private static final String TestConnectionFailMessage = "<strong>Management connection test failed:</strong><BR>";

    private boolean validateRemoteParams() {
        boolean result = !isEmpty(view.getHost()) &&
                isValidPort(view.getPort()) &&
                isValidPort(view.getManagementPort()) &&
                !isEmpty(view.getUsername()) &&
                !isEmpty(view.getPassword());
        if (!result) {
            YesNoCancelPopup.newYesNoCancelPopup("Information",
                                                 "All parameters must be completed for performing validation",
                                                 null,
                                                 null,
                                                 () -> {
                                                     //do nothing.
                                                 }).show();
            return false;
        }
        return true;
    }

    private boolean isValidPort(String port) {
        int value = getInt(port);
        return value > 0 && value <= 65535;
    }

    private int getInt(String port) {
        if (port == null || port.trim().isEmpty()) {
            return -1;
        }
        int value = -1;
        try {
            value = Integer.parseInt(port.trim());
        } catch (Exception e) {
        }
        return value;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}