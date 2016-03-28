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
import com.github.kristofa.brave.ClientTracer;
import com.github.kristofa.brave.ServerTracer;
import com.github.kristofa.brave.SpanId;
import io.opentracing.Span;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

final class BraveSpanImpl extends AbstractSpan {

    final SpanId spanId;
    final Optional<ServerTracer> serverTracer;
    private final Brave brave;
    private Optional<ClientTracer> clientTracer = Optional.empty();

    public BraveSpanImpl(
            Brave brave,
            String operationName,
            Optional<ServerTracer> serverTracer) {

        super(operationName);
        this.brave = brave;
        this.serverTracer = serverTracer;
        this.spanId = brave.localTracer().startNewSpan("???", operationName);
    }

    public BraveSpanImpl(
            Brave brave,
            String operationName,
            Optional<Span> parent,
            Instant start,
            Optional<ServerTracer> serverTracer) {

        super(operationName, parent, start);
        this.brave = brave;
        this.serverTracer = serverTracer;

        this.spanId = brave.localTracer().startNewSpan(
                "???",
                operationName,
                TimeUnit.SECONDS.toMicros(start.getEpochSecond()) + TimeUnit.NANOSECONDS.toMicros(start.getNano()));
    }

    @Override
    public void finish() {
        super.finish();
        if (clientTracer.isPresent()) {
            clientTracer.get().setClientReceived();
        }
        if (serverTracer.isPresent()) {
            serverTracer.get().setServerSend();
        }
    }

    void setClientTracer(ClientTracer clientTracer) {
        this.clientTracer = Optional.of(clientTracer);
    }

}
