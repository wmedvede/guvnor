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

package org.guvnor.ala.registry.vfs;

import org.guvnor.ala.marshalling.BaseMarshallerTest;
import org.guvnor.ala.marshalling.Marshaller;

public class VFSRegistryEntryMarshallerTest
        extends BaseMarshallerTest<VFSRegistryEntry> {

    private static final String ENTRY_CONTENT = "ENTRY_CONTENT";

    @Override
    public Marshaller<VFSRegistryEntry> createMarshaller() {
        return new VFSRegistryEntryMarshaller();
    }

    @Override
    public Class<VFSRegistryEntry> getType() {
        return VFSRegistryEntry.class;
    }

    @Override
    public VFSRegistryEntry getValue() {
        return new VFSRegistryEntry(VFSRegistryEntry.JSON,
                                    VFSRuntimeRegistry.class.getName(),
                                    ENTRY_CONTENT);
    }

    @Override
    public String getMarshallerOutput() {
        return "{\"contentFormat\":\"json\"," +
                "\"contentType\":\"org.guvnor.ala.registry.vfs.VFSRuntimeRegistry\"," +
                "\"content\":\"ENTRY_CONTENT\"}";
    }
}