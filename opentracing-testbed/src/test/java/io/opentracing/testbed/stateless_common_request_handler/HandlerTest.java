/*
 * Copyright 2016-2019 The OpenTracing Authors
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
package io.opentracing.testbed.stateless_common_request_handler;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.util.ThreadLocalScopeManager;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * There is only one instance of 'RequestHandler' per 'Client'. Methods of 'RequestHandler' are
 * executed in the same thread (beforeRequest() and its resulting afterRequest(), that is).
 */
public class HandlerTest {

    private final MockTracer tracer = new MockTracer(new ThreadLocalScopeManager(),
            Propagator.TEXT_MAP);
    private final Client client = new Client(new RequestHandler(tracer));

    @Before
    public void before() {
        tracer.reset();
    }

    @Test
    public void test_requests() throws Exception {
        Future<String> responseFuture = client.send("message");
        Future<String> responseFuture2 = client.send("message2");
        Future<String> responseFuture3 = client.send("message3");

        assertEquals("message3:response", responseFuture3.get(5, TimeUnit.SECONDS));
        assertEquals("message2:response", responseFuture2.get(5, TimeUnit.SECONDS));
        assertEquals("message:response", responseFuture.get(5, TimeUnit.SECONDS));

        List<MockSpan> finished = tracer.finishedSpans();
        assertEquals(3, finished.size());
    }
}