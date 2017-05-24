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
public class ProviderTypeKey {

    private String id;

    public ProviderTypeKey( @MapsTo( "id" ) final String id ) {
        this.id = id;
    }

    public String getId( ) {
        return id;
    }

    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof ProviderTypeKey ) ) {
            return false;
        }

        final ProviderTypeKey that = ( ProviderTypeKey ) o;

        return id.equals( that.id );

    }

    @Override
    public int hashCode( ) {
        int result = id.hashCode();
        result = ~~result;
        return result;
    }

    @Override
    public String toString( ) {
        return "ProviderTypeKey{" +
                "id='" + id + '\'' +
                '}';
    }
}