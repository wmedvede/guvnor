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

package org.guvnor.ala.ui.openshift.client.pipeline.selector;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;

@ApplicationScoped
public class URISelector {

    public interface View
            extends UberElement<URISelector> {

        HasData<URISelectorPageRow> getDisplay();

        URISelectorPageRow getSelectedRow();

        void show();

        void show(final String title);
    }

    private final View view;

    private ListDataProvider<URISelectorPageRow> dataProvider = new ListDataProvider<>();

    private ParameterizedCommand<URIInfo> onSelectCommand;

    private Command onCloseCommand;

    @Inject
    public URISelector(final View view) {
        this.view = view;
    }

    @PostConstruct
    public void init() {
        view.init(this);
        dataProvider.addDataDisplay(view.getDisplay());
    }

    public void show(final String title,
                     final List<URIInfo> uriInfos,
                     final ParameterizedCommand<URIInfo> onSelectCommand,
                     final Command onCloseCommand) {
        this.onSelectCommand = onSelectCommand;
        this.onCloseCommand = onCloseCommand;

        dataProvider.getList().clear();
        for (URIInfo uriInfo : uriInfos) {
            dataProvider.getList().add(new URISelectorPageRow(uriInfo));
        }
        dataProvider.flush();
        view.show(title);
    }

    public void onClose() {
        dataProvider.getList().clear();
        if (onCloseCommand != null) {
            onCloseCommand.execute();
        }
    }

    public void onSelect() {
        if (onSelectCommand != null) {
            onSelectCommand.execute(view.getSelectedRow().getURIInfo());
        }
    }
}