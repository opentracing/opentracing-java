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

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ServerTracer;
import java.util.Optional;

final class BraveSpanBuilderImpl extends AbstractSpanBuilder {

    private final Brave brave;
    long traceId;
    long spanId;

    ServerTracer serverTracer = null;

    BraveSpanBuilderImpl(Brave brave) {
        this.brave = brave;
    }

    @Override
    protected BraveSpanImpl createSpan() {
        BraveSpanImpl span = new BraveSpanImpl(brave, operationName, Optional.ofNullable(serverTracer));
        return span;
    }

    @Override
    boolean isTraceState(String key, Object value) {
        if ("traceId".equals(key)) {
            if (value instanceof Number || (value instanceof String && null != Long.getLong((String)value))) {
                return true;
            } else {
                throw new AssertionError("traceId needs to be a long");
            }
        }
        if ("spanId".equals(key)) {
            if (value instanceof Number || (value instanceof String && null != Long.getLong((String)value))) {
                return true;
            } else {
                throw new AssertionError("spanId needs to be a long");
            }
        }
        return false;
    }

    @Override
    boolean isBaggage(String key, Object value) {
        return !isTraceState(key, value);
    }

    void withServerTracer(ServerTracer serverTracer) {
        this.serverTracer = serverTracer;
    }

}
