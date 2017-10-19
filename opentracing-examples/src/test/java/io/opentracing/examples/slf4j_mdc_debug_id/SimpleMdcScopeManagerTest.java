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
