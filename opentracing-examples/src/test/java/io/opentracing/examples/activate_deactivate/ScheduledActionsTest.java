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
package io.opentracing.examples.activate_deactivate;

import io.opentracing.Scope;
import io.opentracing.examples.AutoFinishScope;
import io.opentracing.examples.AutoFinishScopeManager;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.opentracing.examples.TestUtils.finishedSpansSize;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;

public class ScheduledActionsTest {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledActionsTest.class);

    private final MockTracer tracer = new MockTracer(new AutoFinishScopeManager(),
            Propagator.TEXT_MAP);
    private final ScheduledExecutorService service = Executors.newScheduledThreadPool(10);

    @Test
    public void test_one_scheduled_action() throws Exception {
        Thread entryThread = entryThread();
        entryThread.start();

        await().atMost(15, TimeUnit.SECONDS).until(finishedSpansSize(tracer), equalTo(1));

        List<MockSpan> finished = tracer.finishedSpans();
        assertEquals(1, finished.size());

        assertEquals(1, getTestTagsCount(finished.get(0)));
    }

    @Test
    public void test_two_scheduled_actions() throws Exception {
        Thread entryThread = entryThreadWithTwoActions();
        entryThread.start();
        entryThread.join(10_000);
        // Entry thread is completed but Actions are still running (or even not started)

        await().atMost(15, TimeUnit.SECONDS).until(finishedSpansSize(tracer), equalTo(1));

        List<MockSpan> finished = tracer.finishedSpans();
        assertEquals(1, finished.size());

        // Check that two actions finished and each added to span own tag ('test_tag_{random}')
        assertEquals(2, getTestTagsCount(finished.get(0)));
    }

    private int getTestTagsCount(MockSpan mockSpan) {
        int tagCounter = 0;
        for (String tagKey : mockSpan.tags().keySet()) {
            if (tagKey.startsWith("test_tag_")) {
                tagCounter++;
            }
        }
        return tagCounter;
    }

    /**
     * Thread will be completed before action completed.
     */
    private Thread entryThread() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Entry thread started");
                try (Scope scope = tracer.buildSpan("parent").startActive()) {
                    Runnable action = new RunnableAction((AutoFinishScope)scope);

                    // Action is executed at some time and we are not able to check status
                    service.schedule(action, 500, TimeUnit.MILLISECONDS);
                }
                logger.info("Entry thread finished");
            }
        });
    }

    /**
     * Thread will be completed before action completed.
     */
    private Thread entryThreadWithTwoActions() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Entry thread 2x started");
                try (Scope scope = tracer.buildSpan("parent").startActive()) {
                    Runnable action = new RunnableAction((AutoFinishScope)scope);
                    Runnable action2 = new RunnableAction((AutoFinishScope)scope);

                    Random random = new Random();

                    // Actions are executed at some time and most likely are running in parallel
                    service.schedule(action, random.nextInt(1000) + 100, TimeUnit.MILLISECONDS);
                    service.schedule(action2, random.nextInt(1000) + 100, TimeUnit.MILLISECONDS);

                }
                logger.info("Entry thread 2x finished");
            }
        });
    }
}
