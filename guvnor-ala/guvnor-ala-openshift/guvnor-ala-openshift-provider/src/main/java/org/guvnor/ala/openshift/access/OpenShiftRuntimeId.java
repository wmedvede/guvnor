/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.guvnor.ala.openshift.access;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

@JsonIgnoreType
public final class OpenShiftRuntimeId {

    private static final String NAMESPACE = "ns";
    private static final String SERVICE = "svc";
    private static final String APPLICATION = "app";
 
    private final String namespace;
    private final String service;
    private final String application;

    public OpenShiftRuntimeId(String namespace, String service, String application) {
        this.namespace = namespace;
        this.service = service;
        this.application = application;
    }

    public String namespace() {
        return namespace;
    }

    public String service() {
        return service;
    }

    public String application() {
        return application;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OpenShiftRuntimeId other = (OpenShiftRuntimeId) obj;
        if (namespace == null) {
            if (other.namespace != null)
                return false;
        } else if (!namespace.equals(other.namespace))
            return false;
        if (service == null) {
            if (other.service != null)
                return false;
        } else if (!service.equals(other.service))
            return false;
        if (application == null) {
            if (other.application != null)
                return false;
        } else if (!application.equals(other.application))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
        result = prime * result + ((service == null) ? 0 : service.hashCode());
        return result;
    }

    public String toString() {
        StringWriter sw = new StringWriter();
        try {
            JsonWriter jw = new JsonWriter(sw);
            jw.beginObject();
            if (namespace != null) {
                jw.name(NAMESPACE).value(namespace);
            }
            if (service != null) {
                jw.name(SERVICE).value(service);
            }
            if (application != null) {
                jw.name(APPLICATION).value(application);
            }
            jw.endObject();
            jw.flush();
            jw.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        return sw.toString();
    }

    public static OpenShiftRuntimeId fromString(String s) {
        String namespace = null;
        String service = null;
        String application = null;
        try {
            JsonReader jr = new JsonReader(new StringReader(s));
            jr.beginObject();
            while (jr.hasNext()) {
                String n = jr.nextName();
                if (NAMESPACE.equals(n)) {
                    namespace = jr.nextString();
                } else if (SERVICE.equals(n)) {
                    service = jr.nextString();
                } else if (APPLICATION.equals(n)) {
                    application = jr.nextString();
                } else {
                    jr.skipValue();
                }
            }
            jr.endObject();
            jr.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        return new OpenShiftRuntimeId(namespace, service, application);
    }

}