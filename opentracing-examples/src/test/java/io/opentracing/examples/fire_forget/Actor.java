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
package io.opentracing.examples.fire_forget;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Actor
{
    BlockingQueue<Object> messages = new LinkedBlockingQueue();

    public void startReceiving(ExecutorService executor) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                Object message;
                try {
                    while ((message = messages.take()) != null) {
                        receive(message);
                    }
                } catch (InterruptedException exc) {
                }
            }
        });
    }

    public void tell(Object message) {
        messages.add(message);
    }

    abstract void receive(Object message);
}
