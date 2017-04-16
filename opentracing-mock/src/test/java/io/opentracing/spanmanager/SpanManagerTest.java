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
package io.opentracing.spanmanager;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.spanmanager.concurrent.TracedExecutorService;

/**
 * @author Pavol Loffay
 */
public class SpanManagerTest {

    private final MockTracer mockTracer = new MockTracer(MockTracer.Propagator.TEXT_MAP);

    @Before
    public void before() {
        mockTracer.reset();
    }

    @Test
    public void test() {
        MockSpan root = mockTracer.buildSpan("root").start();
        Assert.assertTrue(mockTracer.spanManager().active() == null);

        root.visibility().capture().on();
        Assert.assertEquals(root, mockTracer.spanManager().active().visibility().span());

        MockSpan child = mockTracer.buildSpan("child").start();

        child.finish();
        root.finish();

        Assert.assertEquals(root.context().spanId(), child.parentId());
    }

    @Test
    public void testTracedRunnable() throws ExecutionException, InterruptedException {
        {
            ExecutorService executorService = new TracedExecutorService(Executors.newFixedThreadPool(500),
                    mockTracer.spanManager());

            MockSpan root = mockTracer.buildSpan("root").start();
            root.visibility().capture().on();

            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Assert.assertNotNull(mockTracer.spanManager().active().visibility().span());

                    mockTracer.buildSpan("child")
                            .start()
                            .finish();
                }
            });

            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
            root.finish();
        }

        List<MockSpan> mockSpans = mockTracer.finishedSpans();
        Assert.assertEquals(2, mockSpans.size());
        Assert.assertEquals(mockSpans.get(0).parentId(), mockSpans.get(1).context().spanId());
    }
}
