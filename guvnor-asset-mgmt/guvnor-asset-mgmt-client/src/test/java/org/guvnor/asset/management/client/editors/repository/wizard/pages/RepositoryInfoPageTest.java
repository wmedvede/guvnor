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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.guvnor.asset.management.client.editors.repository.wizard.CreateRepositoryWizardModel;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.organizationalunit.impl.OrganizationalUnitImpl;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryAlreadyExistsException;
import org.guvnor.structure.repositories.RepositoryInfo;
import org.guvnor.structure.repositories.RepositoryService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.ext.widgets.core.client.wizards.WizardPage;
import org.uberfire.java.nio.base.version.VersionRecord;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class )
public class RepositoryInfoPageTest {

    @GwtMock
    RepositoryInfoPageView view;

    OrganizationalUnitService organizationalUnitService = mock( OrganizationalUnitService.class );

    RepositoryService repositoryService = mock( RepositoryService.class );

    List<OrganizationalUnit> organizationalUnits = buildOrganiztionalUnits();

    /**
     * Tests that organizational units information is properly loaded when the page is initialized.
     */
    @Test
    public void testPageLoad() {

        RepositoryInfoPage infoPage = new RepositoryInfoPageExtended( view,
                new OrganizationalUnitServiceCallerMock( organizationalUnitService ),
                new RepositoryServiceCallerMock( repositoryService ),
                true );

        CreateRepositoryWizardModel model = new CreateRepositoryWizardModel();
        infoPage.setModel( model );

        when( organizationalUnitService.getOrganizationalUnits() ).thenReturn( organizationalUnits );

        infoPage.prepareView();

        verify( view, times( 1 ) ).init( infoPage );
        verify( view ).initOrganizationalUnits( eq( organizationalUnits ) );

        assertPageComplete( false, infoPage );
    }

    /**
     * Tests that the page reacts properly when the selected organizational unit changes.
     */
    public void testOrganizationalUnitChange() {

        RepositoryInfoPage infoPage = new RepositoryInfoPageExtended( view,
                new OrganizationalUnitServiceCallerMock( organizationalUnitService ),
                new RepositoryServiceCallerMock( repositoryService ),
                true );

        CreateRepositoryWizardModel model = new CreateRepositoryWizardModel();
        infoPage.setModel( model );

        when( organizationalUnitService.getOrganizationalUnits() ).thenReturn( organizationalUnits );
        when( view.getOrganizationalUnitName() ).thenReturn( "OrganizationalUnit1" );

        infoPage.prepareView();
        infoPage.onOUChange();

        verify( view, times( 1 ) ).getOrganizationalUnitName();

        assertEquals( organizationalUnits.get( 0 ), model.getOrganizationalUnit() );

        assertPageComplete( false, infoPage );
    }

    /**
     * Tests that the page reacts properly when a valid repository name is entered.
     */
    @Test
    public void testValidRepositoryNameChange( ) {

        RepositoryInfoPage infoPage = new RepositoryInfoPageExtended( view,
                new OrganizationalUnitServiceCallerMock( organizationalUnitService ),
                new RepositoryServiceCallerMock( repositoryService ),
                true );

        CreateRepositoryWizardModel model = new CreateRepositoryWizardModel();
        infoPage.setModel( model );

        when( repositoryService.validateRepositoryName( "ValidRepo" ) ).thenReturn( true );
        when( repositoryService.validateRepositoryName( "InvalidRepo" ) ).thenReturn( false );


        when( view.getName() ).thenReturn( "ValidRepo" );
        infoPage.onNameChange();

        verify( view, times( 2 ) ).getName();
        verify( view, times( 1 ) ).clearNameErrorMessage();


        assertEquals( "ValidRepo", model.getRepositoryName() );

        assertPageComplete( false, infoPage );
    }

    /**
     * Tests that the page reacts properly when an invalid repository name is typed.
     */
    @Test
    public void testInvalidRepositoryNameChange( ) {

        RepositoryInfoPage infoPage = new RepositoryInfoPageExtended( view,
                new OrganizationalUnitServiceCallerMock( organizationalUnitService ),
                new RepositoryServiceCallerMock( repositoryService ),
                true );

        CreateRepositoryWizardModel model = new CreateRepositoryWizardModel();
        infoPage.setModel( model );

        when( repositoryService.validateRepositoryName( "ValidRepo" ) ).thenReturn( true );
        when( repositoryService.validateRepositoryName( "InvalidRepo" ) ).thenReturn( false );


        when( view.getName() ).thenReturn( "InvalidRepo" );
        infoPage.onNameChange();

        verify( view, times( 2 ) ).getName();
        verify( view, times( 1 ) ).setNameErrorMessage( anyString() );

        assertEquals( "InvalidRepo", model.getRepositoryName() );

        assertPageComplete( false, infoPage );
    }

