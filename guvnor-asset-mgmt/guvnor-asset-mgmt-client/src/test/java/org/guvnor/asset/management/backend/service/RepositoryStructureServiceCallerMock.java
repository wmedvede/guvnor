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

package org.guvnor.asset.management.backend.service;

import java.util.List;

import org.guvnor.asset.management.model.RepositoryStructureModel;
import org.guvnor.asset.management.service.RepositoryStructureService;
import org.guvnor.common.services.project.model.GAV;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.structure.repositories.Repository;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.uberfire.backend.vfs.Path;

public class RepositoryStructureServiceCallerMock
        implements Caller<RepositoryStructureService> {

    RepositoryStructureServiceWrapper repositoryStructureServiceWrapper;

    RemoteCallback remoteCallback;

    public RepositoryStructureServiceCallerMock( RepositoryStructureService repositoryStructureService ) {
        this.repositoryStructureServiceWrapper = new RepositoryStructureServiceWrapper( repositoryStructureService );
    }

    @Override
    public RepositoryStructureService call() {
        return repositoryStructureServiceWrapper;
    }

    @Override
    public RepositoryStructureService call( RemoteCallback<?> remoteCallback ) {
        return call( remoteCallback, null );
    }

    @Override
    public RepositoryStructureService call( RemoteCallback<?> remoteCallback, ErrorCallback<?> errorCallback ) {
        this.remoteCallback = remoteCallback;
        return repositoryStructureServiceWrapper;
    }

    private class RepositoryStructureServiceWrapper implements RepositoryStructureService {

        RepositoryStructureService repositoryStructureService;

        public RepositoryStructureServiceWrapper( RepositoryStructureService repositoryStructureService ) {
            this.repositoryStructureService = repositoryStructureService;
        }

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