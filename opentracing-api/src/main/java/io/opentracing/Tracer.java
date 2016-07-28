/**
 * Copyright 2016 The OpenTracing Authors
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

import io.opentracing.propagation.Format;

/**
 * Tracer is a simple, thin interface for Span creation and propagation across arbitrary transports.
 */
public interface Tracer {

  /**
   * Return a new SpanBuilder for a Span with the given `operationName`.
   *
   * <p>A contrived example:
   * <pre>{@code
    Tracer tracer = ...

    Span parentSpan = tracer.buildSpan("DoWork")
                            .start();

    Span http = tracer.buildSpan("HandleHTTPRequest")
                      .asChildOf(parentSpan.context())
                      .withTag("user_agent", req.UserAgent)
                      .withTag("lucky_number", 42)
                      .start();
    }</pre>
   */
  SpanBuilder buildSpan(String operationName);

  /**
   * Inject a SpanContext into a `carrier` of a given type, presumably for propagation across process boundaries.
   *
   * <p>Example:
   * <pre>{@code
   * Tracer tracer = ...
   * Span clientSpan = ...
   * TextMap httpHeadersCarrier = new AnHttpHeaderCarrier(httpRequest);
   * tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, httpHeadersCarrier);
   * }</pre>
   *
   * @param <C> the carrier type, which also parametrizes the Format.
   * @param spanContext the SpanContext instance to inject into the carrier
   * @param format the Format of the carrier
   * @param carrier the carrier for the SpanContext state. All Tracer.inject() implementations must support io.opentracing.propagation.TextMap and java.nio.ByteBuffer.
   *
   * @see io.opentracing.propagation.Format
   * @see io.opentracing.propagation.Format.Builtin
   */
  <C> void inject(SpanContext spanContext, Format<C> format, C carrier);

  /**
   * Extract a SpanContext from a `carrier` of a given type, presumably after propagation across a process boundary.
   *
   * <p>Example:
   * <pre>{@code
   * Tracer tracer = ...
   * TextMap httpHeadersCarrier = new AnHttpHeaderCarrier(httpRequest);
   * SpanContext spanCtx = tracer.extract(Format.Builtin.HTTP_HEADERS, httpHeadersCarrier);
   * tracer.buildSpan('...').withChildOf(spanCtx).start();
   * }</pre>
   *
   * If the span serialized state is invalid (corrupt, wrong version, etc) inside the carrier this will result in an
   * IllegalArgumentException.
   *
   * @param <C> the carrier type, which also parametrizes the Format.
   * @param format the Format of the carrier
   * @param carrier the carrier for the SpanContext state. All Tracer.extract() implementations must support
   *                io.opentracing.propagation.TextMap and java.nio.ByteBuffer.
   * @returns the SpanContext instance extracted from the carrier
   *
   * @see io.opentracing.propagation.Format
   * @see io.opentracing.propagation.Format.Builtin
   */
  <C> SpanContext extract(Format<C> format, C carrier);


  interface SpanBuilder {

      /**
       * A shorthand for withReference(Reference.childOf(parent)).
       */
      SpanBuilder asChildOf(SpanContext parent);

      /**
       * A shorthand for withReference(Reference.childOf(parent.context())).
       */
      SpanBuilder asChildOf(Span parent);

      /**
       * Add a reference from the Span being built to a distinct (usually parent) Span. May be called multiple times to
       * represent multiple such References.
       *
       * @param referenceType the reference type, typically one of the constants defined in References
       * @param referencedContext the SpanContext being referenced; e.g., for a References.CHILD_OF referenceType, the
       *                          referencedContext is the parent
       *
       * @see io.opentracing.References
       */
      SpanBuilder addReference(String referenceType, SpanContext referencedContext);

      /** Same as {@link Span#setTag(String, String)}, but for the span being built. */
      SpanBuilder withTag(String key, String value);

      /** Same as {@link Span#setTag(String, String)}, but for the span being built. */
      SpanBuilder withTag(String key, boolean value);

      /** Same as {@link Span#setTag(String, String)}, but for the span being built. */
      SpanBuilder withTag(String key, Number value);

      /** Specify a timestamp of when the Span was started, represented in microseconds since epoch. */
      SpanBuilder withStartTimestamp(long microseconds);

      /** Returns the started Span. */
      Span start();

  }
}
