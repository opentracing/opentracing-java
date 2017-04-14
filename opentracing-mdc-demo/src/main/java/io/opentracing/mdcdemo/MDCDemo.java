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

package io.opentracing.mdcdemo;

import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MDCDemo {
    Tracer tracer;

    private MDCDemo(Tracer tracer) {
        this.tracer = tracer;
    }

    public void trivialSpan() {
        Span span = this.tracer.buildSpan("trivial").start();
        span.finish();
    }

    public void trivialChild() throws Exception {
        try (ActiveSpan c = this.tracer.buildSpan("trivialParent").startActive()) {
            // The child will automatically know about the parent.
            Span child = this.tracer.buildSpan("trivialChild").start();
            child.finish();
        }
    }

    public void asyncSpans() throws Exception {
        final Tracer tracer = this.tracer; // save typing

        // Create an ExecutorService and makeActive it in a TracedExecutorService.
        ExecutorService realExecutor = Executors.newFixedThreadPool(500);
        final ExecutorService otExecutor = new TracedExecutorService(realExecutor, tracer);

        // Hacky lists of futures we wait for before exiting async Spans.
        final List<Future<?>> futures = new ArrayList<>();
        final List<Future<?>> subfutures = new ArrayList<>();

        // Create a parent Continuation for all of the async activity.
        try (final ActiveSpan parent = tracer.buildSpan("parent").startActive();) {

            // Create 10 async children.
            for (int i = 0; i < 10; i++) {
                final int j = i;
                MDC.put("parent number", new Integer(i).toString());
                futures.add(otExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        // START child body

                        try (final ActiveSpan child =
                                     tracer.buildSpan("child_" + j).startActive();) {
                            Thread.currentThread().sleep(1000);
                            tracer.activeSpan().log("awoke");
                            Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    Span active = tracer.activeSpan();
                                    active.log("awoke again");
                                    System.out.println("MDC parent number: " + MDC.get("parent number"));
                                    // Create a grandchild for each child... note that we don't *need* to use the
                                    // Continuation mechanism.
                                    Span grandchild = tracer.buildSpan("grandchild_" + j).start();
                                    grandchild.finish();
                                }
                            };
                            subfutures.add(otExecutor.submit(r));
                        } catch (Exception e) { }

                        // END child body
                    }
                }));
            }

            // Hacky cleanup... grossness has nothing to do with OT :)
            for (Future<?> f : futures) {
                f.get();
            }
            for (Future<?> f : subfutures) {
                f.get();
            }
        }

        otExecutor.shutdown();
        otExecutor.awaitTermination(3, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
        final Logger logger = org.slf4j.LoggerFactory.getLogger("MDCDemo");
        MDC.put("mdcKey", "mdcVal");

        final MockTracer tracer = new MockTracer(new MDCActiveSpanSource());

        // Do stuff with the MockTracer.
        {
            MDCDemo demo = new MDCDemo(tracer);
            demo.trivialSpan();
            demo.trivialChild();
            demo.asyncSpans();
        }

        // Print out all mock-Spans
        List<MockSpan> finishedSpans = tracer.finishedSpans();
        for (MockSpan span : finishedSpans) {
            logger.info("finished Span '{}'. trace={}, span={}, parent={}", span.operationName(), span.context().traceId(), span.context().spanId(), span.parentId());
        }

    }
}
