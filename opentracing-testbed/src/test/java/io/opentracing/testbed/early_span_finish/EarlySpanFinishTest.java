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
package io.opentracing.testbed.early_span_finish;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.util.ThreadLocalScopeManager;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.opentracing.testbed.TestUtils.assertSameTrace;
import static io.opentracing.testbed.TestUtils.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class EarlySpanFinishTest {

    private final MockTracer tracer = new MockTracer(new ThreadLocalScopeManager(),
            Propagator.TEXT_MAP);
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Test
    public void test() throws Exception {
        // Create a Span manually and use it as parent of a pair of subtasks
        Span parentSpan = tracer.buildSpan("parent").start();
        submitTasks(parentSpan);

        // Early-finish the parent Span now
        parentSpan.finish();

        // Wait for the threadpool to be done first, instead of polling/waiting
        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);


        List<MockSpan> spans = tracer.finishedSpans();
        assertEquals(3, spans.size());
        assertEquals("parent", spans.get(0).operationName());
        assertEquals(1, spans.get(0).generatedErrors().size());
        assertEquals("task1", spans.get(1).operationName());
        assertEquals("task2", spans.get(2).operationName());
        assertSameTrace(spans);

        assertNull(tracer.scopeManager().active());
    }


    /**
     * Fire away a few subtasks, passing a parent Span whose lifetime
     * is not tied at-all to the children
     */
    private void submitTasks(final Span parentSpan) {

        executor.submit(new Runnable() {
            @Override
            public void run() {
                // Activating the parent span is illegal as we don't have control over its life cycle.
                // The caller of tracer.activeSpan() has no way of knowing whether the active span is already finished.
                try (Scope scope = tracer.scopeManager().activate(parentSpan)) {
                    Span childSpan = tracer.buildSpan("task1").start();
                    try (Scope childScope = tracer.scopeManager().activate(childSpan)) {
                        sleep(55);
                        childSpan.setTag("foo", "bar");
                    } finally {
                        childSpan.finish();
                    }
                    assertNotNull(tracer.activeSpan());
                    // this fails as the parent span is already finished
                    tracer.activeSpan().setTag("foo", "bar");
                }
            }
        });

        executor.submit(new Runnable() {
            @Override
            public void run() {
                // We don't own the lifecycle of the parent span,
                // therefore we must activate the span context
                // tracer.activeSpan() will then always return null to make the intention clear
                // that interacting with the parent span is not possible.
                // This puts the burden of being aware of the lifecycle to the person writing the instrumentation
                // (for example io.opentracing.contrib.concurrent.TracedRunnable)
                // instead of the end users, who only want to customize spans.
                try (Scope scope = tracer.scopeManager().activate(parentSpan.context())) {
                    Span childSpan = tracer.buildSpan("task2").start();
                    try (Scope childScope = tracer.scopeManager().activate(childSpan)) {
                        sleep(85);
                        childSpan.setTag("foo", "bar");
                    } finally {
                        childSpan.finish();
                    }
                    assertNull(tracer.activeSpan());
                    assertNotNull(tracer.activeSpanContext());
                }
            }
        });
    }

}

