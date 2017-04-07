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

import io.opentracing.propagation.Format;

/**
 * Tracer is a simple, thin interface for Span creation and propagation across arbitrary transports.
 */
public interface Tracer {

    /**
     * Return a new SpanBuilder for a Span with the given `operationName`.
     *
     * <p>If there is an active Span according to the {@link Tracer#spanSource()}'s {@link ActiveSpanSource#activeContext},
     * buildSpan will automatically reference that active Span as a parent.
     *
     * <p>You can override the operationName later via {@link Span#setOperationName(String)}.
     *
     * <p>A contrived example:
     * <pre>{@code
     *   Tracer tracer = ...
     *
     *   // Note: if there is an {@link ActiveSpanSource#activeContext()}, it will be treated as the parent of workSpan.
     *   try (ActiveSpanSource.Handle workHandle = tracer.buildSpan("DoWork").startAndActivate()) {
     *       workHandle.span().setTag("...", "...");
     *       // etc, etc
     *   }
     *
     *   // It's also possible to create Spans with explicit parent References and tags.
     *   Span http = tracer.buildSpan("HandleHTTPRequest")
     *                     .asChildOf(rpcSpanContext)  // an explicit parent
     *                     .withTag("user_agent", req.UserAgent)
     *                     .withTag("lucky_number", 42)
     *                     .start();
     * }</pre>
     */
    SpanBuilder buildSpan(String operationName);

    /**
     * @return the ActiveSpanSource associated with this Tracer. Must not be null.
     *
     * @see ActiveSpanSource
     * @see ThreadLocalActiveSpanSource a simple built-in thread-local-storage-based ActiveSpanSource
     */
    ActiveSpanSource spanSource();

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
     * tracer.buildSpan('...').asChildOf(spanCtx).start();
     * }</pre>
     *
     * If the span serialized state is invalid (corrupt, wrong version, etc) inside the carrier this will result in an
     * IllegalArgumentException.
     *
     * @param <C> the carrier type, which also parametrizes the Format.
     * @param format the Format of the carrier
     * @param carrier the carrier for the SpanContext state. All Tracer.extract() implementations must support
     *                io.opentracing.propagation.TextMap and java.nio.ByteBuffer.
     *
     * @return the SpanContext instance holding context to create a Span.
     *
     * @see io.opentracing.propagation.Format
     * @see io.opentracing.propagation.Format.Builtin
     */
    <C> SpanContext extract(Format<C> format, C carrier);


    interface SpanBuilder {

        /**
         * A shorthand for addReference(References.CHILD_OF, parent).
         */
        SpanBuilder asChildOf(SpanContext parent);

        /**
         * A shorthand for addReference(References.CHILD_OF, parent.context()).
         */
        SpanBuilder asChildOf(Span parent);

        /**
         * Add a reference from the Span being built to a distinct (usually parent) Span. May be called multiple times to
         * represent multiple such References.
	 *
         * <p>
         * If
	 * <ul>
	 * <li>the {@link Tracer}'s {@link ActiveSpanSource#active()} is not null, and
	 * <li>no <b>explicit</b> references are added via {@link SpanBuilder#addReference}, and
	 * <li>{@link SpanBuilder#asRoot()} is not invoked,
	 * </ul>
	 * ... then an inferred {@link References#CHILD_OF} reference is created to the {@link ActiveSpanSource#active()}
	 * {@link SpanContext} when either {@link SpanBuilder#start()} or {@link SpanBuilder#startAndActivate} is invoked.
         *
         * @param referenceType the reference type, typically one of the constants defined in References
         * @param referencedContext the SpanContext being referenced; e.g., for a References.CHILD_OF referenceType, the
         *                          referencedContext is the parent
         *
         * @see io.opentracing.References
         */
        SpanBuilder addReference(String referenceType, SpanContext referencedContext);

        /**
         * Remove any explicit (e.g., via {@link SpanBuilder#addReference(String,SpanContext)}) or implicit (e.g., via
         * {@link ActiveSpanSource#active}) references to parent / predecessor SpanContexts, thus making the built
         * Span a "root" of a Trace tree/graph.
         *
         * <p>
         * Subsequent calls to {@link SpanBuilder#addReference(String, SpanContext)} /
         * {@link SpanBuilder#asChildOf(Span)} / etc are permitted and behave as per usual.
         */
        SpanBuilder asRoot();

        /** Same as {@link Span#setTag(String, String)}, but for the span being built. */
        SpanBuilder withTag(String key, String value);

        /** Same as {@link Span#setTag(String, boolean)}, but for the span being built. */
        SpanBuilder withTag(String key, boolean value);

        /** Same as {@link Span#setTag(String, Number)}, but for the span being built. */
        SpanBuilder withTag(String key, Number value);

        /** Specify a timestamp of when the Span was started, represented in microseconds since epoch. */
        SpanBuilder withStartTimestamp(long microseconds);

        /**
         * Returns a newly started and activated {@link ActiveSpan}.
         *
         * <p>
         * The returned {@link ActiveSpan} supports try-with-resources. For example:
         * <pre>{@code
         *     try (ActiveSpanSource.Handle handle = tracer.buildSpan("...").startAndActivate()) {
         *         // Do work
         *         Span span = tracer.spanSource().activeSpan();
         *         span.setTag( ... );  // etc, etc
         *     }  // Span finishes automatically unless pinned via {@link ActiveSpan#defer}
         * }</pre>
         *
         * <p>
         * If
         * <ul>
         * <li>the {@link Tracer}'s {@link ActiveSpanSource#active()} is not null, and
         * <li>no <b>explicit</b> references are added via {@link SpanBuilder#addReference}, and
         * <li>{@link SpanBuilder#asRoot()} is not invoked,
         * </ul>
         * ... then an inferred {@link References#CHILD_OF} reference is created to the {@link ActiveSpanSource#active()}
         * {@link SpanContext} when either {@link SpanBuilder#start()} or {@link SpanBuilder#startAndActivate} is invoked.
         *
         * <p>
         * Note: {@link SpanBuilder#startAndActivate()} is a shorthand for
         * {@code tracer.spanSource().adopt(SpanBuilder.start()).activate()}
         * </p>
         *
         * @return a pre-activated {@link ActiveSpan}
         *
         * @see Tracer#spanSource()
         * @see ActiveSpanSource
         * @see ActiveSpan
         */
        ActiveSpan startAndActivate();

        /**
         * @see SpanBuilder#startAndActivate()
         * @return the newly-started Span instance, which will *not* be automatically activated by the
         *         {@link ActiveSpanSource}
         */
        Span start();

    }
}
