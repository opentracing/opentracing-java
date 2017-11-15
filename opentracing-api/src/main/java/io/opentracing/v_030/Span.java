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
package io.opentracing.v_030;

/**
 * Represents an in-flight Span that's <strong>manually propagated</strong> within the given process. Most of
 * the API lives in {@link BaseSpan}.
 *
 * <p>{@link Span}s are created by the {@link Tracer.SpanBuilder#startManual} method; see {@link ActiveSpan} for
 * a {@link BaseSpan} extension designed for automatic in-process propagation.
 *
 * @see ActiveSpan for automatic propagation (recommended for most intstrumentation!)
 */
public interface Span extends BaseSpan<Span> {
    /**
     * Sets the end timestamp to now and records the span.
     *
     * <p>With the exception of calls to {@link #context}, this should be the last call made to the span instance.
     * Future calls to {@link #finish} are defined as noops, and future calls to methods other than {@link #context}
     * lead to undefined behavior.
     *
     * @see Span#context()
     */
    void finish();

    /**
     * Sets an explicit end timestamp and records the span.
     *
     * <p>With the exception of calls to Span.context(), this should be the last call made to the span instance, and to
     * do otherwise leads to undefined behavior.
     *
     * @param finishMicros an explicit finish time, in microseconds since the epoch
     *
     * @see Span#context()
     */
    void finish(long finishMicros);
}
