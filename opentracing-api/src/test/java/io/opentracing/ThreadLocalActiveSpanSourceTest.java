/**
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
package io.opentracing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ThreadLocalActiveSpanSourceTest {
    private ThreadLocalActiveSpanSource source;
    @Before
    public void before() throws Exception {
        source = new ThreadLocalActiveSpanSource();
    }

    @Test
    public void missingActiveSpan() throws Exception {
        ActiveSpan missingSpan = source.activeSpan();
        assertNull(missingSpan);
    }

    @Test
    public void makeActiveSpan() throws Exception {
        Span span = mock(Span.class);

        // We can't use 1.7 features like try-with-resources in this repo -- argh.
        //
        // F*** IT, WE'LL DO IT LIVE!
        ActiveSpan activeSpan = source.makeActive(span);
        try {
            assertNotNull(activeSpan);
            ActiveSpan otherActiveSpan = source.activeSpan();
            assertEquals(otherActiveSpan, activeSpan);
        } finally {
            activeSpan.close();
        }

        // Make sure the Span got finish()ed.
        verify(span).finish();

        // And now it's gone:
        ActiveSpan missingSpan = source.activeSpan();
        assertNull(missingSpan);
    }

}