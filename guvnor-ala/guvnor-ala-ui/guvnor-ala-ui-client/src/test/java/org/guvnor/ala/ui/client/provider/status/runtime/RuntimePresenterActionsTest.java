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

package org.guvnor.ala.ui.client.provider.status.runtime;

import org.guvnor.ala.ui.events.PipelineExecutionChange;
import org.guvnor.ala.ui.events.PipelineExecutionChangeEvent;
import org.guvnor.ala.ui.events.RuntimeChange;
import org.guvnor.ala.ui.events.RuntimeChangeEvent;
import org.guvnor.ala.ui.model.PipelineExecutionTraceKey;
import org.guvnor.ala.ui.model.PipelineStatus;
import org.guvnor.ala.ui.model.RuntimeKey;
import org.guvnor.ala.ui.model.RuntimeListItem;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.events.NotificationEvent;

import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.RuntimePresenter_PipelineExecutionAlreadyStoppedMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.RuntimePresenter_PipelineExecutionConfirmDeleteMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.RuntimePresenter_PipelineExecutionConfirmStopMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.RuntimePresenter_PipelineExecutionDeleteSuccessMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.RuntimePresenter_PipelineExecutionStopSuccessMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.RuntimePresenter_RuntimeConfirmDeleteMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.RuntimePresenter_RuntimeConfirmForcedDeleteMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.RuntimePresenter_RuntimeConfirmStopMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.RuntimePresenter_RuntimeDeleteFailedMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.RuntimePresenter_RuntimeDeleteSuccessMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.RuntimePresenter_RuntimeStartSuccessMessage;
import static org.guvnor.ala.ui.client.resources.i18n.GuvnorAlaUIConstants.RuntimePresenter_RuntimeStopSuccessMessage;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the properly execution of the actions.
 */
