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

package org.guvnor.ala.ui.backend.service.util;

import javax.enterprise.context.ApplicationScoped;

import org.guvnor.ala.ui.model.ProviderTypeKey;

@ApplicationScoped
public class ProviderTypeServiceHelper {

    public static final String WF_10_ICON = "images/wf.png";

    public static final String OSE_ICON = "images/ose.png";

    public static final String DOCKER_ICON = "images/docker.png";

    public ProviderTypeServiceHelper() {
        //Empty constructor for Weld proxying
    }

    public String getProviderTypeIcon(ProviderTypeKey providerTypeKey) {
        String icon = "images/wf.png";
        if (providerTypeKey.getId().toLowerCase().startsWith("wildfly")) {
            icon = WF_10_ICON;
        } else if (providerTypeKey.getId().toLowerCase().startsWith("open")) {
            icon = OSE_ICON;
        } else if (providerTypeKey.getId().toLowerCase().startsWith("docker")) {
            icon = DOCKER_ICON;
        }
        return icon;
    }
}
