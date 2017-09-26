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
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests against deprecated behavior and/or API.
 */
@SuppressWarnings("deprecation")
public class MockSpanDeprecationsTest {

    @Test
    public void testOldStartMethodWtihFinishMicros() throws Exception {
        MockTracer tracer = new MockTracer();
        Span span = tracer.buildSpan("foo").start();
        Thread.sleep(1L);
        span.finish();

        MockSpan finishedSpan = tracer.finishedSpans().get(0);
        long finishMicros = finishedSpan.finishMicros();
        assertTrue(finishMicros > 0);
    }

}
