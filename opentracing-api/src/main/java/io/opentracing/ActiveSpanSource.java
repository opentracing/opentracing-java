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

/**
 * {@link ActiveSpanSource} allows an existing (possibly thread-local-aware) execution context provider to act as a
 * source for an actively-scheduled OpenTracing Span.
 *
 * @see ActiveSpan
 */
public interface ActiveSpanSource {

    /**
     * @return the {@link ActiveSpan active span}, or null if none could be found. This does not affect the reference
     * count for the {@link ActiveSpan}.
     */
    ActiveSpan activeSpan();

    /**
     * Wrap and "makeActive" a {@link Span} by encapsulating it – and any active state (e.g., MDC state) in the
     * execution context – in a new {@link ActiveSpan}.
     *
     * @param span the Span just started
     * @return an {@link ActiveSpan} that encapsulates the given Span and any other Source-specific context (e.g.,
     * MDC data)
     */
    ActiveSpan makeActive(Span span);
}
