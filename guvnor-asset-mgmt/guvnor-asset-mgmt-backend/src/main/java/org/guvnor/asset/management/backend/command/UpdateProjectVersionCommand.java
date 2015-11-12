/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.guvnor.asset.management.backend.command;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.TypeLiteral;

import org.guvnor.asset.management.backend.AssetManagementRuntimeException;
import org.guvnor.asset.management.backend.model.ProjectInfo;
import org.guvnor.asset.management.backend.utils.CDIUtils;
import org.guvnor.asset.management.backend.utils.DataUtils;
import org.guvnor.asset.management.backend.utils.NamedLiteral;
import org.guvnor.asset.management.social.RepositoryChangeEvent;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.project.service.POMService;
import org.guvnor.common.services.project.service.ProjectService;
import org.guvnor.m2repo.backend.server.GuvnorM2Repository;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.Path;

public class UpdateProjectVersionCommand extends AbstractCommand {
	
	private static final Logger logger = LoggerFactory.getLogger(UpdateProjectVersionCommand.class);
    private static final String KIE_PROJECT_CLASS = "org.kie.workbench.common.services.shared.project.KieProject";

	@Override
	public ExecutionResults execute(CommandContext ctx) throws Exception {
		try {
            ExecutionResults executionResults = new ExecutionResults();


            String uri = (String) getParameter(ctx, "Uri");
            String branchToUpdate = (String) getParameter(ctx, "BranchToUpdate");
            String version = (String) getParameter(ctx, "Version");

            BeanManager beanManager = CDIUtils.lookUpBeanManager(ctx);
            logger.debug("BeanManager " + beanManager);

            ProjectService projectService = CDIUtils.createBean( new TypeLiteral<ProjectService<?>>() {
            }.getType(), beanManager );
            logger.debug("ProjectService " + projectService);

            IOService ioService = CDIUtils.createBean( IOService.class, beanManager, new NamedLiteral( "ioStrategy" ) );
            logger.debug("IoService " + ioService);

            GuvnorM2Repository m2service = CDIUtils.createBean( GuvnorM2Repository.class, beanManager );
            logger.debug( "GuvnorM2Repository " + m2service );

            List<ProjectInfo> updatedProject = new ArrayList<ProjectInfo>();

            if (projectService != null) {
                POMService pomService = CDIUtils.createBean(POMService.class, beanManager);
                logger.debug("POMService " + pomService);

                RepositoryService repositoryService = CDIUtils.createBean(RepositoryService.class, beanManager);
                logger.debug("RepositoryService " + repositoryService);

                Repository repo = repositoryService.getRepository(uri);

                // update and register parent pom if exists
                String branchRoot = repo.getBranchRoot(branchToUpdate).toURI();
                Path parentPomPath = ioService.get( URI.create( branchRoot + "pom.xml" ) );
                final List<String> modules = new ArrayList<String>(  );

                if (ioService.exists(parentPomPath)) {
                    org.uberfire.backend.vfs.Path convertedPomPath = Paths.convert( parentPomPath );
                    POM pom = pomService.load(convertedPomPath);
                    pom.getGav().setVersion( version );
                    pomService.save( convertedPomPath, pom, null, "Update parent pom version during release" );

                    //The new parent needs to be deployed before his children starts to be being built.
                    m2service.deployParentPom( pom.getGav() );

                    if ( pom.getModules() != null ) {
                        modules.addAll( pom.getModules() );
                    }
                }

                // update child modules
                Set<Project> projects = projectService.getProjects(repo, branchToUpdate);
                for (Project project : projects) {

                    POM pom = pomService.load(project.getPomXMLPath());
                    pom.getGav().setVersion(version);
                    if ( pom.getParent() != null ) {
                        pom.getParent().setVersion( version );
                    }
                    pomService.save(project.getPomXMLPath(), pom, null, "Update project version during release");
                    executionResults.setData("GAV", pom.getGav().toString());

                    boolean isKieProject = KIE_PROJECT_CLASS.equals(project.getClass().getName());
                    updatedProject.add(new ProjectInfo(repo.getAlias(), branchToUpdate, project.getProjectName(), isKieProject));
                }

                if ( modules.size() > 0 ) {

                    Collections.sort( updatedProject, new Comparator<ProjectInfo>() {
                        @Override
                        public int compare( ProjectInfo projectInfo, ProjectInfo projectInfo2 ) {
                            Integer indexP1 = modules.indexOf( projectInfo.getName() );
                            Integer indexP2 = modules.indexOf( projectInfo2.getName() );

                            return indexP1.compareTo( indexP2 );
                        }
                    } );

                }

                RepositoryChangeEvent event = getSocialEvent( (String)ctx.getData( "_ProcessName" ),
                        uri,
                        branchToUpdate,
                        version,
                        repositoryService);
                beanManager.fireEvent( event );
            }
            executionResults.setData("UpdatedProjects", updatedProject);
            return executionResults;
        } catch (Throwable e) {
            throw new AssetManagementRuntimeException(e);
        }
	}


    private RepositoryChangeEvent getSocialEvent(String processName,
            String repository,
            String branch,
            String version,
            RepositoryService repositoryService) {

        String projectName = null;
        String repositoryURI = null;

        repositoryURI = DataUtils.readRepositoryURI( repositoryService, repository );

        RepositoryChangeEvent event = new RepositoryChangeEvent(processName,
                repository,
                repositoryURI,
                "system",
                System.currentTimeMillis(),
                RepositoryChangeEvent.ChangeType.VERSION_CHANGED);
        event.addParam( "branch", branch );
        event.addParam( "version", version );
        event.addParam( "project", projectName );

        return event;
    }
}
