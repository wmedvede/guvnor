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

package org.guvnor.ala.ui.events;

import org.guvnor.ala.ui.model.RuntimeKey;
import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Event for notifying the deletion of a runtime.
 */
@Portable
public class RuntimeDeletedEvent {

    private RuntimeKey runtimeKey;

    public RuntimeDeletedEvent(@MapsTo("runtimeKey") final RuntimeKey runtimeKey) {
        this.runtimeKey = runtimeKey;
    }

    public RuntimeKey getRuntimeKey() {
        return runtimeKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RuntimeDeletedEvent that = (RuntimeDeletedEvent) o;

        return runtimeKey != null ? runtimeKey.equals(that.runtimeKey) : that.runtimeKey == null;
    }

    @Override
    public int hashCode() {
        return runtimeKey != null ? runtimeKey.hashCode() : 0;
    }
}
