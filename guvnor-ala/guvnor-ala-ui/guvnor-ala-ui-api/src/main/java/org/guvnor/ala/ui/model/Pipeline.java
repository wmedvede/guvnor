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

package org.guvnor.ala.ui.model;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class Pipeline
        extends PipelineKey {

    private List< Step > steps = new ArrayList<>( );

    public Pipeline( @MapsTo( "id" ) String id,
                     @MapsTo( "runtimeKey" ) RuntimeKey runtimeKey,
                     @MapsTo( "steps" ) List< Step > steps ) {
        super ( id, runtimeKey );
        this.steps = steps;
    }

    public Pipeline( final String id,
                     final RuntimeKey runtimeKey ) {
        super( id, runtimeKey );
    }

    public List< Step > getSteps( ) {
        return steps;
    }

    public void addStep( final Step step ) {
        steps.add( step );
    }

}