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
 * Represents an in-flight Span that's <strong>manually propagated</strong> within the given process. Most of
 * the API lives in {@link BaseSpan}.
 *
 * <p>{@link Span}s are created by the {@link Tracer.SpanBuilder#startManual} method; see {@link ActiveSpan} for
 * a {@link BaseSpan} extension designed for automatic in-process propagation.
 *
 * <p>
 * Note that most application code interacts with {@link ActiveSpan} instances (which make themselves available
 * for in-process propagation via the {@link ActiveSpanSource} interface).
 *
 * @see ActiveSpan
 * @see SpanFinisher
 * @see Tracer.SpanBuilder#startManual()
 * @see Tracer.SpanBuilder#startActive()
 */
public interface Span extends BaseSpan<Span>, SpanFinisher {
}
