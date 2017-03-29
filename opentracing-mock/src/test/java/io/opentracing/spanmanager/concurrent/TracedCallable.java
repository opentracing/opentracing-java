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
package io.opentracing.spanmanager.concurrent;

import java.util.concurrent.Callable;

import io.opentracing.SpanManager;

/**
 * @author Pavol Loffay
 */
public class TracedCallable<V> implements Callable<V>{

    private Callable<V> wrapped;
    private SpanManager.VisibilityContext visibility;

    public TracedCallable(Callable<V> wrapped, SpanManager spanManager) {
        this.wrapped = wrapped;
        this.visibility = spanManager.active().visibility().capture();

    }

    @Override
    public V call() throws Exception {
        visibility.on();
       try {
            return wrapped.call();
        } finally {
            visibility.off();
        }
    }
}
