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

package org.guvnor.ala.provisioning.pipelines.service;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenShiftRuntimeSettings {

    private static final Logger logger = LoggerFactory.getLogger(OpenShiftRuntimeSettings.class);

    private static final String OPEN_SHIFT_RUNTIME_SETTINGS = "openshift-runtime-settings.properties";

    private static OpenShiftRuntimeSettings instance;

    private Properties properties;

    private OpenShiftRuntimeSettings(Properties properties) {
        this.properties = properties;
    }

    public static OpenShiftRuntimeSettings getInstance() {
        if (instance == null) {
            instance = new OpenShiftRuntimeSettings(loadSettings());
        }
        return instance;
    }

    public String getProperty(String value) {
        return properties.getProperty(value);
    }

    public Properties getProperties() {
        return properties;
    }

    private static Properties loadSettings() {
        InputStream inputStream =
                OpenShiftRuntimeSettings.class.getResourceAsStream("/" + OPEN_SHIFT_RUNTIME_SETTINGS);

        Properties properties = new Properties();
        if (inputStream == null) {
            logger.debug("Configuration file " + OPEN_SHIFT_RUNTIME_SETTINGS + " was not found.");
            return properties;
        }

        try {
            properties.load(inputStream);
        } catch (Exception e) {
            logger.error("An error was produced during while loading openshift runtime settings file: " +
                                 OPEN_SHIFT_RUNTIME_SETTINGS,
                         e);
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                logger.warn("An error was produced during openshift configuration file closing: " +
                                    OPEN_SHIFT_RUNTIME_SETTINGS,
                            e);
            }
        }
        return properties;
    }
}