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

package org.guvnor.ala.ui.client.wizard.providertype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.guvnor.ala.ui.client.util.ContentChangeHandler;
import org.guvnor.ala.ui.client.wizard.providertype.item.ProviderTypeItemPresenter;
import org.guvnor.ala.ui.model.ProviderType;
import org.guvnor.ala.ui.model.ProviderTypeStatus;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.uberfire.commons.data.Pair;
import org.uberfire.ext.widgets.core.client.wizards.WizardPageStatusChangeEvent;
import org.uberfire.mocks.EventSourceMock;

import static org.guvnor.ala.ui.client.ProvisioningManagementTestCommons.buildProviderTypeStatusList;
import static org.guvnor.ala.ui.client.ProvisioningManagementTestCommons.mockProviderTypeList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class EnableProviderTypePagePresenterTest {

    @Mock
    private EnableProviderTypePagePresenter.View view;

    @Mock
    private EventSourceMock<WizardPageStatusChangeEvent> wizardPageStatusChangeEvent;

    @Mock
    private ManagedInstance<ProviderTypeItemPresenter> providerTypeItemPresenterInstance;

    private EnableProviderTypePagePresenter presenter;

    private List<ProviderType> providerTypes;

    private List<Pair<ProviderType, ProviderTypeStatus>> providerTypeStatus;

    private List<ProviderTypeItemPresenter> itemPresenters;

    @Before
    public void setUp() {

        //mock an arbitrary set of provider types.
        providerTypes = mockProviderTypeList(3);
        providerTypeStatus = buildProviderTypeStatusList(providerTypes,
                                                         ProviderTypeStatus.DISABLED);
        itemPresenters = new ArrayList<>();
        presenter = new EnableProviderTypePagePresenter(view,
                                                        wizardPageStatusChangeEvent,
                                                        providerTypeItemPresenterInstance) {
            @Override
            protected ProviderTypeItemPresenter newProviderTypeItemPresenter() {
                ProviderTypeItemPresenter itemPresenter = mock(ProviderTypeItemPresenter.class);
                when(itemPresenter.getView()).thenReturn(mock(IsElement.class));
                itemPresenters.add(itemPresenter);
                when(providerTypeItemPresenterInstance.get()).thenReturn(itemPresenter);
                return super.newProviderTypeItemPresenter();
            }
        };
        presenter.init();
    }

    /**
     * Tests the presenter setup.
     */
    @Test
    public void testSetup() {
        presenter.setup(providerTypeStatus);

        verify(view,
               times(1)).clear();
        assertEquals(providerTypeStatus.size(),
                     itemPresenters.size());
        for (int i = 0; i < itemPresenters.size(); i++) {
            ProviderTypeItemPresenter itemPresenter = itemPresenters.get(i);
            Pair<ProviderType, ProviderTypeStatus> pair = providerTypeStatus.get(i);
            verify(itemPresenter,
                   times(1)).setup(pair.getK1(),
                                   pair.getK2());
            verify(itemPresenter,
                   times(1)).addContentChangeHandler(any(ContentChangeHandler.class));
            verify(view,
                   times(1)).addProviderType(itemPresenter.getView());
        }
        verify(providerTypeItemPresenterInstance,
               times(providerTypeStatus.size())).get();
    }

    @Test
    public void testPageNotCompleted() {
        presenter.setup(providerTypeStatus);
        itemPresenters.forEach(itemPresenter -> when(itemPresenter.isSelected()).thenReturn(false));
        //no item selected
        presenter.isComplete(Assert::assertFalse);
    }

    @Test
    public void testPageCompleted() {
        presenter.setup(providerTypeStatus);
        itemPresenters.forEach(itemPresenter -> when(itemPresenter.isSelected()).thenReturn(false));
        //select some items.
        int selected1 = 0;
        int selected2 = 2;
        when(itemPresenters.get(selected1).isSelected()).thenReturn(true);
        when(itemPresenters.get(selected1).getProviderType()).thenReturn(providerTypes.get(selected1));

        when(itemPresenters.get(selected2).isSelected()).thenReturn(true);
        when(itemPresenters.get(selected2).getProviderType()).thenReturn(providerTypes.get(selected2));

        //no item selected
        presenter.isComplete(Assert::assertTrue);

        Collection<ProviderType> selectedItems = presenter.getSelectedProviderTypes();
        assertEquals(providerTypes.get(selected1),
                     selectedItems.iterator().next());
        assertEquals(providerTypes.get(selected1),
                     selectedItems.iterator().next());
    }
}
