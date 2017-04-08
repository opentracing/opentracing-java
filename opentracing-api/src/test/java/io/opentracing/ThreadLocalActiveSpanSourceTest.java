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
    public void setUp() throws Exception {
        source = new ThreadLocalActiveSpanSource();
    }

    @After
    public void tearDown() throws Exception {
        source.clearThreadLocal();
    }

    @Test
    public void missingActiveSpan() throws Exception {
        ActiveSpan missingSpan = source.activeSpan();
        assertNull(missingSpan);
    }

    @Test
    public void adoptedActiveSpan() throws Exception {
        Span span = mock(Span.class);

        // We can't use 1.7 features like try-with-resources in this repo -- argh.
        //
        // F*** IT, WE'LL DO IT LIVE!
        ActiveSpan activeSpan = source.adopt(span);
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