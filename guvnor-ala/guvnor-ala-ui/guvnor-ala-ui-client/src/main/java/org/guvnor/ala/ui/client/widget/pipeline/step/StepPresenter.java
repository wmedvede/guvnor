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

package org.guvnor.ala.ui.client.widget.pipeline.step;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.guvnor.ala.ui.model.Stage;
import org.uberfire.client.mvp.UberElement;

@Dependent
public class StepPresenter {

    public interface View extends UberElement<StepPresenter> {

        void setMessage( final String message );

        void setDoneState( );

        void setExecutingState( );
    }

    private final View view;
    private Stage stage;

    @Inject
    public StepPresenter( final View view ) {
        this.view = view;
    }

    public View getView() {
        return view;
    }

    public void setup( final Stage stage) {
        this.stage = stage;
        setMessage(stage.getName() );
        setState( State.EXECUTING );
    }

    public void setMessage( final String message ) {
        view.setMessage( message );
    }

    public void setState( final State state ) {
        if ( state.equals( State.EXECUTING ) ) {
            view.setExecutingState();
        } else {
            view.setDoneState();
        }
    }

}
