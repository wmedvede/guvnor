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

package org.guvnor.ala.pipeline.execution;

/**
 * Base class for exceptions related to the pipeline execution.
 * The cause of the exception is automatically wrapped into an instance of MarshallableExceptionWrapper.
 * This wrapping facilitates the json marshalling/unmarshalling of arbitrary causes that might be produced by the
 * different providers implementations. Clients of this class that wants to consume the entire cause chain should use
 * the getCauseWrapper method.
 */
public class PipelineExecutorException
        extends Exception {

    private MarshallableExceptionWrapper causeWrapper;

    public PipelineExecutorException(final String message) {
        super(message);
    }

    public PipelineExecutorException(final String message,
                                     final Throwable cause) {
        super(message);
        causeWrapper = MarshallableExceptionWrapper.wrap(cause);
    }

    public MarshallableExceptionWrapper getCauseWrapper() {
        return causeWrapper;
    }

}
