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
package io.opentracing;

/**
 * {@link ActiveSpanSource} allows an existing (possibly thread-local-aware) execution context provider to act as a
 * source for an actively-scheduled OpenTracing Span.
 *
 * <p>
 * {@link ActiveSpanSource} is a super-interface to {@link Tracer}, so note that all {@link Tracer}s fulfill the
 * {@link ActiveSpanSource} contract.
 *
 * @see ActiveSpan
 */
public interface ActiveSpanSource {

    /**
     * Return the {@link ActiveSpan active span}. This does not affect the internal reference count for the
     * {@link ActiveSpan}.
     *
     * <p>
     * If there is an {@link ActiveSpan active span}, it becomes an implicit parent of any newly-created
     * {@link Span span} at {@link Tracer.SpanBuilder#startActive()} time (rather than at
     * {@link Tracer#buildSpan(String)} time).
     *
     * @return the {@link ActiveSpan active span}, or null if none could be found.
     */
    ActiveSpan activeSpan();

    /**
     * Wrap and "make active" a {@link Span} by encapsulating it – and any active state (e.g., MDC state) in the
     * current thread – in a new {@link ActiveSpan}.
     *
     * @param span the Span to wrap in an {@link ActiveSpan}
     * @return an {@link ActiveSpan} that encapsulates the given {@link Span} and any other
     *     {@link ActiveSpanSource}-specific context (e.g., the MDC context map)
     */
    ActiveSpan makeActive(Span span);
    ActiveSpan makeActive(Span span, ActiveSpan.Observer observer);
}
