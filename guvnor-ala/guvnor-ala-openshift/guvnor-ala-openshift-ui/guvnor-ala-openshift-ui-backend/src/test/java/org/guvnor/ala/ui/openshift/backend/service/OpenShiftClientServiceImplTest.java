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

package org.guvnor.ala.ui.openshift.backend.service;

import org.guvnor.ala.ui.openshift.model.DefaultSettings;
import org.guvnor.ala.ui.openshift.service.OpenshiftClientService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.io.IOService;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class OpenShiftClientServiceImplTest {

    public static final String DEFAULT_OPEN_SHIFT_TEMPLATE_VALUE = "DEFAULT_OPEN_SHIFT_TEMPLATE_VALUE";

    public static final String DEFAULT_OPEN_SHIFT_IMAGE_STREAMS_VALUE = "DEFAULT_OPEN_SHIFT_IMAGE_STREAMS_VALUE";

    public static final String DEFAULT_OPEN_SHIFT_SECRETS_VALUE = "DEFAULT_OPEN_SHIFT_SECRETS_VALUE";

    private OpenshiftClientService service;

    @Mock
    private IOService ioService;

    @Before
    public void setUp() {
        service = new OpenShiftClientServiceImpl(ioService);
    }

    @Test
    public void testGetDefaultSettings() {
        System.getProperties().setProperty(DefaultSettings.DEFAULT_OPEN_SHIFT_TEMPLATE,
                                           DEFAULT_OPEN_SHIFT_TEMPLATE_VALUE);
        System.getProperties().setProperty(DefaultSettings.DEFAULT_OPEN_SHIFT_IMAGE_STREAMS,
                                           DEFAULT_OPEN_SHIFT_IMAGE_STREAMS_VALUE);
        System.getProperties().setProperty(DefaultSettings.DEFAULT_OPEN_SHIFT_SECRETS,
                                           DEFAULT_OPEN_SHIFT_SECRETS_VALUE);

        DefaultSettings defaultSettings = service.getDefaultSettings();
        assertEquals(DEFAULT_OPEN_SHIFT_TEMPLATE_VALUE,
                     defaultSettings.getValue(DefaultSettings.DEFAULT_OPEN_SHIFT_TEMPLATE));
        assertEquals(DEFAULT_OPEN_SHIFT_IMAGE_STREAMS_VALUE,
                     defaultSettings.getValue(DefaultSettings.DEFAULT_OPEN_SHIFT_IMAGE_STREAMS));
        assertEquals(DEFAULT_OPEN_SHIFT_SECRETS_VALUE,
                     defaultSettings.getValue(DefaultSettings.DEFAULT_OPEN_SHIFT_SECRETS));
    }
}
