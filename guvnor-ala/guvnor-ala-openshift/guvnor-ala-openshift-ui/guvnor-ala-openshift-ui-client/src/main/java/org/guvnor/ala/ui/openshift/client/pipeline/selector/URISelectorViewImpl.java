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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.HasData;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.ext.widgets.common.client.common.popups.BaseModal;
import org.uberfire.ext.widgets.common.client.tables.PagedTable;

@Templated
@Dependent
public class URISelectorViewImpl
        implements IsElement,
                   URISelector.View {

    @DataField("items-table")
    private PagedTable<URISelectorPageRow> dataGrid = new PagedTable<>(5);

    private BaseModal modal;

    private URISelectorPageRow selectedRow;

    private boolean cancelNextHiddenEvent = false;

    private URISelector presenter;

    @Inject
    private TranslationService translationService;

    public URISelectorViewImpl() {
    }

    @PostConstruct
    private void init() {
        dataGrid.setHeight("200px");
        dataGrid.setColumnPickerButtonVisible(false);
        dataGrid.setEmptyTableCaption("no elements");

        Column<URISelectorPageRow, String> uriColumn = new Column<URISelectorPageRow, String>(new TextCell()) {
            @Override
            public String getValue(URISelectorPageRow row) {
                return row.getURIInfo().getURI();
            }
        };
        dataGrid.addColumn(uriColumn,
                           "URL Column");
        Column<URISelectorPageRow, String> selectorColumn = new Column<URISelectorPageRow, String>(
                new ButtonCell(ButtonType.PRIMARY,
                               ButtonSize.SMALL)) {
            @Override
            public String getValue(URISelectorPageRow row) {
                return "Select";
            }
        };

        selectorColumn.setFieldUpdater((index, row, value) -> {
            selectedRow = row;
            cancelNextHiddenEvent = true;
            modal.hide();
            presenter.onSelect();
        });
        dataGrid.addColumn(selectorColumn,
                           "");

        this.modal = new BaseModal();
        this.modal.setTitle("URL Selector");
        this.modal.setBody(ElementWrapperWidget.getWidget(this.getElement()));
        this.modal.addHiddenHandler(event -> {
            if (!cancelNextHiddenEvent) {
                presenter.onClose();
            }
            cancelNextHiddenEvent = false;
        });
    }

    @Override
    public void init(final URISelector presenter) {
        this.presenter = presenter;
    }

    @Override
    public HasData<URISelectorPageRow> getDisplay() {
        return dataGrid;
    }

    @Override
    public URISelectorPageRow getSelectedRow() {
        return selectedRow;
    }

    public void show() {
        cancelNextHiddenEvent = false;
        modal.show();
    }

    @Override
    public void show(final String title) {
        this.modal.setTitle(title);
        show();
    }
}
