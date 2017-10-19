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
package io.opentracing.examples.slf4j_mdc_debug_id;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.ThreadLocalScopeManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class SimpleMdcScopeManagerTest {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private MockTracer tracer;

    @Before
    public void setup() {
        ScopeManager scopeManager = new SimpleMdcScopeManager("debug_id",
                new MockSpanDebugId(), new ThreadLocalScopeManager());
        tracer = new MockTracer(scopeManager, MockTracer.Propagator.TEXT_MAP);
    }

    @Before
    @After
    public void clear() {
        MDC.clear();
    }

    @Test
    public void puts_debug_id_when_scope_activates() {
        Scope scope = tracer.buildSpan("op").startActive();

        assertThat(MDC.get("debug_id"), equalTo(getDebugId(scope.span())));
    }

    @Test
    public void removes_debug_id_when_scope_closes() {
        try (Scope ignore = tracer.buildSpan("op").startActive()) {
            assertThat(MDC.get("debug_id"), notNullValue());
        }
        assertThat(MDC.get("debug_id"), equalTo(null));
    }

    @Test
    public void tracks_debug_id_of_innermost_scope() {
        try (Scope outer = tracer.buildSpan("outer").startActive()) {
            String outerId = getDebugId(outer.span());
            assertThat(MDC.get("debug_id"), equalTo(outerId));

            try (Scope middle = tracer.buildSpan("middle").startActive()) {
                String middleId = getDebugId(middle.span());
                assertThat(MDC.get("debug_id"), equalTo(middleId));

                try (Scope inner = tracer.buildSpan("inner").startActive()) {
                    String innerId = getDebugId(inner.span());
                    assertThat(MDC.get("debug_id"), equalTo(innerId));
                }

                assertThat(MDC.get("debug_id"), equalTo(middleId));
            }

            assertThat(MDC.get("debug_id"), equalTo(outerId));
        }

        assertThat(MDC.get("debug_id"), equalTo(null));
    }

    private static String getDebugId(Span span) {
        MockSpan.MockContext ctx = ((MockSpan) span).context();
        return ctx.traceId() + ":" + ctx.spanId();
    }

    private static class MockSpanDebugId implements SimpleMdcScopeManager.DebugIdProvider {
        @Override
        public String get(Span span) {
            MockSpan.MockContext ctx = ((MockSpan) span).context();
            return ctx.traceId() + ":" + ctx.spanId();
        }
    }
}
