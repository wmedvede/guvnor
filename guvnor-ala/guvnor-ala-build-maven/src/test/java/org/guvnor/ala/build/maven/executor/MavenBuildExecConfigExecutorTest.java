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

package org.guvnor.ala.build.maven.executor;

import java.util.Optional;

import org.guvnor.ala.build.maven.config.MavenBuildExecConfig;
import org.guvnor.ala.config.BinaryConfig;
import org.guvnor.ala.registry.BuildRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith( MockitoJUnitRunner.class )
public class MavenBuildExecConfigExecutorTest
        extends MavenBuildExecConfigExecutorTestBase {

    @Mock
    protected BuildRegistry buildRegistry;

    @Mock
    private MavenBuildExecConfig mavenBuildExecConfig;

    protected MavenBuildExecConfigExecutor executor;

    @Override
    protected String getTestProject( ) {
        return "MavenBuildExecutorTest";
    }

    @Override
    protected String getTestProjectJar( ) {
        return "maven-build-executor-test-1.0.0.jar";
    }

    @Before
    public void setUp( ) throws Exception {
        setUpTestProject( );
        executor = new MavenBuildExecConfigExecutor( buildRegistry );
    }

    @Test
    public void testApply( ) throws Exception {
        prepareMavenBuild( );
        Optional< BinaryConfig > result = executor.apply( mavenBuild, mavenBuildExecConfig );
        verifyBinary( result );
        clearTempDir( );
    }
}