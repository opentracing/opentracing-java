/*
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
package io.opentracing.examples.multiple_callbacks;

import io.opentracing.ActiveSpan;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static io.opentracing.examples.TestUtils.sleep;

public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Tracer tracer;

    public Client(Tracer tracer) {
        this.tracer = tracer;
    }

    public Future<Object> send(final Object message, ActiveSpan parentSpan, final long milliseconds) {
        final ActiveSpan.Continuation cont = parentSpan.capture();
        return executor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                logger.info("Child thread with message '{}' started", message);

                try (ActiveSpan parentSpan = cont.activate()) {
                    try (ActiveSpan span = tracer.buildSpan("subtask").startActive()) {
                        // Simulate work.
                        sleep(milliseconds);
                    }
                }

                logger.info("Child thread with message '{}' finished", message);
                return message + "::response";
            }
        });
    }
}