    /**
     * Tests that the page reacts properly when the managed repository option is checked.
     */
    @Test
    public void testManagedRepositorySelected() {
        testManagedRepositoryChange( true );
    }

    /**
     * Tests that the page reacts properly when the managed repository option is un checked.
     */
    @Test
    public void testUnManagedRepositorySelected() {
        testManagedRepositoryChange( false );
    }

    public void testManagedRepositoryChange( boolean isManaged ) {

        RepositoryInfoPage infoPage = new RepositoryInfoPageExtended( view,
                new OrganizationalUnitServiceCallerMock( organizationalUnitService ),
                new RepositoryServiceCallerMock( repositoryService ),
                true );

        CreateRepositoryWizardModel model = new CreateRepositoryWizardModel();
        infoPage.setModel( model );

        when( view.isManagedRepository() ).thenReturn( isManaged );
        infoPage.onManagedRepositoryChange();

        verify( view, times( isManaged ? 4 : 3 ) ).isManagedRepository();

        assertEquals( isManaged, model.isManged() );

        assertPageComplete( false, infoPage );
    }

    /**
     * Test a sequence of steps that will successfully complete the page.
     */
    @Test
    public void testPageCompleted() {

        RepositoryInfoPage infoPage = new RepositoryInfoPageExtended( view,
                new OrganizationalUnitServiceCallerMock( organizationalUnitService ),
                new RepositoryServiceCallerMock( repositoryService ),
                true );

        CreateRepositoryWizardModel model = new CreateRepositoryWizardModel();
        infoPage.setModel( model );


        when( organizationalUnitService.getOrganizationalUnits() ).thenReturn( organizationalUnits );
        when( repositoryService.validateRepositoryName( "ValidRepo" ) ).thenReturn( true );
        when( view.getOrganizationalUnitName() ).thenReturn( "OrganizationalUnit1" );
        when( view.getName() ).thenReturn( "ValidRepo" );

        infoPage.prepareView();
        infoPage.onNameChange();
        infoPage.onOUChange();

        assertEquals( organizationalUnits.get( 0 ), model.getOrganizationalUnit() );
        assertEquals( "ValidRepo", model.getRepositoryName() );

        assertPageComplete( true, infoPage );
    }

    public List<OrganizationalUnit> buildOrganiztionalUnits() {
        List<OrganizationalUnit> organizationalUnits = new ArrayList<OrganizationalUnit>(  );

        OrganizationalUnit organizationalUnit = new OrganizationalUnitImpl( "OrganizationalUnit1", "user1", "group1");
        organizationalUnits.add( organizationalUnit );

        organizationalUnit = new OrganizationalUnitImpl( "OrganizationalUnit2", "user2", "group2");
        organizationalUnits.add( organizationalUnit );
        return organizationalUnits;
    }

    public void assertPageComplete( final boolean expectedResult, WizardPage page ) {
        page.isComplete( new Callback<Boolean>() {
            @Override public void callback( Boolean result ) {
                assertEquals( expectedResult, result );
            }
        } );
    }

    private class RepositoryInfoPageExtended extends RepositoryInfoPage {

        private boolean ouMandatory = false;

        public RepositoryInfoPageExtended( RepositoryInfoPageView view,
                Caller<OrganizationalUnitService> organizationalUnitService,
                Caller<RepositoryService> repositoryService,
                boolean ouMandatory ) {

            super( view, organizationalUnitService, repositoryService );
            this.ouMandatory = ouMandatory;
        }

        @Override protected boolean isOUMandatory() {
            return ouMandatory;
        }

        @Override public void fireEvent() {

        }
    }

    private class OrganizationalUnitServiceCallerMock implements Caller<OrganizationalUnitService> {

        OrganizationalUnitServiceWrapper organizationalUnitServiceWrapper;

        RemoteCallback remoteCallback;

        public OrganizationalUnitServiceCallerMock( OrganizationalUnitService organizationalUnitService ) {
            this.organizationalUnitServiceWrapper = new OrganizationalUnitServiceWrapper( organizationalUnitService );
        }

        @Override public OrganizationalUnitService call() {
            return organizationalUnitServiceWrapper;
        }

        @Override public OrganizationalUnitService call( RemoteCallback<?> remoteCallback ) {
            return call( remoteCallback, null );
        }

        @Override public OrganizationalUnitService call( RemoteCallback<?> remoteCallback, ErrorCallback<?> errorCallback ) {
            this.remoteCallback = remoteCallback;
            return organizationalUnitServiceWrapper;
        }

        private class OrganizationalUnitServiceWrapper implements OrganizationalUnitService {

            OrganizationalUnitService organizationalUnitService;

            public OrganizationalUnitServiceWrapper( OrganizationalUnitService organizationalUnitService ) {
                this.organizationalUnitService = organizationalUnitService;
            }

