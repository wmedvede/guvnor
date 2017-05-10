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

package org.guvnor.ala.ui.service;

import java.util.Collection;

import org.guvnor.ala.ui.model.ProviderTypeKey;
import org.guvnor.ala.ui.model.ProviderKey;
import org.guvnor.ala.ui.model.RuntimeKey;
import org.jboss.errai.bus.server.annotations.Remote;
import org.guvnor.ala.ui.model.Source;
import org.guvnor.ala.ui.model.Runtime;

@Remote
public interface RuntimeService {

    Runtime getRuntime(final RuntimeKey runtimeKey);

    Collection< Runtime > getRuntimes(final ProviderKey provider);

    Collection< String > getPipelines(final ProviderKey providerKey);

    Collection< String > getPipelines(final ProviderTypeKey providerTypeKey);

    void createRuntime(final ProviderKey provider,
                       final String runtimeId,
                       final Source source,
                       final String pipeline);

    void start(RuntimeKey runtimeKey);

    void stop(RuntimeKey runtimeKey);

    void rebuild(RuntimeKey runtimeKey);

    void delete(RuntimeKey runtimeKey);
}