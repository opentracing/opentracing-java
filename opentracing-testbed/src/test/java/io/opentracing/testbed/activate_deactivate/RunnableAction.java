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
package io.opentracing.testbed.activate_deactivate;

import io.opentracing.util.AutoFinishScope;
import io.opentracing.util.AutoFinishScope.Continuation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Runnable Action. Scheduler submit it for execution.
 */
public class RunnableAction implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RunnableAction.class);

    private final Continuation continuation;

    RunnableAction(AutoFinishScope scope) {
        continuation = scope.capture();
        logger.info("Action created");
    }

    /**
     * Can be used continuation.activate().deactivate() chain only. It is splitted for testing
     * purposes (span should not be finished before deactivate() called here).
     */
    @Override
    public void run() {
        logger.info("Action started");
        AutoFinishScope scope = continuation.activate();

        try {
            TimeUnit.SECONDS.sleep(1); // without sleep first action can finish before second is started
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // set random tag starting with 'test_tag_' to test that finished span has all of them
        scope.span().setTag("test_tag_" + ThreadLocalRandom.current().nextInt(), "random");

        scope.close();
        logger.info("Action finished");
    }
}
