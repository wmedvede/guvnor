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

package org.guvnor.ala.marshalling;

import java.nio.file.FileAlreadyExistsException;
import java.sql.SQLClientInfoException;
import java.util.ArrayList;
import java.util.List;

import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.guvnor.ala.pipeline.execution.PipelineExecutorException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestExceptionExtendedMarshallerTest
        extends BaseMarshallerTest<PipelineExecutorException> {

    @Override
    public Marshaller<PipelineExecutorException> createMarshaller() {
        return new TestExceptionExtendedMarshaller();
    }

    @Override
    public Class<PipelineExecutorException> getType() {
        return PipelineExecutorException.class;
    }

    @Override
    public PipelineExecutorException getValue() {

        /*
        return new TestExceptionExtended("The Error",
                                         new PipelineExecutorException("errores", new FileAlreadyExistsException("fileeee",
                                                                                                                 "otherrrr",
                                                                                                                 "reasoon")));

                                                                                                                 */

//        return new TestExceptionExtended("The Error",
//                                         new KubernetesClientException("errores", 666, new Status()));

        /*
        return new TestExceptionExtended("The Error",
                                         new FileAlreadyExistsException("fileValue", "otherValue", "reason"));
                                         */


//        ,  new FileAlreadyExistsException("fileeee",
//                                                                                                                 "otherrrr",
//                                                                                                                 "reasoon")));
    return null;

    }

    @Test
    public void testMarshallUnMarshall() throws Exception {
        List<PipelineExecutorException> exceptions = new ArrayList<>();
//        exceptions.add(new PipelineExecutorException("Error1"));
//        exceptions.add(new PipelineExecutorException("Error2", new KubernetesClientException("errores", 666, new Status())));
//        exceptions.add(new PipelineExecutorException("Error3", new FileAlreadyExistsException("fileValue", "otherValue", "reason")));
//        exceptions.add(new PipelineExecutorException("Error4", new Exception("level1", new FileAlreadyExistsException("fileValue", "otherValue", "reason"))));
//        exceptions.add(new PipelineExecutorException("Error5", new Exception("level1", new Exception("level2", new FileAlreadyExistsException("fileValue", "otherValue", "reason")))));
        exceptions.add(new PipelineExecutorException("Error5", new Exception("level1", new PipelineExecutorException("level2", new Exception("level3", new PipelineExecutorException("level4", new FileAlreadyExistsException("fileValue", "otherValue", "reason")))))));

        for( PipelineExecutorException e : exceptions ) {
            String value = marshaller.marshal(e);
            PipelineExecutorException obj = marshaller.unmarshal(value);
            String value2 = marshaller.marshal(e);
            assertEquals(value, value2);
            int i = 0;
        }

        int i = 89;
    }

    @Test
    @Override
    public void testUnMarshall() throws Exception {
        super.testUnMarshall();
    }
}
