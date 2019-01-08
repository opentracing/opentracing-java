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
package io.opentracing.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.opentracing.Scope;
import io.opentracing.Span;
import org.junit.Before;
import org.junit.Test;

public class AutoFinishScopeTest {
    private AutoFinishScopeManager manager;

    @Before
    public void before() throws Exception {
        manager = new AutoFinishScopeManager();
    }

    @Test
    public void continuation() throws Exception {
        Span span = mock(Span.class);

        // Quasi try-with-resources (this is 1.6).
        AutoFinishScope active = (AutoFinishScope)manager.activate(span, true);
        AutoFinishScope.Continuation continued = null;
        try {
            assertNotNull(active);
            continued = active.capture();
        } finally {
            active.close();
        }

        // Make sure the Span was not finished since there was a capture().
        verify(span, never()).finish();

        // Activate the continuation.
        try {
            active = continued.activate();
        } finally {
            active.close();
        }

        // Now the Span should be finished.
        verify(span, times(1)).finish();

        // And now it's no longer active.
        Scope missingSpan = manager.active();
        assertNull(missingSpan);
    }

    @Test
    public void implicitSpanStack() throws Exception {
        Span backgroundSpan = mock(Span.class);
        Span foregroundSpan = mock(Span.class);

        // Quasi try-with-resources (this is 1.6).
        Scope backgroundActive = manager.activate(backgroundSpan, true);
        try {
            assertNotNull(backgroundActive);

            // Activate a new Scope on top of the background one.
            Scope foregroundActive = manager.activate(foregroundSpan, true);
            try {
                Scope shouldBeForeground = manager.active();
                assertEquals(foregroundActive, shouldBeForeground);
            } finally {
                foregroundActive.close();
            }

            // And now the backgroundActive should be reinstated.
            Scope shouldBeBackground = manager.active();
            assertEquals(backgroundActive, shouldBeBackground);
        } finally {
            backgroundActive.close();
        }

        // The background and foreground Spans should be finished.
        verify(backgroundSpan, times(1)).finish();
        verify(foregroundSpan, times(1)).finish();

        // And now nothing is active.
        Scope missingSpan = manager.active();
        assertNull(missingSpan);
    }

    @Test
    public void testDeactivateWhenDifferentSpanIsActive() {
        Span span = mock(Span.class);

        Scope active = manager.activate(span, true);
        manager.activate(mock(Span.class), true);
        active.close();

        verify(span, times(0)).finish();
    }
}
