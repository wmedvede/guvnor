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

import java.util.ArrayList;
import java.util.List;

import org.guvnor.ala.docker.config.impl.DockerProviderConfigImpl;
import org.guvnor.ala.docker.model.DockerProviderImpl;
import org.guvnor.ala.runtime.providers.Provider;
import org.guvnor.ala.wildfly.config.impl.WildflyProviderConfigImpl;
import org.guvnor.ala.wildfly.model.WildflyProviderImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.uberfire.mocks.FileSystemTestingUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VFSRuntimeRegistryTest {

    private FileSystemTestingUtils fileSystemTestingUtils = new FileSystemTestingUtils();

    private VFSRuntimeRegistry registry;

    @Before
    public void setUp() throws Exception {
        fileSystemTestingUtils.setup();
        registry = new VFSRuntimeRegistry(fileSystemTestingUtils.getIoService(),
                                          fileSystemTestingUtils.getFileSystem());
        registry.init();
    }

    @Test
    public void testRegisterProvider() {

        Provider wfProvider = createWildflyProvider("wf");
        Provider dockerProvider = createDockerProvider("docker");

        List<Provider> providers = new ArrayList<>();
        providers.add(wfProvider);
        providers.add(dockerProvider);


        registry.registerProvider(wfProvider);
        registry.registerProvider(dockerProvider);



        VFSRuntimeRegistry loadedRegistry = new VFSRuntimeRegistry(fileSystemTestingUtils.getIoService(),
                                                                   fileSystemTestingUtils.getFileSystem());
        loadedRegistry.init();

        //he just read registry must have the same values as the orignal one.

        int i = 0;

        for( Provider provider : providers ) {
            assertNotNull(loadedRegistry.getProvider(provider.getId()));
            assertEquals(provider, loadedRegistry.getProvider(provider.getId()));
        }

    }

    //@After
    public void cleanUp() {
        fileSystemTestingUtils.cleanup();
    }

    private WildflyProviderImpl createWildflyProvider(String suffix) {
        return new WildflyProviderImpl(
                new WildflyProviderConfigImpl("name." + suffix,
                                              "host." + suffix,
                                              "port." + suffix,
                                              "managementPort." + suffix,
                                              "user." + suffix,
                                              "password." + suffix));
    }

    private DockerProviderImpl createDockerProvider(String suffix) {
        return new DockerProviderImpl(new DockerProviderConfigImpl("name." + suffix,
                                                                   "host." + suffix));
    }

}
