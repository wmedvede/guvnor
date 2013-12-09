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

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.guvnor.udc.client.i8n.Constants;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.workbench.events.NotificationEvent;

import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;

@Dependent
@Templated(value = "GfsUsageDataViewImpl.html")
public class GfsUsageDataViewImpl extends Composite implements GfsUsageDataPresenter.GfsUsageDataView {

    private Constants constants = GWT.create(Constants.class);

    private GfsUsageDataPresenter presenter;

    @Inject
    private Event<NotificationEvent> notification;

    @Inject
    @DataField
    public Label vfsLabel;

    @Inject
    @DataField
    public Label inboxLabel;

    @Inject
    @DataField
    public TextArea textAreaInbox;

    @Inject
    @DataField
    public Label pathLabel;

    @Inject
    @DataField
    public TextBox pathText;

    @Inject
    @DataField
    public Label uriLabel;

    @Inject
    @DataField
    public TextBox uriText;

    @Inject
    @DataField
    public Label fileSystemLabel;

    @Inject
    @DataField
    public TextBox fileSystemText;

    @Override
    public void init(GfsUsageDataPresenter presenter) {
        this.presenter = presenter;
        this.setTextAllLabels();
        this.disableAllTextBox();
    }

    private void setTextAllLabels() {
        inboxLabel.setText(constants.Inbox_Existing());
        vfsLabel.setText(constants.Vfs());
        pathLabel.setText(constants.Path());
        uriLabel.setText(constants.Uri());
        fileSystemLabel.setText(constants.FileSystem());
    }

    private void disableAllTextBox() {
        pathText.setEnabled(false);
        uriText.setEnabled(false);
        fileSystemText.setEnabled(false);
        textAreaInbox.setEnabled(false);
    }

    @Override
    public void displayNotification(String text) {
        notification.fire(new NotificationEvent(text));
    }

    public TextArea getTextAreaInbox() {
        return textAreaInbox;
    }

    @Override
    public TextBox getPathText() {
        return pathText;
    }

    @Override
    public TextBox getUriText() {
        return uriText;
    }

    @Override
    public TextBox getFileSystemText() {
        return fileSystemText;
    }

}
