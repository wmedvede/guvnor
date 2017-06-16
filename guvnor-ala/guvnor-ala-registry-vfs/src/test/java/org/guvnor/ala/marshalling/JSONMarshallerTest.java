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
import java.util.List;

import org.guvnor.ala.build.Project;
import org.guvnor.ala.build.maven.config.impl.MavenBuildConfigImpl;
import org.guvnor.ala.config.ProjectConfig;
import org.guvnor.ala.pipeline.Pipeline;
import org.guvnor.ala.pipeline.PipelineConfig;
import org.guvnor.ala.pipeline.Stage;
import org.junit.Before;
import org.junit.Test;

public class JSONMarshallerTest {

    private static final String CONTENT_URI = "/tmp/JSONMarshallerTest.json";

    private Path contentPath;

    private JSONMarshaller marshaller;

    private String serializedContent;

    private ConfigRegistry configRegistry = new ConfigRegistry();

    private MavenBuildConfigImpl mavenBuildConfig;

    @Before
    public void setUp() throws Exception {
        contentPath = Paths.get(CONTENT_URI);
        marshaller = new JSONMarshaller();

        configRegistry = new ConfigRegistry();

        //deserialization of anonymous classes won't work
        ProjectConfig projectConfig = new ProjectConfig() {
        };

        mavenBuildConfig = new MavenBuildConfigImpl();

        configRegistry.register(new MyProjectConfig());
        configRegistry.register(mavenBuildConfig);
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
        System.out.println("unserializedContent: " + result);
    }

    static class MyProjectConfig implements ProjectConfig {

    }

    public void nada() {
        Pipeline<?> pipeline = new Pipeline<Stage>() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public List<Stage> getStages() {
                return null;
            }

            @Override
            public PipelineConfig getConfig() {
                return null;
            }
        };

        pipeline.getStages().forEach(stage -> {
                                         System.out.println(stage.getName());
                                     }
        );
    }
}
