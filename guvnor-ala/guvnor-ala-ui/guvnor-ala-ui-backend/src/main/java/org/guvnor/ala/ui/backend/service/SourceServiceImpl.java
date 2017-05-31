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

package org.guvnor.ala.ui.backend.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.ala.ui.service.SourceService;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.project.service.ProjectService;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.jboss.errai.bus.server.annotations.Service;

import static java.util.stream.Collectors.toCollection;

@Service
@ApplicationScoped
public class SourceServiceImpl
        implements SourceService {

    @Inject
    private OrganizationalUnitService organizationalUnitService;

    @Inject
    private RepositoryService repositoryService;

    @Inject
    private ProjectService< ? extends Project > projectService;

    public SourceServiceImpl() {
        //Empty constructor for Weld proxying
    }

    @Override
    public Collection< String > getOrganizationUnits() {
        return organizationalUnitService.getOrganizationalUnits().stream()
                .map(OrganizationalUnit::getName)
                .collect(toCollection(ArrayList::new));
    }

    @Override
    public Collection< String > getRepositories(final String organizationUnit) {
        return organizationalUnitService.getOrganizationalUnit(organizationUnit)
                .getRepositories()
                .stream()
                .map(Repository::getAlias)
                .collect(toCollection(ArrayList::new));
    }

    @Override
    public Collection< String > getBranches(final String repository) {
        //TODO load all branches.
        return Arrays.asList("master");
    }

    @Override
    public Collection< Project > getProjects(final String repositoryAlias,
                                             final String branch) {
        Repository repo = repositoryService.getRepository(repositoryAlias);
        return projectService.getProjects(repo,
                                          branch);
    }
}
