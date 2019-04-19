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

import io.opentracing.Scope;
import io.opentracing.Span;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ThreadLocalScopeManagerTest {
    private ThreadLocalScopeManager source;

    @Before
    public void before() throws Exception {
        source = new ThreadLocalScopeManager();
    }

    @Test
    public void missingActiveSpan() throws Exception {
        Span missingSpan = source.activeSpan();
        assertNull(missingSpan);
    }

    @Test
    public void defaultActivateSpan() throws Exception {
        Span span = mock(Span.class);

        Scope scope = source.activate(span);
        try {
            assertNotNull(scope);

            Span otherSpan = source.activeSpan();
            assertEquals(otherSpan, span);
        } finally {
            scope.close();
        }

        // Make sure the Span is not finished.
        verify(span, never()).finish();

        // And now Scope/Span are gone:
        Span missingSpan = source.activeSpan();
        assertNull(missingSpan);
    }
}
