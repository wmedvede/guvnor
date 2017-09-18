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
 * Wrapper class for facilitating the marshaling/unmarshalling for arbitrary exceptions produced by internal components
 * and that should be marshalled/unmarshalled into json.
 */
public class ExceptionWrapper {

    private String exceptionClass;

    private String message;

    private StackTraceElement[] stackTrace;

    private ExceptionWrapper cause;

    private ExceptionWrapper(String exceptionClass,
                            String message,
                            StackTraceElement[] stackTrace,
                            ExceptionWrapper cause) {
        this.exceptionClass = exceptionClass;
        this.message = message;
        this.stackTrace = stackTrace;
        this.cause = cause;
    }

    private ExceptionWrapper(String exceptionClass,
                            String message,
                            StackTraceElement[] stackTrace) {
        this.exceptionClass = exceptionClass;
        this.message = message;
        this.stackTrace = stackTrace;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public String getMessage() {
        return message;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public static ExceptionWrapper wrapException(Throwable throwable) {
        if (throwable == null) {
            return null;
        } else if (throwable.getCause() == null) {
            return new ExceptionWrapper(throwable.getClass().getName(),
                                        throwable.getMessage(),
                                        throwable.getStackTrace());
        } else {
            return new ExceptionWrapper(throwable.getClass().getName(),
                                        throwable.getMessage(),
                                        throwable.getStackTrace(),
                                        wrapException(throwable.getCause()));
        }
    }
}
