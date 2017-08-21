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

package org.guvnor.ala.ui.openshift.model;

public abstract class ConfigRequest<T> {

    protected SourceType sourceType;
    protected T source;
    protected ConfigType configType;

    protected ConfigRequest(final SourceType sourceType,
                            final T source,
                            final ConfigType configType) {
        this.sourceType = sourceType;
        this.source = source;
        this.configType = configType;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public T getSource() {
        return source;
    }

    public void setSource(T source) {
        this.source = source;
    }

    public ConfigType getConfigType() {
        return configType;
    }

    public void setConfigType(ConfigType configType) {
        this.configType = configType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConfigRequest<?> that = (ConfigRequest<?>) o;

        if (sourceType != that.sourceType) {
            return false;
        }
        if (source != null ? !source.equals(that.source) : that.source != null) {
            return false;
        }
        return configType == that.configType;
    }

    @Override
    public int hashCode() {
        int result = sourceType != null ? sourceType.hashCode() : 0;
        result = ~~result;
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = ~~result;
        result = 31 * result + (configType != null ? configType.hashCode() : 0);
        result = ~~result;
        return result;
    }
}