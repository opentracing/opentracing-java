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
package io.opentracing.examples.fire_forget;

import io.opentracing.Span;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.tag.Tags;
import io.opentracing.util.ThreadLocalScopeManager;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.opentracing.examples.TestUtils.finishedSpansSize;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public final class FireAndForgetTest
{
    private final MockTracer tracer = new MockTracer(new ThreadLocalScopeManager(),
            Propagator.TEXT_MAP);
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Test
    public void linearTest() throws Exception {
        ActorSystem system = new ActorSystem(executor);

        Actor finishActor = system.actorOf(new Actors.FinishActor());
        Actor actor2 = system.actorOf(new Actors.WorkerActor(finishActor));
        Actor actor1 = system.actorOf(new Actors.WorkerActor(actor2));

        Map<String, Object> map = new TreeMap<String, Object>();
        map.put("span", tracer.buildSpan("one").startManual());
        map.put("working.value", 7);
        actor1.tell(map);

        await().atMost(5, TimeUnit.SECONDS).until(finishedSpansSize(tracer), equalTo(1));

        List<MockSpan> spans = tracer.finishedSpans();
        assertEquals(1, spans.size());
        assertEquals("one", spans.get(0).operationName());

        Map<String, Object> tags = spans.get(0).tags();
        assertEquals(1, tags.size());
        assertEquals(28, tags.get("return.value"));
    }

    @Test
    public void unexpectedErrorTest() throws Exception {
        final ActorSystem system = new ActorSystem(executor);

        Actor finishActor = system.actorOf(new Actors.FinishActor());
        Actor actor3 = system.actorOf(new Actors.WorkerActor(finishActor));
        Actor actor2 = system.actorOf(new Actors.WorkerActor(actor3) {
            @Override
            public void receive(Object message) {
                // Simulate an unexpected error
                Map<String, Object> nextMessage = new TreeMap<String, Object>();
                nextMessage.put("error.code", 1999);
                nextMessage.put("span", ((Map<String, Object>)message).get("span"));
                system.actorOf(new Actors.ErrorActor()).tell(nextMessage);
            }
        });
        Actor actor1 = system.actorOf(new Actors.WorkerActor(actor2));

        Map<String, Object> map = new TreeMap<String, Object>();
        map.put("span", tracer.buildSpan("one").startManual());
        map.put("working.value", 7);
        actor1.tell(map);

        await().atMost(10, TimeUnit.SECONDS).until(finishedSpansSize(tracer), equalTo(1));

        List<MockSpan> spans = tracer.finishedSpans();
        assertEquals(1, spans.size());
        assertEquals("one", spans.get(0).operationName());

        Map<String, Object> tags = spans.get(0).tags();
        assertEquals(2, tags.size());
        assertEquals(Boolean.TRUE, tags.get(Tags.ERROR.getKey()));
        assertEquals(1999, tags.get("return.value"));
    }
}
