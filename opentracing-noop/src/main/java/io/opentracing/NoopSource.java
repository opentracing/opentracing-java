package io.opentracing;

import java.util.Map;

/**
 * A noop (i.e., cheap-as-possible) implementation of a Source.
 */
public class NoopSource implements ActiveSpanSource {
    public static final ActiveSpan NOOP_ACTIVE_SPAN = new NoopActiveSpan();
    public static final ActiveSpan.Continuation NOOP_CONTINUATION = new NoopContinuation();

    @Override
    public ActiveSpan adopt(Span span) {
        return NOOP_ACTIVE_SPAN;
    }

    @Override
    public ActiveSpan activeSpan() { return NOOP_ACTIVE_SPAN; }

    public static class NoopActiveSpan implements ActiveSpan {
        @Override
        public void deactivate() {}

        @Override
        public Continuation defer() {
            return NOOP_CONTINUATION;
        }

        @Override
        // XXX audit
        public SpanContext context() {
            return null;
        }

        @Override
        public void finish() {

        }

        @Override
        public void finish(long finishMicros) {

        }

        @Override
        public void close() {}

        @Override
        public Span setTag(String key, String value) {
            return null;
        }

        @Override
        public Span setTag(String key, boolean value) {
            return null;
        }

        @Override
        public Span setTag(String key, Number value) {
            return null;
        }

        @Override
        public Span log(Map<String, ?> fields) {
            return null;
        }

        @Override
        public Span log(long timestampMicroseconds, Map<String, ?> fields) {
            return null;
        }

        @Override
        public Span log(String event) {
            return null;
        }

        @Override
        public Span log(long timestampMicroseconds, String event) {
            return null;
        }

        @Override
        public Span setBaggageItem(String key, String value) {
            return null;
        }

        @Override
        public String getBaggageItem(String key) {
            return null;
        }

        @Override
        public Span setOperationName(String operationName) {
            return null;
        }

        @Override
        public Span log(String eventName, Object payload) {
            return null;
        }

        @Override
        public Span log(long timestampMicroseconds, String eventName, Object payload) {
            return null;
        }
    }

    public static class NoopContinuation implements ActiveSpan.Continuation {
        @Override
        public ActiveSpan activate() {
            return NOOP_ACTIVE_SPAN;
        }
    }
}
