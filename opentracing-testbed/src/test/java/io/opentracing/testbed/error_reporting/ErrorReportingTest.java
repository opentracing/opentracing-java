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
package io.opentracing.testbed.error_reporting;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tags;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

import static io.opentracing.testbed.TestUtils.finishedSpansSize;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;


public class ErrorReportingTest {

    private final MockTracer tracer = new MockTracer();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /* Very simple error handling **/
    @Test
    public void testSimpleError() {
        Scope scope = tracer.buildSpan("one").startActive();
        try {
            throw new RuntimeException("Invalid state");
        } catch (Exception e) {
            Tags.ERROR.set(scope.span(), true);
        } finally {
            scope.close();
            scope.span().finish();
        }

        assertNull(tracer.scopeManager().active());

        List<MockSpan> spans = tracer.finishedSpans();
        assertEquals(spans.size(), 1);
        assertEquals(spans.get(0).tags().get(Tags.ERROR.getKey()), true);
    }

    /* Error handling in a callback capturing/activating the Span */
    @Test
    public void testCallbackError() {
        Span span = tracer.buildSpan("one").start();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try (Scope scope = tracer.scopeManager().activate(span)) {
                    throw new RuntimeException("Invalid state");
                } catch (Exception exc) {
                    Tags.ERROR.set(span, true);
                } finally {
                    span.finish();
                }
            }
        });

        await().atMost(5, TimeUnit.SECONDS).until(finishedSpansSize(tracer), equalTo(1));

        List<MockSpan> spans = tracer.finishedSpans();
        assertEquals(spans.size(), 1);
        assertEquals(spans.get(0).tags().get(Tags.ERROR.getKey()), true);
    }

    /* Error handling for a max-retries task (such as url fetching).
     * We log the Exception at each retry. */
    @Test
    public void testErrorRecovery() {
        final int maxRetries = 1;
        int retries = 0;
        Object res = null;

        Scope scope = tracer.buildSpan("one").startActive();
        while (res == null && retries++ < maxRetries) {
            try {
                throw new RuntimeException("No url could be fetched");
            } catch (Exception exc) {
                scope.span().log(new TreeMap<String, Object>() {{
                    put(Fields.EVENT, Tags.ERROR);
                    put(Fields.ERROR_OBJECT, exc);
                }});
            }
        }

        if (res == null) {
            Tags.ERROR.set(scope.span(), true); // Could not fetch anything.
        }
        scope.close();
        scope.span().finish();

        assertNull(tracer.scopeManager().active());

        List<MockSpan> spans = tracer.finishedSpans();
        assertEquals(spans.size(), 1);
        assertEquals(spans.get(0).tags().get(Tags.ERROR.getKey()), true);

        List<MockSpan.LogEntry> logs = spans.get(0).logEntries();
        assertEquals(logs.size(), maxRetries);
        assertEquals(logs.get(0).fields().get(Fields.EVENT), Tags.ERROR);
        assertNotNull(logs.get(0).fields().get(Fields.ERROR_OBJECT));
    }

    /* Error handling for a mocked layer automatically capturing/activating
     * the Span for a submitted Runnable. */
    @Test
    public void testInstrumentationLayer() {
        try (Scope scope = tracer.buildSpan("one").startActive()) {

            // ScopedRunnable captures the active Span at this time.
            executor.submit(new ScopedRunnable(new Runnable() {
                @Override
                public void run() {
                    try {
                        throw new RuntimeException("Invalid state");
                    } catch (Exception exc) {
                        Tags.ERROR.set(tracer.activeSpan(), true);
                    } finally {
                        tracer.activeSpan().finish();
                    }
                }
            }, tracer));
        }

        await().atMost(5, TimeUnit.SECONDS).until(finishedSpansSize(tracer), equalTo(1));

        List<MockSpan> spans = tracer.finishedSpans();
        assertEquals(spans.size(), 1);
        assertEquals(spans.get(0).tags().get(Tags.ERROR.getKey()), true);
    }

    static class ScopedRunnable implements Runnable {
        Runnable runnable;
        Tracer tracer;
        Span span;

        public ScopedRunnable(Runnable runnable, Tracer tracer) {
            this.runnable = runnable;
            this.tracer = tracer;
            this.span = tracer.activeSpan();
        }

        public void run() {
            // No error reporting is done, as we are a simple wrapper.
            try (Scope scope = tracer.scopeManager().activate(span)) {
                runnable.run();
            }
        }
    }
}
