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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Widget;
import org.guvnor.ala.ui.client.util.ContentChangeHandler;
import org.guvnor.ala.ui.client.widget.FormStatus;
import org.guvnor.ala.ui.model.IDataSourceInfo;
import org.guvnor.ala.ui.model.InternalGitSource;
import org.guvnor.ala.ui.model.Provider;
import org.guvnor.ala.ui.model.Source;
import org.guvnor.ala.ui.service.IProvisioningService;
import org.guvnor.ala.ui.service.SourceService;
import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.ext.widgets.common.client.common.popups.errors.ErrorPopup;
import org.uberfire.ext.widgets.core.client.wizards.WizardPage;
import org.uberfire.ext.widgets.core.client.wizards.WizardPageStatusChangeEvent;

import static org.guvnor.ala.ui.client.util.UIUtil.getIntValue;
import static org.guvnor.ala.ui.client.util.UIUtil.getStringValue;
import static org.guvnor.ala.ui.model.WF10ProviderConfigParams.HOST;
import static org.guvnor.ala.ui.model.WF10ProviderConfigParams.MANAGEMENT_PORT;
import static org.guvnor.ala.ui.model.WF10ProviderConfigParams.PASSWORD;
import static org.guvnor.ala.ui.model.WF10ProviderConfigParams.USER;

@Dependent
public class SourceFormPresenter implements WizardPage {

    @Override
    public Widget asWidget() {
        return ElementWrapperWidget.getWidget(view.getElement());
    }

    @Override
    public String getTitle() {
        return view.getWizardTitle();
    }

    @Override
    public void isComplete(final Callback< Boolean > callback) {
        isValid(callback);
    }

    @Override
    public void initialise() {

    }

    @Override
    public void prepareView() {

    }

    public Source buildSource() {
        return new InternalGitSource(getOU(),
                                     getRepository(),
                                     getBranch(),
                                     getProject(),
                                     getDataSourceInfo());
    }

    public interface View extends UberElement< SourceFormPresenter > {

        String getRuntime();

        String getOU();

        String getRepository();

        String getBranch();

        String getProject();

        String getDataSource();

        void disable();

        void enable();

        void setRuntimeStatus(final FormStatus status);

        void setOUStatus(final FormStatus formStatus);

        void setRepositoryStatus(final FormStatus formStatus);

        void setBranchStatus(final FormStatus formStatus);

        void setProjectStatus(final FormStatus formStatus);

        void setDataSourceStatus(final FormStatus status);

        void clear();

        void addContentChangeHandler(ContentChangeHandler contentChangeHandler);

        String getWizardTitle();

        void clearRepositories();

        void addRepository(String repo);

        void clearBranches();

        void addBranch(String branch);

        void clearOrganizationUnits();

        void addOrganizationUnit(String ou);

        void clearProjects();

        void addProject(String projectName);

        void clearDataSources();

        void addDataSource(String dataSourceName, String value);
    }

    private View view;
    private Caller< SourceService > serviceCaller;
    private Caller<IProvisioningService> provisioningService;
    private Event< WizardPageStatusChangeEvent > wizardPageStatusChangeEvent;
    private Map< String, IDataSourceInfo > dataSourceInfos = new HashMap<>( );

    public SourceFormPresenter() {

    }

