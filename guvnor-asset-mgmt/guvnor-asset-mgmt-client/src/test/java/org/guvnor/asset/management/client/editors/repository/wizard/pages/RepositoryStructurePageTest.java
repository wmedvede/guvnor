/*
 * Copyright 2015 JBoss Inc
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

package org.guvnor.asset.management.client.editors.repository.wizard.pages;

import java.util.List;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.guvnor.asset.management.model.RepositoryStructureModel;
import org.guvnor.asset.management.service.RepositoryStructureService;
import org.guvnor.common.services.project.model.GAV;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.structure.repositories.Repository;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.uberfire.backend.vfs.Path;

@RunWith( GwtMockitoTestRunner.class )
public class RepositoryStructurePageTest {


    @Test
    void testPageLoad() {
        //RepositoryStructurePage structurePage = new RepositoryStructurePage(  ) ;
    }

    private class RepositoryStructureStructureServiceCallerMock implements Caller<RepositoryStructureService> {

        RemoteCallback remoteCallback;

        @Override public RepositoryStructureService call() {
            return null;
        }

        @Override public RepositoryStructureService call( RemoteCallback<?> remoteCallback ) {
            return null;
        }

        @Override public RepositoryStructureService call( RemoteCallback<?> remoteCallback, ErrorCallback<?> errorCallback ) {
            return null;
        }

        private class RepositoryStructureServiceWrapper implements RepositoryStructureService {

            RepositoryStructureService repositoryStructureService;

            @Override public Path initRepositoryStructure( GAV gav, Repository repo ) {
                return null;
            }

            @Override public Path initRepositoryStructure( POM pom, String baseUrl, Repository repo, boolean multiProject ) {
                return null;
            }

            @Override public Repository initRepository( Repository repo, boolean managed ) {
                return null;
            }

            @Override public Path convertToMultiProjectStructure( List<Project> projects, GAV parentGav, Repository repo, boolean updateChildrenGav, String comment ) {
                return null;
            }

            @Override public RepositoryStructureModel load( Repository repository ) {
                return null;
            }

            @Override public RepositoryStructureModel load( Repository repository, boolean includeModules ) {
                return null;
            }

            @Override public void save( Path pathToPomXML, RepositoryStructureModel model, String comment ) {

            }

            @Override public boolean isValidProjectName( String name ) {
                boolean result = repositoryStructureService.isValidProjectName( name );
                remoteCallback.callback( result );
                return result;
            }

            @Override public boolean isValidGroupId( String groupId ) {
                boolean result = repositoryStructureService.isValidGroupId( groupId );
                remoteCallback.callback( result );
                return result;
            }

            @Override public boolean isValidArtifactId( String artifactId ) {
                boolean result = repositoryStructureService.isValidArtifactId( artifactId );
                remoteCallback.callback( result );
                return result;
            }

            @Override public boolean isValidVersion( String version ) {
                boolean result = repositoryStructureService.isValidVersion( version );
                remoteCallback.callback( result );
                return result;
            }

            @Override public void delete( Path pathToPomXML, String comment ) {

            }
        }
    }

}