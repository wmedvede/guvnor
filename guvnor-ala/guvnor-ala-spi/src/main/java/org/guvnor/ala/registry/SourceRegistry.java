/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.guvnor.ala.registry;

import java.util.List;

import org.guvnor.ala.build.Project;
import org.guvnor.ala.source.Repository;
import org.guvnor.ala.source.Source;
import org.uberfire.java.nio.file.Path;

/**
 * Represents the SourceRegistry source, projects & repositories are registered
 */
public interface SourceRegistry {

    /**
     * Register a repository containing source code projects.
     * @param path to the repository
     * @param repo the repository to register.
     * @see Repository
     */
    void registerRepositorySources(final Path path,
                                   final Repository repo);

    /**
     * Get the path for a given repository
     * @param repo the repository for getting the path.
     * @return Path where the repository is stored
     * @see Path
     * @see Repository
     */
    Path getRepositoryPath(final Repository repo);

    /**
     * Get the repository for a given path
     * @param location the path for getting the repository.
     * @return the repository associated with the path.
     * @see Path
     * @see Repository
     */
    Repository getRepositoryByPath(final Path location);

    /**
     * Get All the registered repositories
     * @return List<Repository> with all the registered repositories
     * @see Repository
     */
    List<Repository> getAllRepositories();

    /**
     * Get the repository path by repository Id
     * @param repoId the repository identifier.
     * @return Path where the repository is located
     * @see Path
     */
    Path getRepositoryPathById(final String repoId);

    /**
     * Registers a Project from a repository
     * @param repo the repository for registering the project.
     * @param project a project to be registered
     * @see Repository
     * @see Project
     */
    void registerProject(final Repository repo,
                         final Project project);

    /**
     * Get All the registered projects for a given repository
     * @param repo the repository for getting the projects.
     * @return a list with all the registered projects for the given repository.
     * @see Repository
     * @see Project
     */
    List<Project> getAllProjects(final Repository repo);

    /**
     * Gets a Repository filtered by Id
     * @param repositoryId the repository identifier.
     * @return the registered repository, when exists, null in any other case.
     * @see Repository
     */
    Repository getRepositoryById(final String repositoryId);

    /**
     * Register a Source code from a Repository
     * @param repo the repository for registering the source.
     * @param source a source to be registered
     * @see Source
     * @see Project
     */
    void registerSource(final Repository repo,
                        final Source source);

    /**
     * Register a Project code from a Source
     * @param source the source for registering the project.
     * @param project a project to be registered.
     * @see Source
     * @see Project
     */
    void registerProject(final Source source,
                         final Project project);
}
