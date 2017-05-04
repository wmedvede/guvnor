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

import org.guvnor.ala.ui.model.Runtime;
import org.guvnor.ala.ui.model.StageStatus;
import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class StageStatusChange {

    private Runtime runtime;

    private String stage;

    private StageStatus status;

    public StageStatusChange() {
    }

    public StageStatusChange(@MapsTo("runtime") final Runtime runtime,
                             @MapsTo("state") final String stage,
                             @MapsTo("status") StageStatus status) {
        this.runtime = runtime;
        this.stage = stage;
        this.status = status;
    }

    public Runtime getRuntime() {
        return runtime;
    }

    public String getStage() {
        return stage;
    }

    public StageStatus getStatus() {
        return status;
    }
}
