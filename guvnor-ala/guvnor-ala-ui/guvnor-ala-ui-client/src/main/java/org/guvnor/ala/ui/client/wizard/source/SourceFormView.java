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

package org.guvnor.ala.ui.client.wizard.source;

import java.util.ArrayList;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.Event;
import org.guvnor.ala.ui.client.util.ContentChangeHandler;
import org.guvnor.ala.ui.client.widget.FormStatus;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Select;
import org.jboss.errai.common.client.dom.TextInput;
import org.jboss.errai.common.client.dom.Window;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.SinkNative;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import static org.guvnor.ala.ui.client.widget.StyleHelper.*;
import static org.uberfire.commons.validation.PortablePreconditions.*;

@Dependent
@Templated
public class SourceFormView implements IsElement,
                                       SourceFormPresenter.View {

    private SourceFormPresenter presenter;

    @Inject
    @DataField("runtime-form")
    Div runtimeForm;

    @Inject
    @DataField("runtime-name")
    TextInput runtimeName;

    @Inject
    @DataField("ou-form")
    Div ouForm;

    @Inject
    @DataField
    Select ous;

    @Inject
    @DataField("repo-form")
    Div repoForm;

    @Inject
    @DataField
    Select repos;

    @Inject
    @DataField("branch-form")
    Div branchForm;

    @Inject
    @DataField
    Select branches;

    @Inject
    @DataField("project-form")
    Div projectForm;

    @Inject
    @DataField
    Select projects;

    private final ArrayList< ContentChangeHandler > changeHandlers = new ArrayList< ContentChangeHandler >();

    @Override
    public void init(final SourceFormPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public String getRuntime() {
        return runtimeName.getValue();
    }

    @Override
    public String getOU() {
        return ous.getValue();
    }

    @Override
    public String getRepository() {
        return repos.getValue();
    }

    @Override
    public String getBranch() {
        return branches.getValue();
    }

    @Override
    public String getProject() {
        return projects.getValue();
    }

    @Override
    public void disable() {
        resetFormState();
        this.runtimeName.setDisabled(true);
        this.ous.setDisabled(true);
        this.repos.setDisabled(true);
        this.branches.setDisabled(true);
        this.projects.setDisabled(true);
    }

    @Override
    public void enable() {
        resetFormState();
        this.runtimeName.setDisabled(false);
        this.ous.setDisabled(false);
        this.repos.setDisabled(false);
        this.branches.setDisabled(false);
        this.projects.setDisabled(false);
    }

    @Override
    public void setRuntimeStatus(final FormStatus status) {
        checkNotNull("status",
                     status);
        if (status.equals(FormStatus.ERROR)) {
            addUniqueEnumStyleName(runtimeForm,
                                   ValidationState.class,
                                   ValidationState.ERROR);
        } else {
            addUniqueEnumStyleName(runtimeForm,
                                   ValidationState.class,
                                   ValidationState.NONE);
        }
    }

    @SinkNative(Event.ONCHANGE)
    @EventHandler("runtime-name")
    public void onNameChange(final Event event) {
        if (!runtimeName.getValue().trim().isEmpty()) {
            addUniqueEnumStyleName(runtimeForm,
                                   ValidationState.class,
                                   ValidationState.NONE);
        }
        fireChangeHandlers();
    }

    @Override
    public void setOUStatus(final FormStatus status) {
        checkNotNull("status",
                     status);
        if (status.equals(FormStatus.ERROR)) {
            addUniqueEnumStyleName(ouForm,
                                   ValidationState.class,
                                   ValidationState.ERROR);
        } else {
            addUniqueEnumStyleName(ouForm,
                                   ValidationState.class,
                                   ValidationState.NONE);
        }
    }

    @Override
    public void setRepositoryStatus(final FormStatus status) {
        checkNotNull("status",
                     status);
        if (status.equals(FormStatus.ERROR)) {
            addUniqueEnumStyleName(repoForm,
                                   ValidationState.class,
                                   ValidationState.ERROR);
        } else {
            addUniqueEnumStyleName(repoForm,
                                   ValidationState.class,
                                   ValidationState.NONE);
        }
    }

    @Override
    public void setBranchStatus(final FormStatus status) {
        checkNotNull("status",
                     status);
        if (status.equals(FormStatus.ERROR)) {
            addUniqueEnumStyleName(branchForm,
                                   ValidationState.class,
                                   ValidationState.ERROR);
        } else {
            addUniqueEnumStyleName(branchForm,
                                   ValidationState.class,
                                   ValidationState.NONE);
        }
    }

    @Override
    public void setProjectStatus(final FormStatus status) {
        checkNotNull("status",
                     status);
        if (status.equals(FormStatus.ERROR)) {
            addUniqueEnumStyleName(projectForm,
                                   ValidationState.class,
                                   ValidationState.ERROR);
        } else {
            addUniqueEnumStyleName(projectForm,
                                   ValidationState.class,
                                   ValidationState.NONE);
        }
    }

    @Override
    public void clear() {
        resetFormState();
        this.clearOrganizationUnits();
        this.clearRepositories();
        this.clearBranches();
        this.clearProjects();
        this.runtimeName.setValue("");
        this.ous.setValue("");
        this.repos.setValue("");
        this.branches.setValue("");
        this.projects.setValue("");
    }

    @Override
    public void addContentChangeHandler(final ContentChangeHandler contentChangeHandler) {
        changeHandlers.add(contentChangeHandler);
    }

    @Override
    public String getWizardTitle() {
        return "Source Selection";
    }

    @Override
    public void clearOrganizationUnits() {
        for (int i = 0; i < ous.getOptions().getLength() + 1; i++) {
            ous.remove(i);
        }
        ous.setInnerHTML("");
        ous.add(defaultOption(),
                null);
    }

    @Override
    public void addOrganizationUnit(final String ou) {
        final HTMLElement option = Window.getDocument().createElement("option");
        option.setAttribute("value",
                            ou);
        option.setTextContent(ou);
        ous.add(option,
                null);
    }

    @EventHandler("ous")
    private void onOrganizationalUnitChange(final ChangeEvent event) {
        if (!ous.getValue().trim().isEmpty()) {
            addUniqueEnumStyleName(ouForm,
                                   ValidationState.class,
                                   ValidationState.NONE);
            clearRepositories();
            clearBranches();
            clearProjects();
            presenter.loadRepositories(getOU());
        } else {
            addUniqueEnumStyleName(ouForm,
                                   ValidationState.class,
                                   ValidationState.ERROR);
        }
        fireChangeHandlers();
    }

    @Override
    public void clearRepositories() {
        for (int i = 0; i < repos.getOptions().getLength() + 1; i++) {
            repos.remove(i);
        }
        repos.setInnerHTML("");
        repos.add(defaultOption(),
                  null);
    }

    @Override
    public void addRepository(final String repo) {
        final HTMLElement option = Window.getDocument().createElement("option");
        option.setAttribute("value",
                            repo);
        option.setInnerHTML(repo);
        repos.add(option,
                  null);
    }

    @EventHandler("repos")
    private void onRepositoryChange(final ChangeEvent event) {
        if (!repos.getValue().trim().isEmpty()) {
            addUniqueEnumStyleName(repoForm,
                                   ValidationState.class,
                                   ValidationState.NONE);
            clearBranches();
            clearProjects();
            presenter.loadBranches(getRepository());
        } else {
            addUniqueEnumStyleName(repoForm,
                                   ValidationState.class,
                                   ValidationState.ERROR);
        }
        fireChangeHandlers();
    }

    @Override
    public void clearBranches() {
        for (int i = 0; i < branches.getOptions().getLength() + 1; i++) {
            branches.remove(i);
        }
        branches.setInnerHTML("");
        branches.add(defaultOption(),
                     null);
    }

    @Override
    public void addBranch(final String branch) {
        final HTMLElement option = Window.getDocument().createElement("option");
        option.setAttribute("value",
                            branch);
        option.setInnerHTML(branch);
        branches.add(option,
                     null);
    }

    @EventHandler("branches")
    private void onBranchChange(final ChangeEvent event) {
        if (!branches.getValue().trim().isEmpty()) {
            addUniqueEnumStyleName(branchForm,
                                   ValidationState.class,
                                   ValidationState.NONE);
            clearProjects();
            presenter.loadProjects(getRepository(),
                                   getBranch());
        } else {
            addUniqueEnumStyleName(branchForm,
                                   ValidationState.class,
                                   ValidationState.ERROR);
        }
        fireChangeHandlers();
    }

    @Override
    public void clearProjects() {
        for (int i = 0; i < projects.getOptions().getLength() + 1; i++) {
            projects.remove(i);
        }
        projects.setInnerHTML("");
        projects.add(defaultOption(),
                     null);
    }

    @Override
    public void addProject(String projectName) {
        final HTMLElement option = Window.getDocument().createElement("option");
        option.setAttribute("value",
                            projectName);
        option.setInnerHTML(projectName);
        projects.add(option,
                     null);
    }

    @EventHandler("projects")
    private void onProjectChange(final ChangeEvent event) {
        if (!projects.getValue().trim().isEmpty()) {
            addUniqueEnumStyleName(projectForm,
                                   ValidationState.class,
                                   ValidationState.NONE);
        } else {
            addUniqueEnumStyleName(projectForm,
                                   ValidationState.class,
                                   ValidationState.ERROR);
        }
        fireChangeHandlers();
    }

    private HTMLElement defaultOption() {
        final HTMLElement option = Window.getDocument().createElement("option");
        option.setAttribute("value",
                            "");
        option.setAttribute("disabled",
                            "");
        option.setAttribute("selected",
                            "");
        option.setTextContent("-- select an option --");
        return option;
    }

    private void resetFormState() {
        addUniqueEnumStyleName(runtimeForm,
                               ValidationState.class,
                               ValidationState.NONE);
        addUniqueEnumStyleName(ouForm,
                               ValidationState.class,
                               ValidationState.NONE);
        addUniqueEnumStyleName(repoForm,
                               ValidationState.class,
                               ValidationState.NONE);
        addUniqueEnumStyleName(branchForm,
                               ValidationState.class,
                               ValidationState.NONE);
        addUniqueEnumStyleName(projectForm,
                               ValidationState.class,
                               ValidationState.NONE);
    }

    private void fireChangeHandlers() {
        for (final ContentChangeHandler changeHandler : changeHandlers) {
            changeHandler.onContentChange();
        }
    }
}