/*
 * Copyright 2016-2018 The OpenTracing Authors
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
package io.opentracing.examples.client_server;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format.Builtin;
import io.opentracing.propagation.TextMapInjectAdapter;
import io.opentracing.tag.Tags;

import java.util.concurrent.ArrayBlockingQueue;

public class Client {

    private final ArrayBlockingQueue<Message> queue;
    private final Tracer tracer;

    public Client(ArrayBlockingQueue<Message> queue, Tracer tracer) {
        this.queue = queue;
        this.tracer = tracer;
    }

    public void send() throws InterruptedException {
        Message message = new Message();

        try (Scope scope = tracer.buildSpan("send")
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .withTag(Tags.COMPONENT.getKey(), "example-client")
                .startActive(true)) {
            tracer.inject(scope.span().context(), Builtin.TEXT_MAP, new TextMapInjectAdapter(message));
            queue.put(message);
        }
    }

}
