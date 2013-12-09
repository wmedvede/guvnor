/*
 * Copyright 2013 JBoss Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.guvnor.udc.client.gfsinfo;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.guvnor.udc.client.i8n.Constants;
import org.guvnor.udc.model.GfsSummary;
import org.guvnor.udc.service.UDCServiceEntryPoint;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.Caller;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchPopup;
import org.uberfire.client.mvp.UberView;
import org.uberfire.lifecycle.OnOpen;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.security.Identity;
import org.uberfire.workbench.events.BeforeClosePlaceEvent;

import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;

@Dependent
@WorkbenchPopup(identifier = "Info GFS")
public class GfsUsageDataPresenter {

    public GfsUsageDataPresenter() {

    }

    private PlaceRequest place;

    @Inject
    GfsUsageDataView view;

    @Inject
    Identity identity;

    @Inject
    private Event<BeforeClosePlaceEvent> closePlaceEvent;

    @Inject
    private Caller<UDCServiceEntryPoint> usageDataService;

    @WorkbenchPartTitle
    public String getTitle() {
        return constants.Gfs_info();
    }

    @WorkbenchPartView
    public UberView<GfsUsageDataPresenter> getView() {
        return view;
    }

    private Constants constants = GWT.create(Constants.class);

    public interface GfsUsageDataView extends UberView<GfsUsageDataPresenter> {
        void displayNotification(String text);

        TextBox getPathText();

        TextBox getUriText();

        TextBox getFileSystemText();

        TextArea getTextAreaInbox();

    }

    @PostConstruct
    public void init() {
    }

    @OnStartup
    public void onStart(final PlaceRequest place) {
        this.place = place;

    }

    @OnOpen
    public void onReveal() {
        getInfoGfs();
    }

    private void getInfoGfs() {
        usageDataService.call(new RemoteCallback<GfsSummary>() {
            @Override
            public void callback(GfsSummary summary) {
                if (summary != null) {
                    refreshData(summary);
                }

            }
        }).getInfoGfs();
    }

    private void refreshData(GfsSummary summary) {
        view.getPathText().setText(summary.getPath());
        view.getUriText().setText(summary.getUriPath());
        view.getFileSystemText().setText(summary.getFileSystem());
        view.getTextAreaInbox().setText("-");
        if (summary.getInboxUsers() != null) {
            StringBuilder inbox = new StringBuilder();
            for (String user : summary.getInboxUsers()) {
                if (!user.equals("mailman")) {
                    inbox.append(user).append("\n");
                }
            }
            view.getTextAreaInbox().setText(inbox.toString());
        }
    }

    public void close() {
        closePlaceEvent.fire(new BeforeClosePlaceEvent(this.place));
    }

}
