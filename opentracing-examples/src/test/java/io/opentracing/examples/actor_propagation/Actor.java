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
package io.opentracing.examples.actor_propagation;

import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.mock.MockTracer;
import io.opentracing.tag.Tags;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;

/** @author tylerbenson */
public class Actor implements AutoCloseable {
  private final ExecutorService executor;
  private final MockTracer tracer;
  private final Phaser phaser;

  public Actor(MockTracer tracer, Phaser phaser) {
    // Passed along here for testing. Normally should be referenced via GlobalTracer.get().
    this.tracer = tracer;

    this.phaser = phaser;
    executor = Executors.newFixedThreadPool(2);
  }

  @Override
  public void close() {
    executor.shutdown();
  }

  public void tell(final String message) {
    final Span parent = tracer.scopeManager().active().span();
    phaser.register();
    executor.submit(
        new Runnable() {
          @Override
          public void run() {
            try (Scope child =
                tracer
                    .buildSpan("received")
                    .addReference(References.FOLLOWS_FROM, parent.context())
                    .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER)
                    .startActive()) {
              phaser.arriveAndAwaitAdvance(); // child tracer started
              child.span().log("received " + message);
              phaser.arriveAndAwaitAdvance(); // assert size
            }
            phaser.arriveAndAwaitAdvance(); // child tracer finished
            phaser.arriveAndAwaitAdvance(); // assert size
          }
        });
  }

  public Future<String> ask(final String message) {
    final Span parent = tracer.scopeManager().active().span();
    phaser.register();
    Future<String> future =
        executor.submit(
            new Callable<String>() {
              @Override
              public String call() throws Exception {
                try (Scope child =
                    tracer
                        .buildSpan("received")
                        .addReference(References.FOLLOWS_FROM, parent.context())
                        .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER)
                        .startActive()) {
                  phaser.arriveAndAwaitAdvance(); // child tracer started
                  phaser.arriveAndAwaitAdvance(); // assert size
                  return "received " + message;
                } finally {
                  phaser.arriveAndAwaitAdvance(); // child tracer finished
                  phaser.arriveAndAwaitAdvance(); // assert size
                }
              }
            });
    return future;
  }
}
