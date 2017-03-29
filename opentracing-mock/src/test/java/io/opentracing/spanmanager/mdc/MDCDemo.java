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
package io.opentracing.spanmanager.mdc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.MDC;

import io.opentracing.Span;
import io.opentracing.SpanManager;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.spanmanager.concurrent.TracedExecutorService;

/**
 * @author Pavol Loffay
 */
public class MDCDemo {
    static {
        org.apache.log4j.BasicConfigurator.configure();
    }
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(MDCDemo.class.getSimpleName());
    private static final MockTracer tracer = new MockTracer(MockTracer.Propagator.PRINTER, new MDCThreadLocalSpanManager());

    public void singleSpan() {
        tracer.buildSpan("single").start().finish();
    }

    public void parentWithChild() throws Exception {
        Span parent = tracer.buildSpan("parent").start();
        SpanManager.VisibilityContext parentVisibility = parent.visibility().capture().on();
        // The child will automatically know about the parent.
        tracer.buildSpan("child")
                .start()
                .finish();
        parentVisibility.off();
        parent.finish();
    }

    public void asyncSpans() throws Exception {
        final ExecutorService otExecutor = new TracedExecutorService(Executors.newFixedThreadPool(100), tracer.spanManager());

        // Hacky lists of futures we wait for before exiting async Spans.
        List<Future<?>> futures = new ArrayList<>();
        final List<Future<?>> subfutures = new ArrayList<>();

        // Create a parent Continuation for all of the async activity.
        Span parent = tracer.buildSpan("asyncParent").start();
        SpanManager.VisibilityContext parentVisibility = parent.visibility().capture().on();

        // Create 10 async children.
        for (int i = 0; i < 10; i++) {
            final int j = i;
            MDC.put("parent_number", String.valueOf(j));
            futures.add(otExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    final Span childSpan = tracer.buildSpan("child_" + j).start();
                    childSpan.visibility().capture().on();

                    try {
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    tracer.spanManager().active().visibility().span().log("awoke");
                    subfutures.add(otExecutor.submit(new Runnable() {
                        @Override
                        public void run() {
                            Span active = tracer.spanManager().active().visibility().span();
                            active.log("awoke again");
                            System.out.println(String.format("j=%d, MDC parent number: %s, MDC map: %s",
                                    j, MDC.get("parent_number"), MDC.getCopyOfContextMap()));
                            // Create a grandchild for each child... note that we don't *need* to use the
                            // Continuation mechanism.
                            Span grandchild = tracer.buildSpan("grandchild_" + j).start();
                            grandchild.finish();
                            childSpan.finish();
                        }
                    }));
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

        parent.finish();
        parentVisibility.off();

        otExecutor.shutdown();
        otExecutor.awaitTermination(3, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws Exception {
        MDC.put("method", "main");

        MDCDemo demo = new MDCDemo();
        demo.singleSpan();
        demo.parentWithChild();
        demo.asyncSpans();

        // Print out all mock-Spans
        List<MockSpan> finishedSpans = tracer.finishedSpans();
        for (MockSpan span : finishedSpans) {
            logger.info("finished Span '{}', span.log: {} ", span, logEntryToString(span.logEntries()));
        }
    }

    public static String logEntryToString(List<MockSpan.LogEntry> logEntries) {
        StringBuilder sb = new StringBuilder();
        for (MockSpan.LogEntry logEntry: logEntries) {
            sb.append(logEntry.fields());
        }
        return sb.length() == 0 ? "{}" : sb.toString();
    }
}

