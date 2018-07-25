/*
 * Copyright 2016-2018 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.testbed.listener_per_request;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.tag.Tags;
import io.opentracing.util.ThreadLocalScopeManager;
import org.junit.Test;

import java.util.List;

import static io.opentracing.testbed.TestUtils.getOneByTag;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Each request has own instance of ResponseListener
 */
public class ListenerTest {

    private final MockTracer tracer = new MockTracer(new ThreadLocalScopeManager(),
            Propagator.TEXT_MAP);

    @Test
    public void test() throws Exception {
        Client client = new Client(tracer);
        Object response = client.send("message").get();
        assertEquals("message:response", response);

        List<MockSpan> finished = tracer.finishedSpans();
        assertEquals(1, finished.size());
        assertNotNull(getOneByTag(finished, Tags.SPAN_KIND, Tags.SPAN_KIND_CLIENT));
        assertNull(tracer.scopeManager().active());
    }
}
