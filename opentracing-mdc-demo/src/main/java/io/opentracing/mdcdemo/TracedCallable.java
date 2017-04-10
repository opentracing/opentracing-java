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

package io.opentracing.mdcdemo;

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpanSource;

import java.util.concurrent.Callable;

public class TracedCallable<T> implements Callable<T> {
    private ActiveSpan.Continuation continuation;
    private Callable<T> callable;

    public TracedCallable(Callable<T> callable, ActiveSpanSource spanSource) {
        this(callable, spanSource.activeSpan());
    }

    public TracedCallable(Callable<T> callable, ActiveSpan handle) {
        if (callable == null) throw new NullPointerException("Callable is <null>.");
        this.callable = callable;
        this.continuation = handle.capture();
    }

    public T call() throws Exception {
        try (ActiveSpan handle = continuation.activate()) {
            return callable.call();
        }
    }
}
