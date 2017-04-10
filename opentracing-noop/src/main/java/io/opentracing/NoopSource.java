/**
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
        public Continuation capture() {
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
