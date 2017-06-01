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

package org.guvnor.ala.ui.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.Instance;

import org.guvnor.ala.ui.model.ProviderTypeKey;

public abstract class AbstractProviderHandlerRegistry<T extends ProviderHandler> {

    protected Instance<T> handlerInstance;

    protected List<T> handlers = new ArrayList<>();

    protected void setUp() {
        handlerInstance.forEach(handlers::add);
    }

    public boolean isProviderEnabled(ProviderTypeKey providerTypeKey) {
        return getProviderHandler(providerTypeKey) != null;
    }

    public T getProviderHandler(ProviderTypeKey providerTypeKey) {
        return handlers.stream()
                .filter(handler -> handler.acceptProviderType(providerTypeKey))
                .sorted((o1, o2) -> o1.getPriority() - o2.getPriority())
                .findFirst().orElse(null);
    }

    public List<T> getProviderHandlers() {
        return Collections.unmodifiableList(handlers);
    }
}