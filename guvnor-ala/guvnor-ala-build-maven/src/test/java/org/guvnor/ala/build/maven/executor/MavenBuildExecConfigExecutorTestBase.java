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

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.guvnor.ala.build.Project;
import org.guvnor.ala.build.maven.model.MavenBuild;
import org.guvnor.ala.build.maven.model.impl.MavenProjectBinaryBuildImpl;
import org.guvnor.ala.config.BinaryConfig;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public abstract class MavenBuildExecConfigExecutorTestBase {

    public static final String BUILD_SUCCESS = "BUILD SUCCESS";

    protected Path tempDir;

    protected Path targetProjectPath;

    @Mock
    protected Project project;

    @Mock
    protected MavenBuild mavenBuild;

    protected abstract String getTestProject( );

    protected abstract String getTestProjectJar( );

    public URL getPomURL( ) {
        return this.getClass( ).getResource( "/" + getTestProject( ) + "/pom.xml" );
    }

    public void setUpTestProject( ) throws Exception {
        final URL pomUrl = getPomURL( );
        final Path testProjectPath = Paths.get( pomUrl.toURI( ) ).getParent( );
        tempDir = Files.createTempDirectory( getClass( ).getSimpleName( ) );
        targetProjectPath = tempDir.resolve( getTestProject( ) );
        FileUtils.copyDirectory( testProjectPath.toFile( ), targetProjectPath.toFile( ) );
    }

    protected void prepareMavenBuild( ) {
        ArrayList< String > goals = new ArrayList<>( );
        goals.add( "clean" );
        goals.add( "package" );

        when( mavenBuild.getGoals( ) ).thenReturn( goals );
        when( mavenBuild.getProperties( ) ).thenReturn( new Properties( ) );
        when( mavenBuild.getProject( ) ).thenReturn( project );

        when( project.getTempDir( ) ).thenReturn( targetProjectPath.toString( ) );
        when( project.getExpectedBinary( ) ).thenReturn( getTestProjectJar( ) );
    }

    protected void verifyBinary( Optional< BinaryConfig > optional ) {
        assertTrue( optional.get( ) instanceof MavenProjectBinaryBuildImpl );
        MavenProjectBinaryBuildImpl mavenProjectBinaryBuild = ( MavenProjectBinaryBuildImpl ) optional.get( );
        assertFalse( mavenProjectBinaryBuild.getMavenBuildResult( ).hasExceptions( ) );
        assertTrue( Files.exists( targetProjectPath.resolve( "target/" + getTestProjectJar( ) ) ) );
        assertEquals( 1, mavenProjectBinaryBuild.getMavenBuildResult( )
                .getBuildMessages( )
                .stream( )
                .filter( buildMessage -> BUILD_SUCCESS.equals( buildMessage.getMessage( ) ) )
                .count( ) );
    }

    protected void clearTempDir( ) throws Exception {
        FileUtils.deleteDirectory( tempDir.toFile( ) );
    }
}