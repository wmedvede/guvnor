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

package org.guvnor.ala.pipeline;

/**
 * Contract for defining a component that has the ability of doing some processing upon a pipeline input,
 * e.g. for setting by default values, and returns the input.
 * @see SystemPipelineDescriptor
 */
public interface InputProcessor {

    /**
     * @param input a pipeline input for processing.
     * @return the processed pipeline input.
     */
    Input processInput(Input input);

}