            @Override public OrganizationalUnit getOrganizationalUnit( String name ) {
                return organizationalUnitService.getOrganizationalUnit( name );
            }

            @Override public Collection<OrganizationalUnit> getOrganizationalUnits() {
                Collection<OrganizationalUnit> result = organizationalUnitService.getOrganizationalUnits();
                remoteCallback.callback( result );
                return result;
            }

            @Override public OrganizationalUnit createOrganizationalUnit( String name, String owner, String defaultGroupId ) {
                return organizationalUnitService.createOrganizationalUnit( name, owner, defaultGroupId );
            }

            @Override public OrganizationalUnit createOrganizationalUnit( String name, String owner, String defaultGroupId, Collection<Repository> repositories ) {
                return organizationalUnitService.createOrganizationalUnit( name, owner, defaultGroupId, repositories );
            }

            @Override public OrganizationalUnit updateOrganizationalUnit( String name, String owner, String defaultGroupId ) {
                return organizationalUnitService.updateOrganizationalUnit( name, owner, defaultGroupId );
            }

            @Override public void addRepository( OrganizationalUnit organizationalUnit, Repository repository ) {
                organizationalUnitService.addRepository( organizationalUnit, repository );
            }

            @Override public void removeRepository( OrganizationalUnit organizationalUnit, Repository repository ) {
                organizationalUnitService.removeRepository( organizationalUnit, repository );
            }

            @Override public void addGroup( OrganizationalUnit organizationalUnit, String group ) {
                organizationalUnitService.addGroup( organizationalUnit, group );
            }

            @Override public void removeGroup( OrganizationalUnit organizationalUnit, String group ) {
                organizationalUnitService.removeGroup( organizationalUnit, group );
            }

            @Override public void removeOrganizationalUnit( String name ) {
                organizationalUnitService.removeOrganizationalUnit( name );
            }

            @Override public OrganizationalUnit getParentOrganizationalUnit( Repository repository ) {
                return organizationalUnitService.getParentOrganizationalUnit( repository );
            }

            @Override public String getSanitizedDefaultGroupId( String proposedGroupId ) {
                return organizationalUnitService.getSanitizedDefaultGroupId( proposedGroupId );
            }

            @Override public Boolean isValidGroupId( String proposedGroupId ) {
                return organizationalUnitService.isValidGroupId( proposedGroupId );
            }
        };

    }

    private class RepositoryServiceCallerMock implements Caller<RepositoryService> {

        RepositoryServiceWrapper repositoryServiceWrapper;

        RemoteCallback remoteCallback;

        public RepositoryServiceCallerMock( RepositoryService repositoryService ) {
            repositoryServiceWrapper = new RepositoryServiceWrapper( repositoryService );
        }

        @Override public RepositoryService call() {
            return repositoryServiceWrapper;
        }

        @Override public RepositoryService call( RemoteCallback<?> remoteCallback ) {
            return call( remoteCallback, null );
        }

        @Override public RepositoryService call( RemoteCallback<?> remoteCallback, ErrorCallback<?> errorCallback ) {
            this.remoteCallback = remoteCallback;
            return repositoryServiceWrapper;
        }

        private class RepositoryServiceWrapper implements RepositoryService {

            RepositoryService repositoryService;

            public RepositoryServiceWrapper( RepositoryService repositoryService ) {
                this.repositoryService = repositoryService;
            }

            @Override public RepositoryInfo getRepositoryInfo( String alias ) {
                return null;
            }

            @Override public List<VersionRecord> getRepositoryHistory( String alias, int startIndex ) {
                return null;
            }

            @Override public List<VersionRecord> getRepositoryHistory( String alias, int startIndex, int endIndex ) {
                return null;
            }

            @Override public List<VersionRecord> getRepositoryHistoryAll( String alias ) {
                return null;
            }

            @Override public Repository getRepository( String alias ) {
                return null;
            }

            @Override public Repository getRepository( Path root ) {
                return null;
            }

            @Override public Collection<Repository> getRepositories() {
                return null;
            }

            @Override public Repository createRepository( OrganizationalUnit organizationalUnit, String scheme, String alias, Map<String, Object> env ) throws RepositoryAlreadyExistsException {
                return null;
            }

            @Override public Repository createRepository( String scheme, String alias, Map<String, Object> env ) throws RepositoryAlreadyExistsException {
                return null;
            }

            @Override public String normalizeRepositoryName( String name ) {
                return null;
            }

            @Override public boolean validateRepositoryName( String name ) {
                boolean result = repositoryService.validateRepositoryName( name );
                remoteCallback.callback( result );
                return result;
            }

            @Override public void addGroup( Repository repository, String group ) {

            }

            @Override public void removeGroup( Repository repository, String group ) {

            }

            @Override public void removeRepository( String alias ) {

            }

            @Override public Repository updateRepository( Repository repository, Map<String, Object> config ) {
                return null;
            }
        }
    }

}
