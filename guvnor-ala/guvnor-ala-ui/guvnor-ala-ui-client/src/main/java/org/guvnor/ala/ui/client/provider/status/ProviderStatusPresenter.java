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

package org.guvnor.ala.ui.client.provider.status;

import java.util.Collection;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.guvnor.ala.ui.client.provider.status.runtime.RuntimePresenter;
import org.guvnor.ala.ui.model.RuntimeListItem;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.ioc.client.container.IOC;
import org.guvnor.ala.ui.model.Runtime;

@Dependent
public class ProviderStatusPresenter {

    public interface View extends IsElement {

        void addListItem( final IsElement listItem );

        void clear( );
    }

    private final View view;

    @Inject
    public ProviderStatusPresenter( final View view ) {
        this.view = view;
    }

    /*
    public void setup( final Collection<Runtime> response ) {
        view.clear();
        for ( final Runtime runtime : response ) {
            final RuntimePresenter runtimePresenter = IOC.getBeanManager().lookupBean( RuntimePresenter.class ).getInstance();
            runtimePresenter.setup( runtime );
            view.addListItem( runtimePresenter.getView() );
        }
    }
    */

    public void setupItems(final Collection<RuntimeListItem> response) {
        view.clear();
        for (final RuntimeListItem item : response) {
            final RuntimePresenter runtimePresenter = newRuntimePresenter();
            runtimePresenter.setup(item);
            view.addListItem(runtimePresenter.getView());
        }
    }

    public View getView() {
        return view;
    }

    private RuntimePresenter newRuntimePresenter() {
        return IOC.getBeanManager().lookupBean(RuntimePresenter.class).getInstance();
    }
}
