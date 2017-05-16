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
package io.opentracing.util;

import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ThreadLocalActiveSpanTest {
    private ThreadLocalActiveSpanSource source;

    @Before
    public void before() throws Exception {
        source = new ThreadLocalActiveSpanSource();
    }

    @Test
    public void continuation() throws Exception {
        Span span = mock(Span.class);

        // Quasi try-with-resources (this is 1.6).
        ActiveSpan activeSpan = source.makeActive(span);
        ActiveSpan.Continuation continued = null;
        try {
            assertNotNull(activeSpan);
            continued = activeSpan.capture();
        } finally {
            activeSpan.close();
        }

        // Make sure the Span was not finished since there was a capture().
        verify(span, never()).finish();

        // Activate the continuation.
        try {
            activeSpan = continued.activate();
        } finally {
            activeSpan.close();
        }

        // Now the Span should be finished.
        verify(span, times(1)).finish();

        // And now it's no longer active.
        ActiveSpan missingSpan = source.activeSpan();
        assertNull(missingSpan);
    }

    @Test
    public void implicitSpanStack() throws Exception {
        Span backgroundSpan = mock(Span.class);
        Span foregroundSpan = mock(Span.class);

        // Quasi try-with-resources (this is 1.6).
        ActiveSpan backgroundActive = source.makeActive(backgroundSpan);
        try {
            assertNotNull(backgroundActive);

            // Activate a new ActiveSpan on top of the background one.
            ActiveSpan foregroundActive = source.makeActive(foregroundSpan);
            try {
                ActiveSpan shouldBeForeground = source.activeSpan();
                assertEquals(foregroundActive, shouldBeForeground);
            } finally {
                foregroundActive.close();
            }

            // And now the backgroundActive should be reinstated.
            ActiveSpan shouldBeBackground = source.activeSpan();
            assertEquals(backgroundActive, shouldBeBackground);
        } finally {
            backgroundActive.close();
        }

        // The background and foreground Spans should be finished.
        verify(backgroundSpan, times(1)).finish();
        verify(foregroundSpan, times(1)).finish();

        // And now nothing is active.
        ActiveSpan missingSpan = source.activeSpan();
        assertNull(missingSpan);
    }

}