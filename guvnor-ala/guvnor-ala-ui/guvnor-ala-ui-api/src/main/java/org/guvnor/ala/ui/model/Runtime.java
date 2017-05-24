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
public class Runtime
        extends AbstractHasKeyObject<RuntimeKey> {

    private Pipeline pipeline;
    private Source source;
    private RuntimeStatus status;
    private String endpoint;
    private String createdDate;

    public Runtime(@MapsTo("key") final RuntimeKey key,
                   @MapsTo("pipeline") final Pipeline pipeline,
                   @MapsTo("source") final Source source,
                   @MapsTo("status") final RuntimeStatus status,
                   @MapsTo("endpoint") final String endpoint,
                   @MapsTo("createdDate") final String createdDate) {
        super(key);
        this.pipeline = pipeline;
        this.source = source;
        this.status = status;
        this.endpoint = endpoint;
        this.createdDate = createdDate;
    }

    public Runtime(RuntimeKey key) {
        super(key);
    }

    public Runtime(final RuntimeKey key,
                   final RuntimeStatus status,
                   final String endpoint,
                   String createdDate) {
        super(key);
        this.status = status;
        this.endpoint = endpoint;
        this.createdDate = createdDate;
    }

    public void setPipeline(final Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public RuntimeStatus getStatus() {
        return status;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public String createDate() {
        return createdDate;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }

    public void setStatus(final RuntimeStatus status) {
        this.status = status;
    }

    public Source getSource() {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        Runtime runtime = (Runtime) o;

        if (pipeline != null ? !pipeline.equals(runtime.pipeline) : runtime.pipeline != null) {
            return false;
        }
        if (source != null ? !source.equals(runtime.source) : runtime.source != null) {
            return false;
        }
        if (status != runtime.status) {
            return false;
        }
        if (endpoint != null ? !endpoint.equals(runtime.endpoint) : runtime.endpoint != null) {
            return false;
        }
        return createdDate != null ? createdDate.equals(runtime.createdDate) : runtime.createdDate == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = ~~result;
        result = 31 * result + (pipeline != null ? pipeline.hashCode() : 0);
        result = ~~result;
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = ~~result;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = ~~result;
        result = 31 * result + (endpoint != null ? endpoint.hashCode() : 0);
        result = ~~result;
        result = 31 * result + (createdDate != null ? createdDate.hashCode() : 0);
        result = ~~result;
        return result;
    }
}