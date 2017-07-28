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

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.util.AbstractHasContentChangeHandlers;
import org.guvnor.ala.ui.client.widget.FormStatus;
import org.guvnor.ala.ui.client.wizard.pipeline.params.PipelineParamsForm;
import org.guvnor.ala.ui.model.PipelineKey;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.client.mvp.UberElement;

@Dependent
public class OSEPipelineParamsFormPresenter
        extends AbstractHasContentChangeHandlers
        implements PipelineParamsForm {

    protected static final String KIE_SERVER_USER = "KIE_SERVER_USER";

    protected static final String KIE_SERVER_PWD = "KIE_SERVER_PWD";

    public interface View
            extends UberElement<OSEPipelineParamsFormPresenter> {

        String getKieServerUser();

        String getKieServerUserPassword();

        void setKieServerUser(String name);

        void setKieServerUserPassword(String kieServerUserPassword);

        void disable();

        void enable();

        void setKieServerUserStatus(FormStatus formStatus);

        void setKieServerUserPasswordStatus(FormStatus error);

        void clear();

        String getWizardTitle();
    }

    private final View view;

    @Inject
    public OSEPipelineParamsFormPresenter(final View view) {
        this.view = view;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public Map<String, String> buildParams() {
        final Map<String, String> params = new HashMap<>();
        params.put(KIE_SERVER_USER,
                   getKieServerUser());
        params.put(KIE_SERVER_PWD,
                   getKieServerUserPassword());
        return params;
    }

    @Override
    public void clear() {
        view.clear();
    }

    public String getKieServerUser() {
        return view.getKieServerUser();
    }

    public String getKieServerUserPassword() {
        return view.getKieServerUserPassword();
    }

    @Override
    public void isValid(final Callback<Boolean> callback) {
        boolean isValid = !isEmpty(view.getKieServerUser()) &&
                !isEmpty(view.getKieServerUserPassword());
        callback.callback(isValid);
    }

    @Override
    public String getWizardTitle() {
        return view.getWizardTitle();
    }

    protected void onKieServerUserChange() {
        if (!isEmpty(view.getKieServerUser())) {
            view.setKieServerUserStatus(FormStatus.VALID);
        } else {
            view.setKieServerUserStatus(FormStatus.ERROR);
        }
        onContentChange();
    }

    protected void onKieServerUserPasswordChange() {
        if (!isEmpty(view.getKieServerUserPassword())) {
            view.setKieServerUserPasswordStatus(FormStatus.VALID);
        } else {
            view.setKieServerUserPasswordStatus(FormStatus.ERROR);
        }
        onContentChange();
    }

    private void onContentChange() {
        fireChangeHandlers();
    }

    @Override
    public boolean accept(PipelineKey pipelineKey) {
        return pipelineKey != null && "pipeline from stages".equals(pipelineKey.getId());
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
