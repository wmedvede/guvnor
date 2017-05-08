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
public class ProviderKey {

    private ProviderTypeKey providerTypeKey;
    private String id;
    private String name;

    public ProviderKey(@MapsTo("providerTypeKey") final ProviderTypeKey providerTypeKey,
                       @MapsTo("id") final String id,
                       @MapsTo("name") final String name) {
        this.providerTypeKey = providerTypeKey;
        this.id = id;
        this.name = name;
    }

    public ProviderTypeKey getProviderTypeKey() {
        return providerTypeKey;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProviderKey that = (ProviderKey) o;

        if (providerTypeKey != null ? !providerTypeKey.equals(that.providerTypeKey) : that.providerTypeKey != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = providerTypeKey != null ? providerTypeKey.hashCode() : 0;
        result = ~~result;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = ~~result;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = ~~result;
        return result;
    }
}