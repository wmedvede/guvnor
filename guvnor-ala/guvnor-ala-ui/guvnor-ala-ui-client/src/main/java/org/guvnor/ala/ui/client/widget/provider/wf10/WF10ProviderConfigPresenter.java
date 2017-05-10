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

package org.guvnor.ala.ui.client.widget.provider.wf10;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.util.ContentChangeHandler;
import org.guvnor.ala.ui.client.widget.FormStatus;
import org.guvnor.ala.ui.client.widget.provider.FormProvider;
import org.guvnor.ala.ui.model.ITestConnectionResult;
import org.guvnor.ala.ui.model.Provider;
import org.guvnor.ala.ui.model.ProviderConfiguration;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.WF10ProviderConfigParams;
import org.guvnor.ala.ui.service.IProvisioningService;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.ext.widgets.common.client.common.popups.YesNoCancelPopup;
import org.uberfire.ext.widgets.common.client.common.popups.errors.ErrorPopup;

import static org.guvnor.ala.ui.client.util.UIUtil.getStringValue;
import static org.uberfire.commons.validation.PortablePreconditions.checkCondition;
import static org.uberfire.commons.validation.PortablePreconditions.checkNotEmpty;

@Dependent
public class WF10ProviderConfigPresenter
        implements FormProvider,
                   WF10ProviderConfigParams {

    public interface View extends UberElement< WF10ProviderConfigPresenter > {

        void setContent(final String name,
                        final String host,
                        final String port,
                        final String managementPort,
                        final String username,
                        final String password);

        String getName();

        String getHost();

        String getPort();

        String getManagementPort();

        String getUsername();

        String getPassword();

        void disable();

        void enable();

        void setProviderNameStatus(final FormStatus formStatus);

        void setHostStatus(final FormStatus formStatus);

        void setPortStatus(final FormStatus formStatus);

        void setManagementPortStatus(final FormStatus formStatus);

        void setUsernameStatus(final FormStatus formStatus);

        void setPasswordStatus(final FormStatus formStatus);

        void clear();

        void addContentChangeHandler(ContentChangeHandler contentChangeHandler);

        String getWizardTitle();
    }

    private final View view;
    private Caller<IProvisioningService> provisioningService;

    @Inject
    public WF10ProviderConfigPresenter(final View view, final Caller<IProvisioningService> provisioningService) {
        this.view = view;
        this.provisioningService = provisioningService;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    public View getView() {
        return view;
    }

    @Override
    public void addContentChangeHandler(final ContentChangeHandler contentChangeHandler) {
        view.addContentChangeHandler(contentChangeHandler);
    }

    @Override
    public ProviderConfiguration buildProviderConfiguration() {
        final Map values = new HashMap<>();
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
        return new ProviderConfiguration(getName(),
                                         getName(),
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
                       providerTypeId.equals(ProviderType.WILDFY_PROVIDER_TYPE));
    }

    public void setup(final String providerTypeId,
                      final String name,
                      final String host,
                      final String port,
                      final String managementPort,
                      final String username,
                      final String password) {
        setup(providerTypeId);
        view.setContent(checkNotEmpty(PROVIDER_NAME,
                                      name),
                        checkNotEmpty(HOST,
                                      host),
                        checkNotEmpty(PORT,
                                      port),
                        checkNotEmpty(MANAGEMENT_PORT,
                                      managementPort),
                        checkNotEmpty(USER,
                                      username),
                        checkNotEmpty(PASSWORD,
                                      password));
    }

    public String getName() {
        return view.getName();
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

    public void isValid(final Callback< Boolean > callback) {
        boolean isValid = true;
        if (getName().trim().isEmpty()) {
            view.setProviderNameStatus(FormStatus.ERROR);
            isValid = false;
        } else {
            view.setProviderNameStatus(FormStatus.VALID);
        }

        if (getHost().trim().isEmpty()) {
            view.setHostStatus(FormStatus.ERROR);
            isValid = false;
        } else {
            view.setHostStatus(FormStatus.VALID);
        }

        if (getPort().trim().isEmpty()) {
            view.setPortStatus(FormStatus.ERROR);
            isValid = false;
        } else {
            try {
                Integer.valueOf(getPort().trim());
                view.setPortStatus(FormStatus.VALID);
            } catch (Exception ex) {
                view.setPortStatus(FormStatus.ERROR);
                isValid = false;
            }
        }

        if (getManagementPort().trim().isEmpty()) {
            view.setManagementPortStatus(FormStatus.ERROR);
            isValid = false;
        } else {
            try {
                Integer.valueOf(getManagementPort().trim());
                view.setManagementPortStatus(FormStatus.VALID);
            } catch (Exception ex) {
                view.setManagementPortStatus(FormStatus.ERROR);
                isValid = false;
            }
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
        view.setContent(provider.getKey().getName(),
                        getStringValue(provider.getValues(),
                                       HOST),
                        getStringValue(provider.getValues(),
                                       PORT),
                        getStringValue(provider.getValues(),
                                       MANAGEMENT_PORT),
                        getStringValue(provider.getValues(),
                                       USER),
                        "****");
    }

    public void onTestConnection( ) {
        if ( validateRemoteParams( ) ) {
            provisioningService.call( getTestConnectionSuccessCallback(),
                                      getTestConnectionErrorCallback() ).testConnection( view.getHost( ),
                                                                                         getInt(view.getPort( )),
                                                                                         getInt(view.getManagementPort( )),
                                                                                         view.getUsername(),
                                                                                         view.getPassword(),
                                                                                         "ManagementRealm" );
        }
    }

    private RemoteCallback<ITestConnectionResult> getTestConnectionSuccessCallback() {
        return response -> {
            String message = response.getManagementConnectionError( ) ?
                    translateMessage( TestConnectionFailMessage, response.getManagementConnectionMessage( ) ) :
                    translateMessage( TestConnectionSuccessfulMessage, response.getManagementConnectionMessage( ) );
            YesNoCancelPopup.newYesNoCancelPopup("Information", message, null, null, ( org.uberfire.mvp.Command ) ( ) -> {
                //do nothing.
            } ).show( );
        };
    }

    private ErrorCallback< Message > getTestConnectionErrorCallback() {
        return (message, throwable) -> {
            ErrorPopup.showMessage("An error was produced during connection test: " + throwable.getMessage());
            return false;
        };
    }

    private String translateMessage( String msg, String content ) {
        return msg + content;
    }
    private static final String TestConnectionSuccessfulMessage="<strong>Management connection test successful:</strong><BR>";
    private static final String TestConnectionFailMessage="<strong>Management connection test failed:</strong><BR>";


    private boolean validateRemoteParams( ) {
        boolean result = !isEmpty( view.getHost() ) &&
                isValidPort( view.getPort() ) &&
                isValidPort( view.getManagementPort() ) &&
                !isEmpty( view.getUsername() ) &&
                !isEmpty( view.getPassword() );
        if ( !result ) {
            YesNoCancelPopup.newYesNoCancelPopup("Information",
                                                 "All parameters must be completed for performing validation",
                                                 null,
                                                 null,
                                                 ( ) -> {
                //do nothing.
            }).show( );
            return false;
        }
        return true;
    }

    private boolean isValidPort( String port ) {
        int value = getInt(port);
        return value > 0 && value <= 65535;
    }

    private int getInt( String port ) {
        if ( port == null || port.trim().isEmpty() ) return -1;
        int value = -1;
        try {
            value = Integer.parseInt( port.trim() );
        } catch (Exception e) {
        }
        return value;
    }


    private boolean isEmpty( String value ) {
        return value == null || value.trim( ).isEmpty( );
    }

}