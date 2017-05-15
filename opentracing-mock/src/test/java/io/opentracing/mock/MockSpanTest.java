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

/**
 * @author Pavol Loffay
 */
public class MockSpanTest {

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
}
