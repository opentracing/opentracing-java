/*
 * Copyright 2016-2019 The OpenTracing Authors
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
package io.opentracing.noop;

import static org.junit.Assert.*;

import org.junit.Test;

import io.opentracing.Span;
import io.opentracing.tag.Tags;

public class NoopTracerTest {

    @Test
    public void activeSpanValueToleratesUseTest() {
        try {
            final Span activeSpan = NoopTracerImpl.INSTANCE.activeSpan();
            assertNotNull(activeSpan);
            Tags.ERROR.set(activeSpan, true);
        } catch (final NullPointerException e) {
            fail("NoopTracer.activeSpan() should return a usable span");
        }
    }
}