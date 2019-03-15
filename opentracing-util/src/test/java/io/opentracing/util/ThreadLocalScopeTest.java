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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.opentracing.Scope;
import io.opentracing.Span;
import org.junit.Before;
import org.junit.Test;

public class ThreadLocalScopeTest {
    private ThreadLocalScopeManager scopeManager;
    private ScopeListener scopeListener;

    @Before
    public void before() throws Exception {
        scopeListener = mock(ScopeListener.class);
        scopeManager = new ThreadLocalScopeManager(scopeListener);
    }

    @Test
    public void implicitSpanStack() throws Exception {
        Span backgroundSpan = mock(Span.class);
        Span foregroundSpan = mock(Span.class);

        // Quasi try-with-resources (this is 1.6).
        Scope backgroundActive = scopeManager.activate(backgroundSpan, true);
        try {
            assertNotNull(backgroundActive);

            // Activate a new Scope on top of the background one.
            Scope foregroundActive = scopeManager.activate(foregroundSpan, true);
            try {
                Scope shouldBeForeground = scopeManager.active();
                assertEquals(foregroundActive, shouldBeForeground);
            } finally {
                foregroundActive.close();
            }

            // And now the backgroundActive should be reinstated.
            Scope shouldBeBackground = scopeManager.active();
            assertEquals(backgroundActive, shouldBeBackground);
        } finally {
            backgroundActive.close();
        }

        // The background and foreground Spans should be finished.
        verify(backgroundSpan, times(1)).finish();
        verify(foregroundSpan, times(1)).finish();

        // Verify listener calls
        verify(scopeListener, times(2)).onActivate(backgroundSpan);
        verify(scopeListener, times(1)).onActivate(foregroundSpan);
        verify(scopeListener, times(1)).onClose();

        // And now nothing is active.
        Scope missingSpan = scopeManager.active();
        assertNull(missingSpan);
    }

    @Test
    public void testDeactivateWhenDifferentSpanIsActive() {
        Span span = mock(Span.class);
        Span nestedSpan = mock(Span.class);

        Scope active = scopeManager.activate(span, false);
        scopeManager.activate(nestedSpan, false);
        active.close();

        verify(span, times(0)).finish();

        verify(scopeListener, times(1)).onActivate(span);
        verify(scopeListener, times(1)).onActivate(nestedSpan);
        verify(scopeListener, times(0)).onClose();
    }
}
