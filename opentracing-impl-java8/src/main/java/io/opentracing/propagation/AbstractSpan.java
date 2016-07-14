/**
 * Copyright 2016 The OpenTracing Authors
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
package io.opentracing.propagation;

import io.opentracing.SpanContext;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.opentracing.Span;

abstract class AbstractSpan implements Span {

    final Map<String,String> baggage = new HashMap<>();

    final String operationName;
    private final Instant start;
    private Duration duration;
    private final SpanContext spanContext;
    private final Map<String,Object> tags = new HashMap<>();
    private final List<LogData> logs = new ArrayList<>();

    AbstractSpan(String operationName, SpanContext spanContext) {
        this(operationName, spanContext, Instant.now());
    }

    AbstractSpan(String operationName, SpanContext spanContext, Instant start) {
        this.operationName = operationName;
        this.start = start;
        this.spanContext = spanContext;
    }

    @Override
    public SpanContext context() { return this.spanContext; }

    @Override
    public void finish() {
        assert null == duration;
        duration = Duration.between(start, Instant.now());
    }

    @Override
    public void close() {
        finish();
    }

    @Override
    public final Span setTag(String key, String value) {
        tags.put(key, value);
        return this;
    }

    @Override
    public final Span setTag(String key, boolean value) {
        tags.put(key, value);
        return this;
    }

    @Override
    public final Span setTag(String key, Number value) {
        tags.put(key, value);
        return this;
    }

    @Override
    public final Span log(String message, /* @Nullable */ Object payload) {
        Instant now = Instant.now();

        return log(
                TimeUnit.SECONDS.toMicros(now.getEpochSecond()) + TimeUnit.NANOSECONDS.toMicros(now.getNano()),
                message,
                payload);
    }

    @Override
    public final Span log(long instantMicroseconds, String message, /* @Nullable */ Object payload) {
        logs.add(new LogData(start, message, payload));
        return this;
    }

    final class LogData {
            private final Instant time;
            private final String message;
            private final Object payload;

            LogData(Instant time, String message, Object payload) {
                this.time = time;
                this.message = message;
                this.payload = payload;
            }
        }
}
