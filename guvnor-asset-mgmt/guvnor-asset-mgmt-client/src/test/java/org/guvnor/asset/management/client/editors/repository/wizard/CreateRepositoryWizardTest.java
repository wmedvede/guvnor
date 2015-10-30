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

package org.guvnor.asset.management.client.editors.repository.wizard;

import java.util.List;

import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.guvnor.asset.management.backend.service.AssetManagementServiceCallerMock;
import org.guvnor.asset.management.backend.service.RepositoryStructureServiceCallerMock;
import org.guvnor.asset.management.client.editors.repository.wizard.pages.RepositoryInfoPageTest;
import org.guvnor.asset.management.client.editors.repository.wizard.pages.RepositoryInfoPageView;
import org.guvnor.asset.management.client.editors.repository.wizard.pages.RepositoryStructurePageTest;
import org.guvnor.asset.management.client.editors.repository.wizard.pages.RepositoryStructurePageView;
import org.guvnor.asset.management.service.AssetManagementService;
import org.guvnor.asset.management.service.RepositoryStructureService;
import org.guvnor.common.services.shared.security.KieWorkbenchACL;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.organizationalunit.OrganizationalUnitServiceCallerMock;
import org.guvnor.structure.repositories.RepositoryService;
import org.guvnor.structure.repositories.RepositoryServiceCallerMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.rpc.SessionInfo;

import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class )
public class CreateRepositoryWizardTest {

    @GwtMock
    RepositoryInfoPageView infoPageView;

    @GwtMock
    RepositoryStructurePageView structurePageView;

    OrganizationalUnitService organizationalUnitService = mock( OrganizationalUnitService.class );

    RepositoryService repositoryService = mock( RepositoryService.class );

    RepositoryStructureService repositoryStructureService = mock( RepositoryStructureService.class );

    AssetManagementService assetManagementService = mock( AssetManagementService.class );

    List<OrganizationalUnit> organizationalUnits = RepositoryInfoPageTest.buildOrganiztionalUnits();

    SessionInfo sessionInfo = mock( SessionInfo.class );

    KieWorkbenchACL kieACL = mock( KieWorkbenchACL.class );

    @Test
    public void testWizardCompletedTest() {


        RepositoryInfoPageTest.RepositoryInfoPageExtended infoPage = new RepositoryInfoPageTest.RepositoryInfoPageExtended( infoPageView,
                new OrganizationalUnitServiceCallerMock( organizationalUnitService ),
                new RepositoryServiceCallerMock( repositoryService ),
                true );

        RepositoryStructurePageTest.RepositoryStructurePageExtended structurePage = new RepositoryStructurePageTest.RepositoryStructurePageExtended( structurePageView,
                new RepositoryStructureServiceCallerMock( repositoryStructureService ) );

        CreateRepositoryWizardModel model = new CreateRepositoryWizardModel();

        CreateRepositoryWizard createRepositoryWizard = new CreateRepositoryWizard( infoPage,
                structurePage,
                model,
                new RepositoryServiceCallerMock( repositoryService ),
                new RepositoryStructureServiceCallerMock( repositoryStructureService ),
                new AssetManagementServiceCallerMock( assetManagementService ),
                null,
                kieACL,
                sessionInfo );


        createRepositoryWizard.setupPages();
        createRepositoryWizard.start();

        createRepositoryWizard.isComplete( new Callback<Boolean>() {
            @Override public void callback( Boolean result ) {
                System.out.println( result );
            }
        } );

    }
}
