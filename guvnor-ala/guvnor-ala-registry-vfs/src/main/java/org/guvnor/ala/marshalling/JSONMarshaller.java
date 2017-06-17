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

package org.guvnor.ala.marshalling;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.SimpleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic json marshaller.
 */
public class JSONMarshaller {

    private static final Logger logger = LoggerFactory.getLogger(JSONMarshaller.class);

    protected ObjectMapper objectMapper;

    public JSONMarshaller() {
        this.objectMapper = new ObjectMapper();
    }

    public String marshall(final Object objectInput) throws Exception {
        try {
            return objectMapper.writeValueAsString(objectInput);
        } catch (IOException e) {
            logger.error("An error was produced during object marshalling, objectInput: " + objectInput,
                         e);
            throw e;
        }
    }

    public <T> T unmarshall(String marshalledInput,
                            Class<T> type) throws Exception {
        try {
            return objectMapper.readValue(marshalledInput,
                                          type);
        } catch (IOException e) {
            logger.error("An error was produced during object unmarshalling, marshalledInput: " + marshalledInput,
                         e);
            throw e;
        }
    }

    /**
     * @param marshalledInput marshalled content to unmarshall.
     * @param clazz Expected result type. The expected result type can not be a Map, Collection, or an array.
     * An exception si thrown if any of the following conditions are met:
     * Map.class.isAssignableFrom(clazz)
     * Collection.class.isAssignableFrom(clazz)
     * clazz.isArray()
     * @return the unmarshalled object.
     */
    public Object unmarshallSimpleType(String marshalledInput,
                                       Class<?> clazz) throws Exception {
        try {
            return objectMapper.readValue(marshalledInput,
                                          SimpleType.construct(clazz));
        } catch (IOException e) {
            logger.error("An error was produced during object unmarshalling, marshalledInput: " + marshalledInput,
                         e);
            throw e;
        }
    }
}
