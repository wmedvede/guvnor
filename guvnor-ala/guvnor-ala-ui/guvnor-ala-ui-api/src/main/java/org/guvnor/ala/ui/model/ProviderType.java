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
public class ProviderType
        extends ProviderTypeKey {

    //TODO, check if we wants to keep and use this constants, ideally we shouldn't since the provider type
    //might be something that is dynamically returned from server. And different versions from the same
    //provider ming required different parameters etc, but for now that we'll only have two providers it's fine.
    public static final String OPEN_SHIFT_PROVIDER_TYPE = "openshift";

    public static final String WILDFY_PROVIDER_TYPE = "wildfly";

    public static final String DOCKER_PROVIDER_TYPE = "docker";

    private String name;

    private String imageURL;

    public ProviderType( @MapsTo( "id" ) String id,
                         @MapsTo( "name" ) String name,
                         @MapsTo( "imageURL" ) String imageURL ) {
        super( id );
        this.name = name;
        this.imageURL = imageURL;
    }

    public String getName( ) {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getImageURL( ) {
        return imageURL;
    }

    public void setImageURL( String imageURL ) {
        this.imageURL = imageURL;
    }
}