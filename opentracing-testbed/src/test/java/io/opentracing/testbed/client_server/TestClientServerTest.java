/*
 * Copyright 2016-2020 The OpenTracing Authors
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
package io.opentracing.testbed.client_server;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.tag.Tags;
import io.opentracing.util.ThreadLocalScopeManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static io.opentracing.testbed.TestUtils.finishedSpansSize;
import static io.opentracing.testbed.TestUtils.getOneByTag;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestClientServerTest {

    private final MockTracer tracer = new MockTracer(new ThreadLocalScopeManager(),
            Propagator.TEXT_MAP);
    private final ArrayBlockingQueue<Message> queue = new ArrayBlockingQueue<>(10);
    private Server server;

    @Before
    public void before() {
        server = new Server(queue, tracer);
        server.start();
    }

    @After
    public void after() throws InterruptedException {
        server.interrupt();
        server.join(5_000L);
    }

    @Test
    public void test() throws Exception {
        Client client = new Client(queue, tracer);
        client.send();

        await().atMost(15, TimeUnit.SECONDS).until(finishedSpansSize(tracer), equalTo(2));

        List<MockSpan> finished = tracer.finishedSpans();
        assertEquals(2, finished.size());
        assertEquals(finished.get(0).context().traceId(), finished.get(1).context().traceId());
        assertNotNull(getOneByTag(finished, Tags.SPAN_KIND, Tags.SPAN_KIND_CLIENT));
        assertNotNull(getOneByTag(finished, Tags.SPAN_KIND, Tags.SPAN_KIND_SERVER));
        assertNull(tracer.scopeManager().activeSpan());
    }
}
