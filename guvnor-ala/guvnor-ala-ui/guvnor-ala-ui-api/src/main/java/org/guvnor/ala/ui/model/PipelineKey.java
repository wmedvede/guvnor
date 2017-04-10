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

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class PipelineKey {

    private String id;

    private RuntimeKey runtimeKey;

    public PipelineKey( @MapsTo( "id" ) final String id,
                        @MapsTo( "runtimeKey" ) final RuntimeKey runtimeKey ) {
        this.id = id;
        this.runtimeKey = runtimeKey;
    }

    public String getId( ) {
        return id;
    }

    public RuntimeKey getRuntimeKey( ) {
        return runtimeKey;
    }

    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof PipelineKey ) ) {
            return false;
        }

        final PipelineKey that = ( PipelineKey ) o;

        if ( !id.equals( that.id ) ) {
            return false;
        }
        return runtimeKey.equals( that.runtimeKey );

    }

    @Override
    public int hashCode( ) {
        int result = id.hashCode( );
        result = ~~result;
        result = 31 * result + runtimeKey.hashCode( );
        result = ~~result;
        return result;
    }
}