    @Inject
    public SourceFormPresenter(final View view,
                               final Caller< SourceService > serviceCaller,
                               final Caller<IProvisioningService> provisioningService,
                               final Event< WizardPageStatusChangeEvent > wizardPageStatusChangeEvent) {
        this.view = view;
        this.serviceCaller = serviceCaller;
        this.provisioningService = provisioningService;
        this.wizardPageStatusChangeEvent = wizardPageStatusChangeEvent;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    public View getView() {
        return view;
    }

    public void addContentChangeHandler(final ContentChangeHandler changeHandler) {
        final ContentChangeHandler contentChangeHandler = () -> {
            changeHandler.onContentChange();
            wizardPageStatusChangeEvent.fire(new WizardPageStatusChangeEvent(SourceFormPresenter.this));
        };

        view.addContentChangeHandler(contentChangeHandler);
    }

    public void clear() {
        view.clear();
    }

    public void setup(Provider provider) {
        loadDataSources(provider);
        loadOUs();
    }

    public void isValid(final Callback< Boolean > callback) {
        boolean isValid = true;
        if (getRuntime().trim().isEmpty()) {
            view.setRuntimeStatus(FormStatus.ERROR);
            isValid = false;
        } else {
            view.setRuntimeStatus(FormStatus.VALID);
        }

        if (getOU().trim().isEmpty()) {
            view.setOUStatus(FormStatus.ERROR);
            isValid = false;
        } else {
            view.setOUStatus(FormStatus.VALID);
        }

        if (getRepository().trim().isEmpty()) {
            view.setRepositoryStatus(FormStatus.ERROR);
            isValid = false;
        } else {
            view.setRepositoryStatus(FormStatus.VALID);
        }

        if (getBranch().trim().isEmpty()) {
            view.setBranchStatus(FormStatus.ERROR);
            isValid = false;
        } else {
            view.setBranchStatus(FormStatus.VALID);
        }

        if (getProject().trim().isEmpty()) {
            view.setProjectStatus(FormStatus.ERROR);
            isValid = false;
        } else {
            view.setProjectStatus(FormStatus.VALID);
        }

        callback.callback(isValid);
    }

    public String getRuntime() {
        return view.getRuntime();
    }

    private String getBranch() {
        return view.getBranch();
    }

    private String getRepository() {
        return view.getRepository();
    }

    private String getOU() {
        return view.getOU();
    }

    private String getProject() {
        return view.getProject();
    }

    private IDataSourceInfo getDataSourceInfo() {
        String dataSourceId = view.getDataSource() == null ? "" : view.getDataSource().trim();
        return !dataSourceId.isEmpty() ? dataSourceInfos.get(dataSourceId) : null;
    }

    public String getWizardTitle() {
        return view.getWizardTitle();
    }

    public void disable() {
        view.disable();
    }

    public void loadOUs() {
        serviceCaller.call(new RemoteCallback< Collection< String > >() {
            @Override
            public void callback(final Collection< String > ous) {
                view.clearOrganizationUnits();
                for (String ou : ous) {
                    view.addOrganizationUnit(ou);
                }
                view.clearRepositories();
                view.clearBranches();
                view.clearProjects();
            }
        }).getOrganizationUnits();
    }

    private void loadDataSources(Provider provider) {
        provisioningService.call( loadDataSourcesSuccessCallback(), loadDataSourcesErrorCallback() ).findAvailableDataSources(
                getStringValue(provider.getValues(), HOST),
                getIntValue(provider.getValues(), MANAGEMENT_PORT, 9990),
                getStringValue(provider.getValues(), USER),
                getStringValue(provider.getValues(), PASSWORD),
                "ManagementRealm");
    }

    private RemoteCallback< Collection< IDataSourceInfo > > loadDataSourcesSuccessCallback() {
        return this::loadDataSources;
    }

    private ErrorCallback< Message > loadDataSourcesErrorCallback() {
        return (message, throwable) -> {
            ErrorPopup.showMessage("An error was produced while loading data sources information: " + throwable.getMessage());
            return false;
        };
    }

    private void loadDataSources( Collection< IDataSourceInfo > dataSources ) {
        view.clearDataSources();
        dataSourceInfos.clear();
        dataSources.forEach( dsInfo -> {
            this.dataSourceInfos.put( dsInfo.getDeploymentId( ), dsInfo );
            view.addDataSource(buildDescription(dsInfo), dsInfo.getDeploymentId());
        } );
    }

    private String buildDescription( IDataSourceInfo dsInfo ) {
        return dsInfo.isKieDataSource( ) ? dsInfo.getName( ) : ( dsInfo.getName() + "(external)" );
    }

    public void loadRepositories(final String ou) {
        serviceCaller.call(new RemoteCallback< Collection< String > >() {
            @Override
            public void callback(final Collection< String > repos) {
                view.clearRepositories();
                for (String repo : repos) {
                    view.addRepository(repo);
                }
                view.clearBranches();
                view.clearProjects();
            }
        }).getRepositories(ou);
    }

    public void loadBranches(final String repository) {
        serviceCaller.call(new RemoteCallback< Collection< String > >() {
            @Override
            public void callback(final Collection< String > branches) {
                view.clearBranches();
                for (String branch : branches) {
                    view.addBranch(branch);
                }
                view.clearProjects();
            }
        }).getBranches(repository);
    }

    public void loadProjects(String repository,
                             String branch) {
        serviceCaller.call(new RemoteCallback< Collection< Project > >() {
            @Override
            public void callback(Collection< Project > projects) {
                view.clearProjects();
                projects.forEach(project -> view.addProject(project.getProjectName()));
            }
        }).getProjects(repository,
                       branch);
    }


}
