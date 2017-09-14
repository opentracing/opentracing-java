/*
 * Copyright 2016-2017 The OpenTracing Authors
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
package io.opentracing.examples.common_request_handler;

import io.opentracing.ActiveSpan;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.tag.Tags;
import io.opentracing.util.ThreadLocalActiveSpanSource;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.opentracing.examples.TestUtils.sortByStartMicros;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * There is only one instance of 'RequestHandler' per 'Client'. Methods of 'RequestHandler' are
 * executed concurrently in different threads which are reused (common pool). Therefore we cannot
 * use current active span and activate span. So one issue here is setting correct parent span.
 */
public class HandlerTest {

    private final MockTracer tracer = new MockTracer(new ThreadLocalActiveSpanSource(),
            Propagator.TEXT_MAP);
    private final Client client = new Client(new RequestHandler(tracer));

    @Before
    public void before() {
        tracer.reset();
    }

    @Test
    public void two_requests() throws Exception {
        Future<String> responseFuture = client.send("message");
        Future<String> responseFuture2 = client.send("message2");

        assertEquals("message:response", responseFuture.get(15, TimeUnit.SECONDS));
        assertEquals("message2:response", responseFuture2.get(15, TimeUnit.SECONDS));

        List<MockSpan> finished = tracer.finishedSpans();
        assertEquals(2, finished.size());

        for (MockSpan span : finished) {
            assertEquals(Tags.SPAN_KIND_CLIENT, span.tags().get(Tags.SPAN_KIND.getKey()));
        }

        assertNotEquals(finished.get(0).context().traceId(), finished.get(1).context().traceId());
        assertEquals(0, finished.get(0).parentId());
        assertEquals(0, finished.get(1).parentId());

        assertNull(tracer.activeSpan());
    }

    /**
     * active parent is not picked up by child
     */
    @Test
    public void parent_not_picked_up() throws Exception {
        try (ActiveSpan parent = tracer.buildSpan("parent").startActive()) {
            String response = client.send("no_parent").get(15, TimeUnit.SECONDS);
            assertEquals("no_parent:response", response);
        }

        List<MockSpan> finished = tracer.finishedSpans();
        assertEquals(2, finished.size());

        MockSpan child = getOneByOperationName(finished, RequestHandler.OPERATION_NAME);
        assertNotNull(child);

        MockSpan parent = getOneByOperationName(finished, "parent");
        assertNotNull(parent);

        // Here check that there is no parent-child relation although it should be because child is
        // created when parent is active
        assertNotEquals(parent.context().spanId(), child.parentId());
    }

    /**
     * Solution is bad because parent is per client (we don't have better choice).
     * Therefore all client requests will have the same parent.
     * But if client is long living and injected/reused in different places then initial parent will not be correct.
     */
    @Test
    public void bad_solution_to_set_parent() throws Exception {
        Client client;
        try (ActiveSpan parent = tracer.buildSpan("parent").startActive()) {
            client = new Client(new RequestHandler(tracer, parent.context()));
            String response = client.send("correct_parent").get(15, TimeUnit.SECONDS);
            assertEquals("correct_parent:response", response);
        }

        // Send second request, now there is no active parent, but it will be set, ups
        String response = client.send("wrong_parent").get(15, TimeUnit.SECONDS);
        assertEquals("wrong_parent:response", response);

        List<MockSpan> finished = tracer.finishedSpans();
        assertEquals(3, finished.size());

        sortByStartMicros(finished);

        MockSpan parent = getOneByOperationName(finished, "parent");
        assertNotNull(parent);

        // now there is parent/child relation between first and second span:
        assertEquals(parent.context().spanId(), finished.get(1).parentId());

        // third span should not have parent, but it has, damn it
        assertEquals(parent.context().spanId(), finished.get(2).parentId());
    }

    private static MockSpan getOneByOperationName(List<MockSpan> spans, String name) {
        MockSpan found = null;
        for (MockSpan span : spans) {
            if (name.equals(span.operationName())) {
                if (found != null) {
                    throw new IllegalArgumentException("there is more than one span with operation name '"
                            + name + "'");
                }
                found = span;
            }
        }
        return found;
    }
}
