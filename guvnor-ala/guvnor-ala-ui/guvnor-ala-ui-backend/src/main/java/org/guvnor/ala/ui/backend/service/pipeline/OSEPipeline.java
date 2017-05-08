/*
 * Copyright ${year} Red Hat, Inc. and/or its affiliates.
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

package org.guvnor.ala.ui.backend.service.pipeline;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.ala.ui.backend.service.ExecutablePipeline;
import org.guvnor.ala.ui.backend.service.ProviderTypeServiceImpl;
import org.guvnor.ala.ui.model.Pipeline;
import org.guvnor.ala.ui.model.Provider;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.service.ProviderTypeService;

@ApplicationScoped
public class OSEPipeline implements ExecutablePipeline {

    @Override
    public ProviderType getSupportProviderType( ) {
        return ProviderTypeServiceImpl.OSE;
    }

    @Override
    public String getName( ) {
        return "OpenShift Deploy";
    }

    @Override
    public void run( Pipeline pipe, Runtime runtime, Provider provider ) {
        System.out.println( "RUN WF PIPELINE: " + pipe.getKey().getId() );
    }
}
