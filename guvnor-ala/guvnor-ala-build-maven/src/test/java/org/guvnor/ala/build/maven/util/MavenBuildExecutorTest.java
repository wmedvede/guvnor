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

package org.guvnor.ala.build.maven.util;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.MavenExecutionResult;
import org.junit.Before;
import org.junit.Test;
import org.kie.scanner.embedder.logger.LocalLoggerConsumer;

import static org.junit.Assert.*;

public class MavenBuildExecutorTest {

    private Path pomFile;

    private Path tempDir;

    private Path expectedJar;

    @Before
    public void setUp( ) throws Exception {
        final URL pomUrl = this.getClass( ).getResource( "/MavenBuildExecutorTest/pom.xml" );
        final Path testProjectPath = Paths.get( pomUrl.toURI( ) ).getParent( );
        tempDir = Files.createTempDirectory( "MavenBuildExecutorTest" );
        Path targetProjectPath = tempDir.resolve( "MavenBuildExecutorTest" );
        FileUtils.copyDirectory( testProjectPath.toFile( ), targetProjectPath.toFile( ) );
        pomFile = targetProjectPath.resolve( "pom.xml" );
        expectedJar = targetProjectPath.resolve( "target" ).resolve( "maven-build-executor-test-1.0.0.jar" );
    }

    @Test
    public void testMavenBuild( ) throws Exception {
        List< String > messages = new ArrayList<>( );
        LocalLoggerConsumer localLoggerConsumer = new LocalLoggerConsumer( ) {
            @Override
            public void debug( String message, Throwable throwable ) {
                messages.add( message );
            }

            @Override
            public void info( String message, Throwable throwable ) {
                messages.add( message );
            }

            @Override
            public void warn( String message, Throwable throwable ) {
                messages.add( message );
            }

            @Override
            public void error( String message, Throwable throwable ) {
                messages.add( message );
            }

            @Override
            public void fatalError( String message, Throwable throwable ) {
                messages.add( message );
            }
        };
        MavenExecutionResult result = MavenBuildExecutor.executeMaven( pomFile.toFile( ), new Properties( ), localLoggerConsumer, "clean", "package" );
        assertFalse( result.hasExceptions( ) );
        assertTrue( Files.exists( expectedJar ) );
        assertTrue( messages.contains( "Building jar: " + expectedJar.toString( ) ) );

        FileUtils.deleteDirectory( tempDir.toFile( ) );
    }
}