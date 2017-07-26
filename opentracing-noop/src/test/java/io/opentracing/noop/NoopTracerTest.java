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
package io.opentracing.noop;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpanSource;
import io.opentracing.Span;
import io.opentracing.Tracer;

public class NoopTracerTest {

    @Test
    public void testMakeActiveUsesActiveSpanSource() {
        ActiveSpan activeSpan = Mockito.mock(ActiveSpan.class);
        ActiveSpanSource activeSpanSource = Mockito.mock(ActiveSpanSource.class);
        Mockito.when(activeSpanSource.makeActive(Matchers.any(Span.class))).thenReturn(activeSpan);

        Tracer tracer = NoopTracerFactory.create(activeSpanSource);

        assertEquals(activeSpan, tracer.makeActive(NoopSpan.INSTANCE));
    }

}
