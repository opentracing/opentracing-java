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
package io.opentracing.examples.nested_callbacks;

import io.opentracing.ActiveSpan;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.util.ThreadLocalActiveSpanSource;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static io.opentracing.examples.TestUtils.reportedSpansSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestNestedCallbacks {

    private final MockTracer tracer = new MockTracer(new ThreadLocalActiveSpanSource(),
            Propagator.TEXT_MAP);
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Test
    public void test() throws Exception {

        try (ActiveSpan span = tracer.buildSpan("one").startActive()) {
            submitCallbacks(span);
        }

        await().atMost(15, TimeUnit.SECONDS).until(reportedSpansSize(tracer), equalTo(1));

        List<MockSpan> spans = tracer.finishedSpans();
        assertEquals(1, spans.size());
        assertEquals("one", spans.get(0).operationName());

        Map<String, Object> tags = spans.get(0).tags();
        assertEquals(3, tags.size());
        for (int i = 1; i <= 3; i++) {
            assertEquals(Integer.toString(i), tags.get("key" + i));
        }

        assertNull(tracer.activeSpan());
    }

    private void submitCallbacks(ActiveSpan span) {
        final ActiveSpan.Continuation cont = span.capture();

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try (ActiveSpan span = cont.activate()) {
                    span.setTag("key1", "1");
                    final ActiveSpan.Continuation cont = span.capture();

                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try (ActiveSpan span = cont.activate()) {
                                span.setTag("key2", "2");
                                final ActiveSpan.Continuation cont = span.capture();

                                executor.submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        try (ActiveSpan span = cont.activate()) {
                                            span.setTag("key3", "3");
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }
}
