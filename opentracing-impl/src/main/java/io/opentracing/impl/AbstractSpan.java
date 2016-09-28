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
package io.opentracing.impl;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

abstract class AbstractSpan implements Span, SpanContext {

    private String operationName;

    private final Map<String,String> baggage = new HashMap<>();

    private final Instant start;
    private Duration duration;
    private final Map<String,Object> tags = new HashMap<>();
    private final List<LogData> logs = new ArrayList<>();

    AbstractSpan(String operationName ) {
        this(operationName, Instant.now());
    }

    AbstractSpan(String operationName, Instant start) {
        this.operationName = operationName;
        this.start = start;
    }

    @Override
    public final SpanContext context() {
        return this;
    }

    @Override
    public void finish() {
        assert null == duration;
        duration = Duration.between(start, Instant.now());
    }

    @Override
    public void finish(long finishMicros) {
        long finishEpochSeconds = TimeUnit.MICROSECONDS.toSeconds(finishMicros);
        long nanos = TimeUnit.MICROSECONDS.toNanos(finishMicros) - TimeUnit.SECONDS.toNanos(finishEpochSeconds);
        assert null == duration;
        duration = Duration.between(start, Instant.ofEpochSecond(finishEpochSeconds, nanos));
    }

    public final String getOperationName() {
    	return operationName;
    }

    @Override
    public Span setOperationName(String operationName) {
        this.operationName = operationName;
        return this;
    }

    public final Instant getStart() {
    	return start;
    }

    public final Duration getDuration() {
    	return duration;
    }

    @Override
    public final void close() {
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

    public final Map<String,Object> getTags() {
        return Collections.unmodifiableMap(tags);
    }

    @Override
    public AbstractSpan setBaggageItem(String key, String value) {
        baggage.put(key, value);
        return this;
    }

    @Override
    public String getBaggageItem(String key) {
        return baggage.get(key);
    }

    @Override
    public final Iterable<Map.Entry<String,String>> baggageItems() {
        return baggage.entrySet();
    }

    public final Map<String,String> getBaggage() {
    	return Collections.unmodifiableMap(baggage);
    }

    @Override
    public final Span log(String event) {
        return log(nowMicros(), event);
    }

    @Override
    public final Span log(long timestampMicros, String event) {
        return log(timestampMicros, Collections.singletonMap("event", event));
    }

    @Override
    public final Span log(Map<String, ?> fields) {
        return log(nowMicros(), fields);
    }

    @Override
    public final Span log(long timestampMicros, Map<String, ?> fields) {
        Instant timestamp = Instant.ofEpochSecond(timestampMicros / 1000000, (timestampMicros % 1000000) * 1000);
        logs.add(new LogData(timestamp, fields));
        return this;
    }

    @Override
    public final Span log(String event, /* @Nullable */ Object payload) {
        Instant now = Instant.now();

        return log(
                TimeUnit.SECONDS.toMicros(now.getEpochSecond()) + TimeUnit.NANOSECONDS.toMicros(now.getNano()),
                event,
                payload);
    }

    @Override
    public final Span log(long timestampMicros, String event, /* @Nullable */ Object payload) {
        Instant timestamp = Instant.ofEpochSecond(timestampMicros / 1000000, (timestampMicros % 1000000) * 1000);
        Map<String, Object> fields = new HashMap<>();
        fields.put("event", event);
        if (payload != null) {
            fields.put("payload", payload);
        }
        logs.add(new LogData(timestamp, fields));
        return this;
    }

    public final List<LogData> getLogs() {
        return Collections.unmodifiableList(logs);
    }

    final class LogData {
        private final Instant time;
        private final Map<String, ?> fields;

        LogData(Instant time, Map<String, ?> fields) {
            this.time = time;
            this.fields = fields;
        }
    }

    static long nowMicros() {
        Instant now = Instant.now();
        return (now.getEpochSecond() * 1000000) + (now.getNano() / 1000);
    }
}
