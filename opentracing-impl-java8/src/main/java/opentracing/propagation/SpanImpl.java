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
package opentracing.propagation;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.opentracing.Span;

final class SpanImpl implements Span {

    private final String operationName;
    private final Optional<Span> parent;
    private final Instant start;
    private Duration duration;
    final Map<String,String> traceState = new HashMap<>();
    final Map<String,String> traceAttributes = new HashMap<>();
    private final Map<String,Object> tags = new HashMap<>();
    private final List<LogData> logs = new ArrayList<>();

    public SpanImpl(String operationName, Optional<Span> parent) {
        this.operationName = operationName;
        this.parent = parent;
        this.start = Instant.now();
    }

    SpanImpl(String operationName, Map<String,String> traceState) {
        this(operationName, Optional.<Span>empty());
        this.traceState.putAll(traceState);
    }

    @Override
    public void finish() {
        assert null == duration;
        duration = Duration.between(start, Instant.now());
    }

    @Override
    public Span setTag(String key, String value) {
        tags.put(key, value);
        return this;
    }

    @Override
    public Span setTag(String key, boolean value) {
        tags.put(key, value);
        return this;
    }

    @Override
    public Span setTag(String key, Number value) {
        tags.put(key, value);
        return this;
    }

    @Override
    public Span setBaggageItem(String key, String value) {
        traceAttributes.put(key, value);
        return this;
    }

    @Override
    public String getBaggageItem(String key) {
        return traceAttributes.get(key);
    }

    @Override
    public Span log(String message, /* @Nullable */ Object payload) {
        Instant now = Instant.now();

        return log(
                TimeUnit.SECONDS.toMicros(now.getEpochSecond()) + TimeUnit.NANOSECONDS.toMicros(now.getNano()),
                message,
                payload);
    }

    @Override
    public Span log(long instantMicroseconds, String message, /* @Nullable */ Object payload) {
        logs.add(new LogData(start, message, payload));
        return this;
    }

    protected Span setTraceState(String key, String value) {
        traceState.put(key, value);
        return this;
    }

    final class LogData {
            private final Instant time;
            private final String message;
            private final Object paylod;

            LogData(Instant time, String message, Object paylod) {
                this.time = time;
                this.message = message;
                this.paylod = paylod;
            }
        }
}
