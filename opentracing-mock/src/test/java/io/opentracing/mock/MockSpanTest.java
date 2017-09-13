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
package io.opentracing.mock;

import io.opentracing.Span;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Pavol Loffay
 */
public class MockSpanTest {

    @Test
    @SuppressWarnings("deprecated")
    public void testExplicitStartInMicroseconds() {
        MockTracer tracer = new MockTracer();
        long now = System.currentTimeMillis() * 1000;
        Span span = tracer.buildSpan("foo")
                .withStartTimestamp(now)
                .startManual();
        span.finish();

        Assert.assertEquals(now, tracer.finishedSpans().get(0).startTimestamp(TimeUnit.MICROSECONDS));
    }

    @Test
    public void testExplicitStart() {
        MockTracer tracer = new MockTracer();
        long now = System.currentTimeMillis();
        Span span = tracer.buildSpan("foo")
                .withStartTimestamp(now, TimeUnit.MILLISECONDS)
                .startManual();
        span.finish();

        Assert.assertEquals(now, tracer.finishedSpans().get(0).startTimestamp(TimeUnit.MILLISECONDS));
    }

    @Test
    @SuppressWarnings("deprecated")
    public void testExplicitFinishInMicroseconds() {
        MockTracer tracer = new MockTracer();
        Span span = tracer.buildSpan("foo").start();
        long finishTime = 133L;
        span.finish(finishTime);

        try {
            span.setOperationName("bar");
            Assert.fail();
        } catch (RuntimeException ex) {
        }
        Assert.assertEquals(finishTime, tracer.finishedSpans().get(0).finishTimestamp(TimeUnit.MICROSECONDS));
    }

    @Test
    public void testExplicitFinish() {
        MockTracer tracer = new MockTracer();
        Span span = tracer.buildSpan("foo").start();
        long finishTime = System.currentTimeMillis();
        span.finish(finishTime, TimeUnit.MILLISECONDS);

        try {
            span.setOperationName("bar");
            Assert.fail();
        } catch (RuntimeException ex) {
        }
        Assert.assertEquals(finishTime, tracer.finishedSpans().get(0).finishTimestamp(TimeUnit.MILLISECONDS));
    }

    @Test
    public void testSetOperationNameAfterFinish() {
        MockTracer tracer = new MockTracer();
        Span span = tracer.buildSpan("foo").start();
        span.finish();

        try {
            span.setOperationName("bar");
            Assert.fail();
        } catch (RuntimeException ex) {
        }
        Assert.assertEquals(1, tracer.finishedSpans().get(0).generatedErrors().size());
    }

    @Test
    public void testSetTagAfterFinish() {
        MockTracer tracer = new MockTracer();
        Span span = tracer.buildSpan("foo").start();
        span.finish();

        try {
            span.setTag("bar", "foo");
            Assert.fail();
        } catch (RuntimeException ex) {
        }
        Assert.assertEquals(1, tracer.finishedSpans().get(0).generatedErrors().size());
    }

    @Test
    public void testAddLogAfterFinish() {
        MockTracer tracer = new MockTracer();
        Span span = tracer.buildSpan("foo").start();
        span.finish();

        try {
            span.log("bar");
            Assert.fail();
        } catch (RuntimeException ex) {
        }
        Assert.assertEquals(1, tracer.finishedSpans().get(0).generatedErrors().size());
    }

    @Test
    public void testAddBaggageAfterFinish() {
        MockTracer tracer = new MockTracer();
        Span span = tracer.buildSpan("foo").start();
        span.finish();

        try {
            span.setBaggageItem("foo", "bar");
            Assert.fail();
        } catch (RuntimeException ex) {
        }
        Assert.assertEquals(1, tracer.finishedSpans().get(0).generatedErrors().size());
    }

    @Test
    @SuppressWarnings("deprecated")
    public void testAddLogEventTimestampInMicroseconds() {
        MockTracer tracer = new MockTracer();
        Span span = tracer.buildSpan("foo").start();
        long now = System.currentTimeMillis() * 1000;
        span.log(now, "foo");
        span.finish();

        MockSpan actual = tracer.finishedSpans().get(0);
        MockSpan.LogEntry logEntry = actual.logEntries().get(0);
        Assert.assertEquals(now, logEntry.timestamp(TimeUnit.MICROSECONDS));
    }

    @Test
    public void testAddLogEventTimestamp() {
        MockTracer tracer = new MockTracer();
        Span span = tracer.buildSpan("foo").start();
        long now = System.currentTimeMillis();
        span.log(now, TimeUnit.MILLISECONDS, "foo");
        span.finish();

        MockSpan actual = tracer.finishedSpans().get(0);
        MockSpan.LogEntry logEntry = actual.logEntries().get(0);
        Assert.assertEquals(now, logEntry.timestamp(TimeUnit.MILLISECONDS));
    }

    @Test
    @SuppressWarnings("deprecated")
    public void testAddLogFieldsTimestampInMicroseconds() {
        MockTracer tracer = new MockTracer();
        Span span = tracer.buildSpan("foo").start();
        long now = System.currentTimeMillis() * 1000;
        Map<String, String> fields = new HashMap<>();
        fields.put("foo", "bar");
        span.log(now, fields);
        span.finish();

        MockSpan actual = tracer.finishedSpans().get(0);
        MockSpan.LogEntry logEntry = actual.logEntries().get(0);
        Assert.assertEquals(now, logEntry.timestamp(TimeUnit.MICROSECONDS));
    }

    @Test
    public void testAddLogFieldsTimestamp() {
        MockTracer tracer = new MockTracer();
        Span span = tracer.buildSpan("foo").start();
        long now = System.currentTimeMillis();
        Map<String, String> fields = new HashMap<>();
        fields.put("foo", "bar");
        span.log(now, TimeUnit.MILLISECONDS, fields);
        span.finish();

        MockSpan actual = tracer.finishedSpans().get(0);
        MockSpan.LogEntry logEntry = actual.logEntries().get(0);
        Assert.assertEquals(now, logEntry.timestamp(TimeUnit.MILLISECONDS));
    }

}
