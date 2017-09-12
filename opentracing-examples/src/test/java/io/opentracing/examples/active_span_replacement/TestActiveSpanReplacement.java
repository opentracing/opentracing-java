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
package io.opentracing.examples.active_span_replacement;

import io.opentracing.ActiveSpan;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.util.ThreadLocalActiveSpanSource;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.opentracing.examples.TestUtils.finishedSpansSize;
import static io.opentracing.examples.TestUtils.sleep;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

public class TestActiveSpanReplacement {

    private final MockTracer tracer = new MockTracer(new ThreadLocalActiveSpanSource(),
            Propagator.TEXT_MAP);

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Test
    public void test() throws Exception {
    /* Start an isolated task and query for its result in another task/thread */
        try (ActiveSpan span = tracer.buildSpan("initial").startActive()) {
            submitAnotherTask(span);
        }

        await().atMost(15, TimeUnit.SECONDS).until(finishedSpansSize(tracer), equalTo(3));

        List<MockSpan> spans = tracer.finishedSpans();
        assertEquals(3, spans.size());
        assertEquals("initial", spans.get(0).operationName()); /* Isolated task. */
        assertEquals("subtask", spans.get(1).operationName());
        assertEquals("task", spans.get(2).operationName());

    /* task/subtask are part of the same trace,
     * and subtask is a child of task */
        assertEquals(spans.get(1).context().traceId(), spans.get(2).context().traceId());
        assertEquals(spans.get(2).context().spanId(), spans.get(1).parentId());

    /* initial task is not related in any way to those two tasks */
        assertNotEquals(spans.get(0).context().traceId(), spans.get(1).context().traceId());
        assertEquals(0, spans.get(0).parentId());

        assertNull(tracer.activeSpan());
    }

    private void submitAnotherTask(ActiveSpan span) {
        final ActiveSpan.Continuation cont = span.capture();

        executor.submit(new Runnable() {
            @Override
            public void run() {
        /* Create a new Span for this task */
                try (ActiveSpan taskSpan = tracer.buildSpan("task").startActive()) {

          /* Simulate work strictly related to the initial Span. */
                    try (ActiveSpan initialSpan = cont.activate()) {
                        sleep(50);
                    }

          /* Restore the span for this task and create a subspan */
                    try (ActiveSpan subTask = tracer.buildSpan("subtask").startActive()) {
                    }
                }
            }
        });
    }
}
