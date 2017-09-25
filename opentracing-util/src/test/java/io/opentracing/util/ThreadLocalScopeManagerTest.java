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

import io.opentracing.Scope;
import io.opentracing.Span;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ThreadLocalScopeManagerTest {
    private ThreadLocalScopeManager source;

    @Before
    public void before() throws Exception {
        source = new ThreadLocalScopeManager();
    }

    @Test
    public void missingActiveScope() throws Exception {
        Scope missingScope = source.active();
        assertNull(missingScope);
    }

    @Test
    public void activateSpan() throws Exception {
        Span span = mock(Span.class);

        // We can't use 1.7 features like try-with-resources in this repo without meddling with pom details for tests.
        Scope scope = source.activate(span);
        try {
            assertNotNull(scope);
            Scope otherScope = source.active();
            assertEquals(otherScope, scope);
        } finally {
            scope.close();
        }

        // Make sure the Span got finish()ed.
        verify(span, times(1)).finish();

        // And now it's gone:
        Scope missingScope = source.active();
        assertNull(missingScope);
    }

    @Test
    public void activateSpanClose() throws Exception {
        Span span = mock(Span.class);

        // We can't use 1.7 features like try-with-resources in this repo without meddling with pom details for tests.
        Scope scope = source.activate(span, true);
        try {
            assertNotNull(scope);
            assertNotNull(source.active());
        } finally {
            scope.close();
        }

        // Make sure the Span got finish()ed.
        verify(span, times(1)).finish();

        // Verify it's gone.
        assertNull(source.active());
    }

    @Test
    public void activateSpanNoClose() throws Exception {
        Span span = mock(Span.class);

        // We can't use 1.7 features like try-with-resources in this repo without meddling with pom details for tests.
        Scope scope = source.activate(span, false);
        try {
            assertNotNull(scope);
            assertNotNull(source.active());
        } finally {
            scope.close();
        }

        // Make sure the Span did *not* get finish()ed.
        verify(span, never()).finish();

        // Verify it's gone.
        assertNull(source.active());
    }
}
