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
package org.guvnor.ala.registry.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.guvnor.ala.build.Project;
import org.guvnor.ala.registry.SourceRegistry;
import org.guvnor.ala.source.Repository;
import org.guvnor.ala.source.Source;
import org.uberfire.java.nio.file.Path;

/**
 * Base implementation of a SourceRegistry.
 */
public abstract class BaseSourceRegistry
        implements SourceRegistry {

    private Map<Path, Repository> repositorySourcesPath = new ConcurrentHashMap<>();
    //Store the repository id and path for reverse lookup
    private Map<String, Path> pathByRepositoryId = new ConcurrentHashMap<>();
    private Map<Repository, List<Project>> projectsByRepo = new ConcurrentHashMap<>();
    private Map<Repository, List<Source>> sourceByRepo = new ConcurrentHashMap<>();
    private Map<Source, Project> projectBySource = new ConcurrentHashMap<>();

    @Override
    public void registerRepositorySources(final Path path,
                                          final Repository repo) {
        repositorySourcesPath.put(path,
                                  repo);
        pathByRepositoryId.put(repo.getId(),
                               path);
    }

    @Override
    public Path getRepositoryPath(final Repository repo) {
        return pathByRepositoryId.get(repo.getId());
    }

    @Override
    public Path getRepositoryPathById(final String repoId) {
        return pathByRepositoryId.get(repoId);
    }

    @Override
    public Repository getRepositoryByPath(final Path path) {
        return repositorySourcesPath.get(path);
    }

    @Override
    public List<Repository> getAllRepositories() {
        return new ArrayList<>(repositorySourcesPath.values());
    }

    @Override
    public void registerProject(final Repository repo,
                                final Project project) {
        projectsByRepo.putIfAbsent(repo,
                                   new ArrayList<>());
        projectsByRepo.get(repo).add(project);
    }

    @Override
    public List<Project> getAllProjects(final Repository repository) {
        Path repoPath = pathByRepositoryId.get(repository.getId());
        List<Project> allProjects = new ArrayList<>();
        for (Source s : projectBySource.keySet()) {
            if (projectBySource.get(s).getRootPath().equals(repoPath)) {
                allProjects.add(projectBySource.get(s));
            }
        }
        return allProjects;
    }

    @Override
    public Repository getRepositoryById(final String repositoryId) {
        return repositorySourcesPath.get(pathByRepositoryId.get(repositoryId));
    }

    @Override
    public void registerSource(final Repository repo,
                               final Source source) {
        sourceByRepo.putIfAbsent(repo,
                                 new ArrayList<>());
        sourceByRepo.get(repo).add(source);
    }

    @Override
    public void registerProject(final Source source,
                                final Project project) {
        projectBySource.put(source,
                            project);
    }
}
