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

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpan.Continuation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Callback which executed at some time. We don't know when it is started, when it is
 * completed. We cannot check status of it (started or completed)
 */
public class Callback implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Callback.class);

    private final Random random = new Random();

    private final Continuation continuation;

    Callback(ActiveSpan activeSpan) {
        continuation = activeSpan.capture();
        logger.info("Callback created");
    }

    /**
     * Can be used continuation.activate().deactivate() chain only. It is splitted for testing
     * purposes (span should not be finished before deactivate() called here).
     */
    @Override
    public void run() {
        logger.info("Callback started");
        ActiveSpan activeSpan = continuation.activate();

        try {
            TimeUnit.SECONDS.sleep(1); // without sleep first callback can finish before second is started
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // set random tag starting with 'test_tag_' to test that finished span has all of them
        activeSpan.setTag("test_tag_" + random.nextInt(), "random");

        activeSpan.deactivate();
        logger.info("Callback finished");
    }
}
