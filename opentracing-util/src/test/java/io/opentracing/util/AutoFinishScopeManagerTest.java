/*
 * Copyright 2016-2018 The OpenTracing Authors
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

public class AutoFinishScopeManagerTest {
    private AutoFinishScopeManager source;
    @Before
    public void before() throws Exception {
        source = new AutoFinishScopeManager();
    }

    @Test
    public void missingScope() throws Exception {
        Scope missingSpan = source.active();
        assertNull(missingSpan);
    }

    @Test
    public void activateSpan() throws Exception {
        Span span = mock(Span.class);

        // We can't use 1.7 features like try-with-resources in this repo without meddling with pom details for tests.
        Scope active = source.activate(span, true);
        try {
            assertNotNull(active);
            Scope otherScope = source.active();
            assertEquals(otherScope, active);
        } finally {
            active.close();
        }

        // Make sure the Span got finish()ed.
        verify(span).finish();

        // And now it's gone:
        Scope missingSpan = source.active();
        assertNull(missingSpan);
    }
}
