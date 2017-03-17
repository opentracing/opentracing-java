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
package io.opentracing.util;

import io.opentracing.NoopSpanBuilder;
import io.opentracing.NoopTracer;
import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class GlobalTracerTest {

    Tracer previousGlobalTracer;

    @Before
    public void setup() {
        previousGlobalTracer = GlobalTracer.register(null); // Reset lazy state and remember previous tracer.
    }

    @After
    public void teardown() {
        GlobalTracer.register(previousGlobalTracer instanceof NoopTracer ? null : previousGlobalTracer);
    }

    @Test
    public void testGet_SingletonReference() {
        Tracer tracer = GlobalTracer.get();
        assertThat(tracer, is(instanceOf(GlobalTracer.class)));
        assertThat(tracer, is(sameInstance(GlobalTracer.get())));
    }

    /**
     * The MockTracer has been declared in the META-INF/services/io.opentracing.Tracer file,
     * so should be the default GlobalTracer instance in tests.
     */
    @Test
    public void testGet_AutomaticServiceLoading() {
        GlobalTracer.get().buildSpan("some operation"); // triggers lazy tracer resolution.
        Tracer resolvedTracer = GlobalTracer.register(null); // clear again, returning current (auto-resolved) tracer.
        assertThat("Resolved Tracer service", resolvedTracer, is(instanceOf(MockTracer.class)));
    }

    @Test
    public void testGet_AutomaticServiceLoading_Concurrent() throws InterruptedException {
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        final Tracer[] resolvedTracers = new Tracer[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            threads[i] = new Thread() {
                @Override
                public void run() {
                    GlobalTracer.get().buildSpan("some operation"); // trigger lazy tracer resolution.
                    resolvedTracers[idx] = GlobalTracer.register(GlobalTracer.get()); // no-op returning tracer
                }
            };
        }

        assertThat("Nothing happened yet", identityCount(null, resolvedTracers), is(threadCount));
        // Start threads & wait for completion
        for (int i = 0; i < threadCount; i++) threads[i].start();
        for (int i = 0; i < threadCount; i++) threads[i].join(1000);

        assertThat("Resolved tracer", resolvedTracers[0], is(instanceOf(MockTracer.class)));
        assertThat("Resolved identical in all threads", identityCount(resolvedTracers[0], resolvedTracers), is(threadCount));
    }

    /**
     * Registering an explicit tracer implementation should take precedence, no matter what the global tracer was before.
     */
    @Test
    public void testGet_AfterRegister() {
        GlobalTracer.get().buildSpan("some operation"); // trigger lazy tracer service loading.
        Tracer t1 = mock(Tracer.class), t2 = mock(Tracer.class);
        when(t1.buildSpan(anyString())).thenReturn(NoopSpanBuilder.INSTANCE);
        when(t2.buildSpan(anyString())).thenReturn(NoopSpanBuilder.INSTANCE);

        GlobalTracer.register(t1);
        GlobalTracer.get().buildSpan("first operation");
        GlobalTracer.get().buildSpan("second operation");

        assertThat(GlobalTracer.register(t2), is(sameInstance(t1)));
        GlobalTracer.get().buildSpan("third operation");

        verify(t1).buildSpan(eq("first operation"));
        verify(t1).buildSpan(eq("second operation"));
        verify(t2).buildSpan(eq("third operation"));
        verifyNoMoreInteractions(t1, t2);
    }

    /**
     * Registering the GlobalTracer as its own delegate should be a no-op.
     */
    @Test
    public void testRegister_GlobalTracerAsItsOwnDelegate() {
        Tracer result1 = GlobalTracer.register(GlobalTracer.get());
        Tracer result2 = GlobalTracer.register(GlobalTracer.get());
        assertThat(result1, is(sameInstance(previousGlobalTracer)));
        assertThat(result2, is(sameInstance(previousGlobalTracer)));
    }

    @Test
    public void testRegister_ConcurrentThreads() throws InterruptedException {
        final int threadCount = 10;
        final Tracer[] tracers = new Tracer[threadCount];
        final Tracer[] previous = new Tracer[threadCount];
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) tracers[i] = mock(Tracer.class);
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            threads[idx] = new Thread() {
                @Override
                public void run() {
                    previous[idx] = GlobalTracer.register(tracers[idx]);
                }
            };
        }

        assertThat("Nothing happened yet", identityCount(null, previous), is(threadCount));
        // Start threads & wait for completion
        for (int i = 0; i < threadCount; i++) threads[i].start();
        for (int i = 0; i < threadCount; i++) threads[i].join(1000);

        assertThat("Previous of first is null", identityCount(null, previous), is(1));
        final Tracer last = GlobalTracer.register(null); // last-register tracer ('previous' of new tracer null).
        assertThat("Last must be from tracers", identityCount(last, tracers), is(1));
        assertThat("Last is no previous tracer", identityCount(last, previous), is(0));
        for (int i = 0; i < threadCount; i++) {
            if (last != tracers[i]) { // All non-last tracers should be previous exactly once!
                assertThat("Occurrences in previous", identityCount(tracers[i], previous), is(1));
            }
        }
    }

    @Test
    public void testUpdate_ConcurrentThreads() throws InterruptedException {
        // Preparation
        final Tracer baseTracer = mock(Tracer.class);
        GlobalTracer.register(baseTracer);
        TrivialWrappingTracer.SEQUENCER.set(0);

        // Define Threads updating the Tracing with a wrapper.
        final int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread() {
                @Override
                public void run() {
                    GlobalTracer.update(TrivialWrappingTracer.WRAP);
                }
            };
        }

        // Start threads & wait for completion
        for (int i = 0; i < threadCount; i++) threads[i].start();
        for (int i = 0; i < threadCount; i++) threads[i].join(1000);

        // Assertions about the result of the parallel wrapping functions:
        Tracer current = GlobalTracer.register(GlobalTracer.get());
        assertThat("Current tracer", current, is(instanceOf(TrivialWrappingTracer.class)));
        List<Tracer> flatTracers = ((TrivialWrappingTracer) current).flatten();
        assertThat(threadCount + " wrappers + 1 base tracer", flatTracers, hasSize(threadCount + 1));
        assertThat(flatTracers.get(0), is(current));
        assertThat(flatTracers.get(threadCount), is(baseTracer));
        int wrapperNr = ((TrivialWrappingTracer) current).nr;
        for (int i = 1; i < threadCount; i++) {
            assertThat("tracer " + i, flatTracers.get(i), is(instanceOf(TrivialWrappingTracer.class)));
            TrivialWrappingTracer tracer = (TrivialWrappingTracer) flatTracers.get(i);
            assertThat("descending wrapper number", tracer.nr, is(lessThan(wrapperNr)));
            wrapperNr = tracer.nr;
        }
    }

    private static int identityCount(Tracer needle, Tracer... haystack) {
        int count = 0;
        for (Tracer t : haystack) if (t == needle) count++;
        return count;
    }

}
