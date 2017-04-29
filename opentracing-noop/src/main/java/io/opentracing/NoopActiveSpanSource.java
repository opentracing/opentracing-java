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

public interface NoopActiveSpanSource extends ActiveSpanSource {
    NoopActiveSpanSource INSTANCE = new NoopActiveSpanSourceImpl();

    interface NoopActiveSpan extends ActiveSpan {
        NoopActiveSpanSource.NoopActiveSpan INSTANCE = new NoopActiveSpanSourceImpl.NoopActiveSpanImpl();
    }
    interface NoopContinuation extends ActiveSpan.Continuation {
        NoopActiveSpanSource.NoopContinuation INSTANCE = new NoopActiveSpanSourceImpl.NoopContinuationImpl();
    }
}

/**
 * A noop (i.e., cheap-as-possible) implementation of an ActiveSpanSource.
 */
class NoopActiveSpanSourceImpl implements NoopActiveSpanSource {
    @Override
    public ActiveSpan makeActive(Span span) {
        return NoopActiveSpanSource.NoopActiveSpan.INSTANCE;
    }

    @Override
    public ActiveSpan activeSpan() { return NoopActiveSpanSource.NoopActiveSpan.INSTANCE; }

    static class NoopActiveSpanImpl implements NoopActiveSpanSource.NoopActiveSpan {
        @Override
        public void deactivate() {}

        @Override
        public Continuation capture() {
            return NoopActiveSpanSource.NoopContinuation.INSTANCE;
        }

        @Override
        public SpanContext context() {
            return NoopSpanContextImpl.INSTANCE;
        }

        @Override
        public void close() {}

        @Override
        public BaseSpan setTag(String key, String value) {
            return NoopActiveSpanSource.NoopActiveSpan.INSTANCE;
        }

        @Override
        public BaseSpan setTag(String key, boolean value) {
            return NoopActiveSpanSource.NoopActiveSpan.INSTANCE;
        }

        @Override
        public BaseSpan setTag(String key, Number value) {
            return NoopActiveSpanSource.NoopActiveSpan.INSTANCE;
        }

        @Override
        public BaseSpan log(Map<String, ?> fields) {
            return NoopActiveSpanSource.NoopActiveSpan.INSTANCE;
        }

        @Override
        public BaseSpan log(long timestampMicroseconds, Map<String, ?> fields) {
            return NoopActiveSpanSource.NoopActiveSpan.INSTANCE;
        }

        @Override
        public BaseSpan log(String event) {
            return NoopActiveSpanSource.NoopActiveSpan.INSTANCE;
        }

        @Override
        public BaseSpan log(long timestampMicroseconds, String event) {
            return NoopActiveSpanSource.NoopActiveSpan.INSTANCE;
        }

        @Override
        public BaseSpan setBaggageItem(String key, String value) {
            return NoopActiveSpanSource.NoopActiveSpan.INSTANCE;
        }

        @Override
        public String getBaggageItem(String key) {
            return null;
        }

        @Override
        public BaseSpan setOperationName(String operationName) {
            return NoopActiveSpanSource.NoopActiveSpan.INSTANCE;
        }

        @Override
        public BaseSpan log(String eventName, Object payload) {
            return NoopActiveSpanSource.NoopActiveSpan.INSTANCE;
        }

        @Override
        public BaseSpan log(long timestampMicroseconds, String eventName, Object payload) {
            return NoopActiveSpanSource.NoopActiveSpan.INSTANCE;
        }
    }

    static class NoopContinuationImpl implements NoopActiveSpanSource.NoopContinuation {
        @Override
        public ActiveSpan activate() {
            return NoopActiveSpanSource.NoopActiveSpan.INSTANCE;
        }
    }
}
