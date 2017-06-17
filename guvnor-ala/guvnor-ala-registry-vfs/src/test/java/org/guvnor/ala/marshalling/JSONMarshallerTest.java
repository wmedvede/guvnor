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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.JsonNode;
import org.guvnor.ala.build.maven.config.impl.MavenBuildConfigImpl;
import org.guvnor.ala.config.ProjectConfig;
import org.guvnor.ala.docker.config.impl.DockerProviderConfigImpl;
import org.guvnor.ala.docker.model.DockerProviderImpl;
import org.guvnor.ala.wildfly.config.impl.WildflyProviderConfigImpl;
import org.guvnor.ala.wildfly.model.WildflyProviderImpl;
import org.junit.Before;
import org.junit.Test;

public class JSONMarshallerTest {

    private static final String CONTENT_URI = "/tmp/JSONMarshallerTest.json";

    private Path contentPath;

    private JSONMarshaller marshaller;

    private String serializedContent;

    private ConfigRegistry configRegistry = new ConfigRegistry();

    private MavenBuildConfigImpl mavenBuildConfig;

    private WildflyProviderImpl wildflyProvider;

    private DockerProviderImpl dockerProvider;

    @Before
    public void setUp() throws Exception {
        contentPath = Paths.get(CONTENT_URI);
        marshaller = new JSONMarshaller();

        configRegistry = new ConfigRegistry();

        //deserialization of anonymous classes won't work
        ProjectConfig projectConfig = new ProjectConfig() {
        };

        mavenBuildConfig = new MavenBuildConfigImpl();

        dockerProvider = createDockerProvider("Docker1");
        wildflyProvider = createWildflyProvider("WF1");

       // configRegistry.register(new MyProjectConfig());
      //  configRegistry.register(mavenBuildConfig);

        configRegistry.registerProvider(wildflyProvider);
        //configRegistry.registerProvider(dockerProvider);
    }

    @Test
    public void testMarshall() throws Exception {
        serializedContent = marshaller.marshall(configRegistry);

        Files.write(contentPath,
                    serializedContent.getBytes());

        System.out.println("serializedContent: " + serializedContent);
    }

    @Test
    public void testUnmarshall() throws Exception {

        byte[] content = Files.readAllBytes(contentPath);
        serializedContent = new String(content);
        ConfigRegistry result = marshaller.unmarshall(serializedContent,
                                                      ConfigRegistry.class);

        JsonNode node = marshaller.objectMapper.readTree(serializedContent);
        //node.size()




        System.out.println("unserializedContent: " + result);
    }

    static class MyProjectConfig implements ProjectConfig {

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
