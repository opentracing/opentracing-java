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

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpanSource;
import io.opentracing.Span;
import io.opentracing.Tracer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple {@link ActiveSpanSource} implementation built on top of Java's thread-local storage primitive.
 *
 * @see ThreadLocalActiveSpan
 * @see Tracer#activeSpan()
 */
public class ThreadLocalActiveSpanSource implements ActiveSpanSource {
    final ThreadLocal<ThreadLocalActiveSpan> tlsSnapshot = new ThreadLocal<ThreadLocalActiveSpan>();

    @Override
    public ThreadLocalActiveSpan activeSpan() {
        return tlsSnapshot.get();
    }

    @Override
    public ActiveSpan makeActive(Span span) {
        return new ThreadLocalActiveSpan(this, span, new AtomicInteger(1));
    }

}
