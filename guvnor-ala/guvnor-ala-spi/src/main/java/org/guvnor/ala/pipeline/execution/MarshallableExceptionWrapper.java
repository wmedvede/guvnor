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
 * Wrapper class for facilitating the storing of arbitrary exceptions information produced by internal components
 * that should be marshalled/unmarshalled into json.
 */
public class MarshallableExceptionWrapper {

    private String wrappedClass;

    private String message;

    private StackTraceElement[] stackTrace;

    private MarshallableExceptionWrapper cause;

    private MarshallableExceptionWrapper() {
        //required for marshalling/unmarshalling
    }

    private MarshallableExceptionWrapper(String wrappedClass,
                                         String message,
                                         StackTraceElement[] stackTrace,
                                         MarshallableExceptionWrapper cause) {
        this.wrappedClass = wrappedClass;
        this.message = message;
        this.stackTrace = stackTrace;
        this.cause = cause;
    }

    private MarshallableExceptionWrapper(String wrappedClass,
                                         String message,
                                         StackTraceElement[] stackTrace) {
        this.wrappedClass = wrappedClass;
        this.message = message;
        this.stackTrace = stackTrace;
    }

    public String getWrappedClass() {
        return wrappedClass;
    }

    public String getMessage() {
        return message;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public MarshallableExceptionWrapper getCause() {
        return cause;
    }

    public static MarshallableExceptionWrapper wrap(Throwable throwable) {
        if (throwable == null) {
            return null;
        } else if (throwable instanceof PipelineExecutorException) {
            return new MarshallableExceptionWrapper(throwable.getClass().getName(),
                                                    throwable.getMessage(),
                                                    throwable.getStackTrace(),
                                                    ((PipelineExecutorException) throwable).getCauseWrapper());
        } else if (throwable.getCause() == null) {
            return new MarshallableExceptionWrapper(throwable.getClass().getName(),
                                                    throwable.getMessage(),
                                                    throwable.getStackTrace());
        } else {
            return new MarshallableExceptionWrapper(throwable.getClass().getName(),
                                                    throwable.getMessage(),
                                                    throwable.getStackTrace(),
                                                    wrap(throwable.getCause()));
        }
    }
}
