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

package org.guvnor.ala.ui.client.resources.i18n;

import org.jboss.errai.ui.shared.api.annotations.TranslationKey;

public class GuvnorAlaUIConstants {

    @TranslationKey(defaultValue = "")
    public static final String ProvisioningManagementBrowserView_title = "ProvisioningManagementBrowserView.title";

    @TranslationKey(defaultValue = "")
    public static final String EnableProviderTypeWizard_title = "EnableProviderTypeWizard.title";

    @TranslationKey(defaultValue = "")
    public static final String EnableProviderTypeWizard_ProviderTypeEnableSuccessMessage = "EnableProviderTypeWizard.ProviderTypeEnableSuccessMessage";

    @TranslationKey(defaultValue = "")
    public static final String EnableProviderTypeWizard_ProviderTypeEnableErrorMessage = "EnableProviderTypeWizard.ProviderTypeEnableErrorMessage";

    @TranslationKey(defaultValue = "")
    public static final String EnableProviderTypePageView_title = "EnableProviderTypePageView.title";

    @TranslationKey(defaultValue = "")
    public static final String NewProviderWizard_title = "NewProviderWizard.title";

    @TranslationKey(defaultValue = "")
    public static final String NewProviderWizard_ProviderCreateSuccessMessage = "NewProviderWizard.ProviderCreateSuccessMessage";

    @TranslationKey(defaultValue = "")
    public static final String NewProviderWizard_ProviderCreateErrorMessage = "NewProviderWizard.ProviderCreateErrorMessage";

    @TranslationKey(defaultValue = "")
    public static final String NewDeployWizard_title = "NewDeployWizard.title";

    @TranslationKey(defaultValue = "")
    public static final String NewDeployWizard_PipelineStartSuccessMessage = "NewDeployWizard.PipelineStartSuccessMessage";

    @TranslationKey(defaultValue = "")
    public static final String NewDeployWizard_PipelineStartErrorMessage = "NewDeployWizard.PipelineStartErrorMessage";

    @TranslationKey(defaultValue = "")
    public static final String SelectPipelinePageView_title = "SelectPipelinePageView.title";

    ////Not yet reviewed

    @TranslationKey(defaultValue = "")
    public static final String ProviderView_RemoveProviderSuccessMessage;

    static {
        ProviderView_RemoveProviderSuccessMessage = "ProviderView.RemoveProviderSuccessMessage";
    }

    @TranslationKey(defaultValue = "")
    public static final String ProviderView_RemoveProviderErrorMessage = "ProviderView.RemoveProviderErrorMessage";

    @TranslationKey(defaultValue = "")
    public static final String ProviderView_ConfirmRemovePopupMessage = "ProviderView.ConfirmRemovePopupMessage";

    @TranslationKey(defaultValue = "")
    public static final String ProviderView_ConfirmRemovePopupTitle = "ProviderView.ConfirmRemovePopupTitle";

    @TranslationKey(defaultValue = "")
    public static final String ProviderTypeView_ProviderTypeRemovePopupText = "ProviderTypeView.ProviderTypeRemovePopupText";

    @TranslationKey(defaultValue = "")
    public static final String ProviderTypeView_ProviderTypeRemovePopupTitle = "ProviderTypeView.ProviderTypeRemovePopupTitle";

    @TranslationKey(defaultValue = "")
    public static final String ProviderTypeNavigationView_TitleText = "ProviderTypeNavigationView.TitleText";
}
