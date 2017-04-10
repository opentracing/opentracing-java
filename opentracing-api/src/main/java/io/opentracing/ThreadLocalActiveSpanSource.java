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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A trivial source for the {@linkplain #activeSpan active} {@link ActiveSpan}.
 *
 * @see ThreadLocalActiveSpan
 * @see Tracer#activeSpan()
 */
public class ThreadLocalActiveSpanSource implements ActiveSpanSource {
    final ThreadLocal<ThreadLocalActiveSpan> tlsSnapshot = new ThreadLocal<ThreadLocalActiveSpan>();

    ThreadLocalActiveSpan.Continuation makeContinuation(Span span, AtomicInteger refCount) {
        return new ThreadLocalActiveSpan.Continuation(this, span, refCount);
    }

    @Override
    public ThreadLocalActiveSpan activeSpan() {
        return tlsSnapshot.get();
    }

    @Override
    public ActiveSpan adopt(Span span) {
        return makeContinuation(span, new AtomicInteger(1)).activate();
    }

    // Only for tests
    void clearThreadLocal() {
        tlsSnapshot.remove();
    }

}
