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

import org.guvnor.ala.marshalling.impl.JSONBaseMarshaller;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JSONBaseMarshallerTest {

    private static final String FIELD1 = "FIELD1";

    private static final String FIELD2 = "FIELD2";

    private static final String MARSHALLED_VALUE = "{\"field1\":\"FIELD1\",\"field2\":\"FIELD2\"}";

    private JSONBaseMarshaller marshaller;

    @Before
    public void setUp() {
        marshaller = new JSONMarshallerMock();
    }

    @Test
    public void testGetClass() {
        assertEquals(MockClass.class,
                     marshaller.getType());
    }

    @Test
    public void testMarshall() throws Exception {
        MockClass value = new MockClass(FIELD1,
                                        FIELD2);
        String marshalledValue = marshaller.marshal(value);
        assertEquals(MARSHALLED_VALUE,
                     marshalledValue);
    }

    @Test
    public void testUnMarshall() throws Exception {
        MockClass value = new MockClass(FIELD1,
                                        FIELD2);
        MockClass result = (MockClass) marshaller.unmarshal(MARSHALLED_VALUE);
        assertEquals(value,
                     result);
    }

    private static class JSONMarshallerMock
            extends JSONBaseMarshaller<MockClass> {

        public JSONMarshallerMock() {
            super(MockClass.class);
        }
    }

    private static class MockClass {

        String field1;
        String field2;

        public MockClass() {
            //no args constructor required for marshalling/unmarshalling.
        }

        public MockClass(String field1,
                         String field2) {
            this.field1 = field1;
            this.field2 = field2;
        }

        public String getField1() {
            return field1;
        }

        public String getField2() {
            return field2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            MockClass mockClass = (MockClass) o;

            if (field1 != null ? !field1.equals(mockClass.field1) : mockClass.field1 != null) {
                return false;
            }
            return field2 != null ? field2.equals(mockClass.field2) : mockClass.field2 == null;
        }

        @Override
        public int hashCode() {
            int result = field1 != null ? field1.hashCode() : 0;
            result = 31 * result + (field2 != null ? field2.hashCode() : 0);
            return result;
        }
    }
}
