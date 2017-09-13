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

public class ThreadLocalScopeTest {
    private ThreadLocalScopeManager scopeManager;

    @Before
    public void before() throws Exception {
        scopeManager = new ThreadLocalScopeManager();
    }

    @Test
    public void implicitSpanStack() throws Exception {
        Span backgroundSpan = mock(Span.class);
        Span foregroundSpan = mock(Span.class);

        // Quasi try-with-resources (this is 1.6).
        Scope backgroundActive = scopeManager.activate(backgroundSpan);
        try {
            assertNotNull(backgroundActive);

            // Activate a new Scope on top of the background one.
            Scope foregroundActive = scopeManager.activate(foregroundSpan);
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

        // And now nothing is active.
        Scope missingSpan = scopeManager.active();
        assertNull(missingSpan);
    }

    @Test
    public void testDeactivateWhenDifferentSpanIsActive() {
        Span span = mock(Span.class);

        Scope active = scopeManager.activate(span);
        scopeManager.activate(mock(Span.class));
        active.close();

        verify(span, times(0)).finish();
    }
}
