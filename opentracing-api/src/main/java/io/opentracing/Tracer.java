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


/**
 * Tracer is a simple, thin interface for Span creation, and Span propagation into different transport formats.
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
                      .withChildOf(parentSpan.context())
                      .withTag("user_agent", req.UserAgent)
                      .withTag("lucky_number", 42)
                      .start();
    }</pre>
   */
  SpanBuilder buildSpan(String operationName);

  /**
   * Inject a SpanContext into a `carrier` of a given `format`, presumably for propagation across process boundaries.
   *
   * <p>Example:
   * <pre>{@code
   * Tracer tracer = ...
   * Span clientSpan = ...
   * TextMapWriter httpHeaderWriter = new AnHttpHeaderCarrier(httpRequest);
   * tracer.inject(span.context(), BuiltinFormats.HTTP_HEADER, httpHeaderWriter);
   * }</pre>
   *
   * @param spanContext the SpanContext instance to inject into the carrier
   * @param format the Format of the carrier. See BuiltinFormats.
   * @param carrier the carrier for the SpanContext state; when inject() returns, the Tracer implementation will have represented the SpanContext within `carrier`
   *
   * All implementations must support, at minimum, the BuiltinFormats.
   *
   * @see BuiltinFormats
   */
  <W> void inject(SpanContext spanContext, Format<?, W> format, W carrier);

  /**
   * Extract a SpanContext from a `carrier` of a given `format`, presumably after propagation across a process boundary.
   *
   * <p>Example:
   * <pre>{@code
   * Tracer tracer = ...
   * TextMapReader httpHeaderReader = new AnHttpHeaderCarrier(httpRequest);
   * SpanContext spanCtx = tracer.extract(BuiltinFormats.HTTP_HEADER, httpHeaderReader);
   * tracer.buildSpan('...').withChildOf(spanCtx).start();
   * }</pre>
   *
   * If the span serialized state is invalid (corrupt, wrong version, etc) inside the carrier this will result in an IllegalArgumentException.
   *
   * @param format the Format of the carrier. See BuiltinFormats.
   * @param carrier the carrier for the SpanContext state
   * @returns the SpanContext instance extracted from the carrier
   *
   * All implementations must support, at minimum, the BuiltinFormats.
   *
   * @see BuiltinFormats
   */
  <R> SpanContext extract(Format<R, ?> format, R carrier);


  interface SpanBuilder {

      /**
       * A shorthand for withReference(Reference.childOf(parent)).
       */
      SpanBuilder withChildOf(SpanContext parent);

      /**
       * Add a reference from the Span being built to a distinct (usually parent) Span. May be called multiple times to represent multiple such References.
       */
      SpanBuilder withReference(Reference ref);

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