public class RuntimePresenterActionsTest
        extends RuntimePresenterTest {

    private static final String SUCCESS_MESSAGE = "SUCCESS_MESSAGE";

    private static final String ERROR_MESSAGE = "ERROR_MESSAGE";

    private static final String CONFIRM_MESSAGE = "CONFIRM_MESSAGE";

    private static final String CONFIRM_MESSAGE_2 = "CONFIRM_MESSAGE_2";

    private static final String CONFIRM_MESSAGE_3 = "CONFIRM_MESSAGE_3";

    private static final String TITLE = "TITLE";

    private static final String WARNING_TITLE = "WARNING_TITLE";

    private static final String ERROR_TITLE = "ERROR_TITLE";

    @Mock
    private ErrorCallback<Message> defaultErrorCallback;

    private ArgumentCaptor<Exception> exceptionCaptor;

    private ArgumentCaptor<Command> yesCommandCaptor;

    private ArgumentCaptor<Command> noCommandCaptor;

    @Before
    public void setUp() {
        super.setUp();
        when(popupHelper.getPopupErrorCallback()).thenReturn(defaultErrorCallback);
        exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        yesCommandCaptor = ArgumentCaptor.forClass(Command.class);
        noCommandCaptor = ArgumentCaptor.forClass(Command.class);
    }

    @Test
    public void testOnCurrentPipelineExecutionStopped() {
        preparePipelineExecutionTraceSetup();
        PipelineExecutionTraceKey currentKey = trace.getKey();
        RuntimeListItem item = new RuntimeListItem(RUNTIME_NAME,
                                                   trace);
        presenter.setup(item);
        presenter.onPipelineExecutionChange(new PipelineExecutionChangeEvent(PipelineExecutionChange.STOPPED,
                                                                             currentKey));
        verify(presenter,
               times(1)).refresh(currentKey);
    }

    @Test
    public void testOnOtherPipelineExecutionStopped() {
        preparePipelineExecutionTraceSetup();
        PipelineExecutionTraceKey otherKey = mock(PipelineExecutionTraceKey.class);

        RuntimeListItem item = new RuntimeListItem(RUNTIME_NAME,
                                                   trace);
        presenter.setup(item);
        presenter.onPipelineExecutionChange(new PipelineExecutionChangeEvent(PipelineExecutionChange.STOPPED,
                                                                             otherKey));
        verify(presenter,
               never()).refresh(any(PipelineExecutionTraceKey.class));
    }

    @Test
    public void testOnCurrentRuntimeStarted() {
        prepareRuntime();
        RuntimeKey currentKey = runtime.getKey();
        presenter.onRuntimeChangeEvent(new RuntimeChangeEvent(RuntimeChange.STARTED,
                                                              currentKey));
        verify(presenter,
               times(1)).refresh(currentKey);
    }

    @Test
    public void testOnOtherRuntimeStarted() {
        prepareRuntime();
        RuntimeKey otherKey = mock(RuntimeKey.class);
        presenter.onRuntimeChangeEvent(new RuntimeChangeEvent(RuntimeChange.STARTED,
                                                              otherKey));
        verify(presenter,
               never()).refresh(any(RuntimeKey.class));
    }

    @Test
    public void testOnCurrentRuntimeStopped() {
        prepareRuntime();
        RuntimeKey currentKey = runtime.getKey();
        presenter.onRuntimeChangeEvent(new RuntimeChangeEvent(RuntimeChange.STOPPED,
                                                              currentKey));
        verify(presenter,
               times(1)).refresh(currentKey);
    }

    @Test
    public void testOnOtherRuntimeStopped() {
        prepareRuntime();
        RuntimeKey otherKey = mock(RuntimeKey.class);
        presenter.onRuntimeChangeEvent(new RuntimeChangeEvent(RuntimeChange.STOPPED,
                                                              otherKey));
        verify(presenter,
               never()).refresh(any(RuntimeKey.class));
    }

    @Test
    public void testStartRuntimeSuccessful() {
        prepareRuntime();
        RuntimeKey currentKey = runtime.getKey();

        when(translationService.format(RuntimePresenter_RuntimeStartSuccessMessage,
                                       currentKey.getId())).thenReturn(SUCCESS_MESSAGE);
        presenter.startRuntime();
        verify(notificationEvent,
               times(1)).fire(new NotificationEvent(SUCCESS_MESSAGE,
                                                    NotificationEvent.NotificationType.SUCCESS));
    }

    @Test
    public void testStartRuntimeFailed() {
        prepareRuntime();
        RuntimeKey currentKey = runtime.getKey();

        doThrow(new RuntimeException(ERROR_MESSAGE))
                .when(runtimeService)
                .startRuntime(currentKey);

        presenter.startRuntime();
        verify(notificationEvent,
               times(0)).fire(any(NotificationEvent.class));
        verify(defaultErrorCallback,
               times(1)).error(any(Message.class),
                               exceptionCaptor.capture());
        assertEquals(ERROR_MESSAGE,
                     exceptionCaptor.getValue().getCause().getMessage());
    }

    @Test
    public void testStopRuntimeConfirmNo() {
        prepareRuntimeStop();
        noCommandCaptor.getValue().execute();
        verify(runtimeService,
               never()).stopRuntime(any(RuntimeKey.class));
    }

    @Test
    public void testStopRuntimeConfirmYesAndSuccessful() {
        prepareRuntimeStop();
        RuntimeKey currentKey = runtime.getKey();
        when(translationService.format(RuntimePresenter_RuntimeStopSuccessMessage,
                                       item.getRuntime().getKey().getId())).thenReturn(SUCCESS_MESSAGE);

        yesCommandCaptor.getValue().execute();
        verify(runtimeService,
               times(1)).stopRuntime(currentKey);
        verify(notificationEvent,
               times(1)).fire(new NotificationEvent(SUCCESS_MESSAGE,
                                                    NotificationEvent.NotificationType.SUCCESS));
    }

    @Test
    public void testStopRuntimeConfirmYesAndFailed() {
        prepareRuntimeStop();
        RuntimeKey currentKey = runtime.getKey();

        doThrow(new RuntimeException(ERROR_MESSAGE))
                .when(runtimeService)
                .stopRuntime(currentKey);

        yesCommandCaptor.getValue().execute();
        verify(runtimeService,
               times(1)).stopRuntime(currentKey);
        verify(defaultErrorCallback,
               times(1)).error(any(Message.class),
                               exceptionCaptor.capture());
        assertEquals(ERROR_MESSAGE,
                     exceptionCaptor.getValue().getCause().getMessage());
    }

    private void prepareRuntimeStop() {
        prepareRuntime();
        when(translationService.getTranslation(RuntimePresenter_RuntimeConfirmStopMessage)).thenReturn(CONFIRM_MESSAGE);
        when(popupHelper.InformationTitle()).thenReturn(TITLE);

        presenter.stopRuntime();
        verify(popupHelper,
               times(1)).showYesNoPopup(eq(TITLE),
                                        eq(CONFIRM_MESSAGE),
                                        yesCommandCaptor.capture(),
                                        noCommandCaptor.capture());
    }

    @Test
    public void testDeleteRuntimeConfirmNo() {
        prepareRuntimeDelete();
        noCommandCaptor.getValue().execute();
        verify(runtimeService,
               never()).deleteRuntime(any(RuntimeKey.class),
                                      anyBoolean());
    }

    @Test
    public void testDeleteRuntimeConfirmYesAndSuccessful() {
        prepareRuntimeDelete();
        RuntimeKey currentKey = runtime.getKey();
        when(translationService.format(RuntimePresenter_RuntimeDeleteSuccessMessage,
                                       item.getRuntime().getKey().getId())).thenReturn(SUCCESS_MESSAGE);

        yesCommandCaptor.getValue().execute();
        verify(runtimeService,
               times(1)).deleteRuntime(currentKey,
                                       false);
        verify(notificationEvent,
               times(1)).fire(new NotificationEvent(SUCCESS_MESSAGE,
                                                    NotificationEvent.NotificationType.SUCCESS));
    }

    @Test
    public void testDeleteRuntimeConfirmYesAndFailedAndDontForceDeletion() {
        prepareRuntimeDelete();
        RuntimeKey currentKey = runtime.getKey();

        RuntimeException deleteException = new RuntimeException(ERROR_MESSAGE);
        //needs to use null instead of ERROR_MESSAGE because of the CallerMock
        when(translationService.format(RuntimePresenter_RuntimeDeleteFailedMessage,
                                       null)).thenReturn(CONFIRM_MESSAGE_2);
        when(popupHelper.ErrorTitle()).thenReturn(ERROR_TITLE);

        doThrow(deleteException)
                .when(runtimeService)
                .deleteRuntime(currentKey,
                               false);

        yesCommandCaptor.getValue().execute();
        verify(runtimeService,
               times(1)).deleteRuntime(currentKey,
                                       false);

        //dialog asking if forced deletion is wanted
        verify(popupHelper,
               times(1)).showYesNoPopup(eq(ERROR_TITLE),
                                        eq(CONFIRM_MESSAGE_2),
                                        yesCommandCaptor.capture(),
                                        noCommandCaptor.capture());
        //say no
        noCommandCaptor.getValue().execute();
        //forced deletion will never be produced.
        verify(runtimeService,
               never()).deleteRuntime(currentKey,
                                      true);
    }

    @Test
    public void testDeleteRuntimeConfirmYesAndFailedAndForceDeletion() {
        prepareRuntimeDelete();
        RuntimeKey currentKey = runtime.getKey();

        RuntimeException deleteException = new RuntimeException(ERROR_MESSAGE);
        //needs to use null instead of ERROR_MESSAGE because of the CallerMock
        when(translationService.format(RuntimePresenter_RuntimeDeleteFailedMessage,
                                       null)).thenReturn(CONFIRM_MESSAGE_2);
        when(popupHelper.ErrorTitle()).thenReturn(ERROR_TITLE);

        when(popupHelper.WarningTitle()).thenReturn(WARNING_TITLE);
        when(translationService.getTranslation(RuntimePresenter_RuntimeConfirmForcedDeleteMessage)).thenReturn(CONFIRM_MESSAGE_3);

        doThrow(deleteException)
                .when(runtimeService)
                .deleteRuntime(currentKey,
                               false);

        yesCommandCaptor.getValue().execute();
        verify(runtimeService,
               times(1)).deleteRuntime(currentKey,
                                       false);

        //dialog asking if forced deletion is wanted
        verify(popupHelper,
               times(1)).showYesNoPopup(eq(ERROR_TITLE),
                                        eq(CONFIRM_MESSAGE_2),
                                        yesCommandCaptor.capture(),
                                        noCommandCaptor.capture());
        //say yes
        yesCommandCaptor.getValue().execute();

        //last dialog confirming the forced deletion
        verify(popupHelper,
               times(1)).showYesNoPopup(eq(WARNING_TITLE),
                                        eq(CONFIRM_MESSAGE_3),
                                        yesCommandCaptor.capture(),
                                        noCommandCaptor.capture());
        yesCommandCaptor.getValue().execute();

        //forced deletion will be produced.
        verify(runtimeService,
               times(1)).deleteRuntime(currentKey,
                                       true);
    }

    @Test
    public void testForceDeleteRuntimeConfirmNo() {
        prepareRuntimeForceDelete();
        noCommandCaptor.getValue().execute();
        verify(runtimeService,
               never()).deleteRuntime(any(RuntimeKey.class),
                                      anyBoolean());
    }

    @Test
    public void testForceDeleteRuntimeConfirmYesAndSuccessful() {
        prepareRuntimeForceDelete();
        RuntimeKey currentKey = runtime.getKey();
        when(translationService.format(RuntimePresenter_RuntimeDeleteSuccessMessage,
                                       item.getRuntime().getKey().getId())).thenReturn(SUCCESS_MESSAGE);

        yesCommandCaptor.getValue().execute();
        verify(runtimeService,
               times(1)).deleteRuntime(currentKey,
                                       true);
        verify(notificationEvent,
               times(1)).fire(new NotificationEvent(SUCCESS_MESSAGE,
                                                    NotificationEvent.NotificationType.SUCCESS));
    }

    @Test
    public void testForceDeleteRuntimeConfirmYesAndFailed() {
        prepareRuntimeForceDelete();
        ;
        RuntimeKey currentKey = runtime.getKey();

        doThrow(new RuntimeException(ERROR_MESSAGE))
                .when(runtimeService)
                .deleteRuntime(currentKey,
                               true);

        yesCommandCaptor.getValue().execute();
        verify(runtimeService,
               times(1)).deleteRuntime(currentKey,
                                       true);
        verify(defaultErrorCallback,
               times(1)).error(any(Message.class),
                               exceptionCaptor.capture());
        assertEquals(ERROR_MESSAGE,
                     exceptionCaptor.getValue().getCause().getMessage());
    }

    private void prepareRuntimeDelete() {
        prepareRuntime();
        when(translationService.getTranslation(RuntimePresenter_RuntimeConfirmDeleteMessage)).thenReturn(CONFIRM_MESSAGE);
        when(popupHelper.InformationTitle()).thenReturn(TITLE);

        presenter.deleteRuntime();
        verify(popupHelper,
               times(1)).showYesNoPopup(eq(TITLE),
                                        eq(CONFIRM_MESSAGE),
                                        yesCommandCaptor.capture(),
                                        noCommandCaptor.capture());
    }

    private void prepareRuntimeForceDelete() {
        prepareRuntime();
        when(translationService.getTranslation(RuntimePresenter_RuntimeConfirmForcedDeleteMessage)).thenReturn(CONFIRM_MESSAGE);
        when(popupHelper.WarningTitle()).thenReturn(TITLE);

        presenter.forceDeleteRuntime();
        verify(popupHelper,
               times(1)).showYesNoPopup(eq(TITLE),
                                        eq(CONFIRM_MESSAGE),
                                        yesCommandCaptor.capture(),
                                        noCommandCaptor.capture());
    }

    @Test
    public void testStopPipelineConfirmNo() {
        preparePipelineStop();
        noCommandCaptor.getValue().execute();
        verify(runtimeService,
               never()).stopPipelineExecution(any(PipelineExecutionTraceKey.class));
    }

    @Test
    public void testStopPipelineConfirmYesAndSuccessful() {
        preparePipelineStop();
        PipelineExecutionTraceKey currentKey = trace.getKey();

        when(translationService.format(RuntimePresenter_PipelineExecutionStopSuccessMessage,
                                       currentKey.getId())).thenReturn(SUCCESS_MESSAGE);

        yesCommandCaptor.getValue().execute();
        verify(runtimeService,
               times(1)).stopPipelineExecution(currentKey);
        verify(notificationEvent,
               times(1)).fire(new NotificationEvent(SUCCESS_MESSAGE,
                                                    NotificationEvent.NotificationType.SUCCESS));
    }

    @Test
    public void testStopPipelineConfirmYesAndFailed() {
        preparePipelineStop();
        PipelineExecutionTraceKey currentKey = trace.getKey();

        doThrow(new RuntimeException(ERROR_MESSAGE))
                .when(runtimeService)
                .stopPipelineExecution(currentKey);

        yesCommandCaptor.getValue().execute();
        verify(runtimeService,
               times(1)).stopPipelineExecution(currentKey);
        verify(defaultErrorCallback,
               times(1)).error(any(Message.class),
                               exceptionCaptor.capture());
        assertEquals(ERROR_MESSAGE,
                     exceptionCaptor.getValue().getCause().getMessage());
    }

    @Test
    public void testStopPipelineConfirmYesButWasARuntime() {
        prepareRuntime();

        when(translationService.getTranslation(RuntimePresenter_PipelineExecutionConfirmStopMessage)).thenReturn(CONFIRM_MESSAGE);
        when(popupHelper.InformationTitle()).thenReturn(TITLE);

        when(translationService.getTranslation(RuntimePresenter_PipelineExecutionAlreadyStoppedMessage)).thenReturn(ERROR_MESSAGE);

        presenter.stopPipeline();
        verify(popupHelper,
               times(1)).showYesNoPopup(eq(TITLE),
                                        eq(CONFIRM_MESSAGE),
                                        yesCommandCaptor.capture(),
                                        noCommandCaptor.capture());
        //say yes
        yesCommandCaptor.getValue().execute();
        verify(popupHelper,
               times(1)).showInformationPopup(ERROR_MESSAGE);

        verify(runtimeService,
               never()).stopPipelineExecution(any(PipelineExecutionTraceKey.class));
    }

    private void preparePipelineStop() {
        preparePipeline();
        when(translationService.getTranslation(RuntimePresenter_PipelineExecutionConfirmStopMessage)).thenReturn(CONFIRM_MESSAGE);
        when(popupHelper.InformationTitle()).thenReturn(TITLE);

        presenter.stopPipeline();
        verify(popupHelper,
               times(1)).showYesNoPopup(eq(TITLE),
                                        eq(CONFIRM_MESSAGE),
                                        yesCommandCaptor.capture(),
                                        noCommandCaptor.capture());
    }

    @Test
    public void testDeletePipelineConfirmNo() {
        preparePipelineDelete();
        noCommandCaptor.getValue().execute();
        verify(runtimeService,
               never()).deletePipelineExecution(any(PipelineExecutionTraceKey.class));
    }

    @Test
    public void testDeletePipelineConfirmYesAndSuccessful() {
        preparePipelineDelete();
        PipelineExecutionTraceKey currentKey = trace.getKey();
        when(translationService.format(RuntimePresenter_PipelineExecutionDeleteSuccessMessage,
                                       currentKey.getId())).thenReturn(SUCCESS_MESSAGE);

        yesCommandCaptor.getValue().execute();
        verify(runtimeService,
               times(1)).deletePipelineExecution(currentKey);
        verify(notificationEvent,
               times(1)).fire(new NotificationEvent(SUCCESS_MESSAGE,
                                                    NotificationEvent.NotificationType.SUCCESS));
    }

    @Test
    public void testDeletePipelineConfirmYesAndFailed() {
        preparePipelineDelete();
        PipelineExecutionTraceKey currentKey = trace.getKey();

        doThrow(new RuntimeException(ERROR_MESSAGE))
                .when(runtimeService)
                .deletePipelineExecution(currentKey);

        yesCommandCaptor.getValue().execute();
        verify(runtimeService,
               times(1)).deletePipelineExecution(currentKey);
        verify(defaultErrorCallback,
               times(1)).error(any(Message.class),
                               exceptionCaptor.capture());
        assertEquals(ERROR_MESSAGE,
                     exceptionCaptor.getValue().getCause().getMessage());
    }

    private void preparePipelineDelete() {
        preparePipeline();
        when(translationService.getTranslation(RuntimePresenter_PipelineExecutionConfirmDeleteMessage)).thenReturn(CONFIRM_MESSAGE);
        when(popupHelper.InformationTitle()).thenReturn(TITLE);

        presenter.deletePipeline();
        verify(popupHelper,
               times(1)).showYesNoPopup(eq(TITLE),
                                        eq(CONFIRM_MESSAGE),
                                        yesCommandCaptor.capture(),
                                        noCommandCaptor.capture());
    }

    private void prepareRuntime() {
        runtime = mockRuntime();
        item = new RuntimeListItem(RUNTIME_NAME,
                                   runtime);
        presenter.setup(item);
    }

    private void preparePipeline() {
        trace = mockPipelineExecutionTrace(EXECUTION_ID,
                                           mockPipeline(PIPELINE_NAME,
                                                        STAGE_NUMBER),
                                           mock(PipelineStatus.class),
                                           mock(PipelineStatus.class));
        item = new RuntimeListItem(RUNTIME_NAME,
                                   trace);
        presenter.setup(item);
    }
}
