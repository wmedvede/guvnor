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

public class RuntimeListItemHandler {

    private RuntimeListItem item;

    public RuntimeListItemHandler(RuntimeListItem item) {
        this.item = item;
    }

    public boolean hasPipeline() {
        return getPipeline() != null;
    }

    public String getItemLabel() {
        return item.getItemLabel();
    }

    public boolean isRuntime() {
        return item.getRuntime() != null;
    }

    public Runtime getRuntime() {
        return item.getRuntime();
    }

    public PipelineExecutionTrace getPipelineTrace() {
        if (isRuntime()) {
            return getRuntime().getPipelineTrace();
        } else{
            return item.getPipelineTrace();
        }
    }

    public Pipeline getPipeline() {
        if (item.getPipelineTrace() != null) {
            return item.getPipelineTrace().getPipeline();
        } else if (item.getRuntime() != null && item.getRuntime().getPipelineTrace() != null) {
            return item.getRuntime().getPipelineTrace().getPipeline();
        }
        return null;
    }
}
