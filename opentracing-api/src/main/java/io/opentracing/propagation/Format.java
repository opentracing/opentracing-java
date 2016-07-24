package io.opentracing.propagation;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;

import java.nio.ByteBuffer;

/**
 * Format instances control the behavier of Tracer.inject and Tracer.extract (and also constrain the type of the carrier parameter to same).
 *
 * Most OpenTracing users will only reference the Format.Builtin constants. For example:
 *
 * <pre>{@code
 * Tracer tracer = ...
 * io.opentracing.propagation.HttpHeaders httpCarrier = new AnHttpHeaderCarrier(httpRequest);
 * SpanContext spanCtx = tracer.extract(Format.Builtin.HTTP_HEADERS, httpHeaderReader);
 * }</pre>
 *
 * @see Tracer#inject(SpanContext, Format, Object)
 * @see Tracer#extract(Format, Object)
 */
public interface Format<C> {
    class Builtin<C> implements Format<C> {
        /**
         * The TEXT_MAP format allows for arbitrary String->String map encoding of SpanContext state for Tracer.inject and Tracer.extract.
         *
         * Unlike HTTP_HEADERS, the builtin TEXT_MAP format expresses no constraints on keys or values.
         *
         * @see io.opentracing.Tracer#inject(SpanContext, Format, Object)
         * @see io.opentracing.Tracer#extract(Format, Object)
         * @see Format
         * @see Builtin#HTTP_HEADERS
         */
        public final static Format<TextMap> TEXT_MAP = new Builtin<>();

        /**
         * The HTTP_HEADERS format allows for HTTP-header-compatible String->String map encoding of SpanContext state for Tracer.inject and Tracer.extract.
         *
         * I.e., keys written to the TextMap MUST be suitable for HTTP header keys (which are poorly defined but certainly restricted); and similarly for values (i.e., URL-escaped and "not too long").
         *
         * @see io.opentracing.Tracer#inject(SpanContext, Format, Object)
         * @see io.opentracing.Tracer#extract(Format, Object)
         * @see Format
         * @see Builtin#TEXT_MAP
         */
        public final static Format<TextMap> HTTP_HEADERS = new Builtin<>();

        /**
         * The BINARY format allows for unconstrained binary encoding of SpanContext state for Tracer.inject and Tracer.extract.
         *
         * @see io.opentracing.Tracer#inject(SpanContext, Format, Object)
         * @see io.opentracing.Tracer#extract(Format, Object)
         * @see Format
         */
        public final static Format<ByteBuffer> BINARY = new Builtin<>();
    }
}
