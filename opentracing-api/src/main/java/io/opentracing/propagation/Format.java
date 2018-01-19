/*
 * Copyright 2016-2018 The OpenTracing Authors
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
package io.opentracing.propagation;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;

import java.nio.ByteBuffer;

/**
 * Format instances control the behavior of Tracer.inject and Tracer.extract (and also constrain the type of the
 * carrier parameter to same).
 *
 * Most OpenTracing users will only reference the Format.Builtin constants. For example:
 *
 * <pre><code>
 * Tracer tracer = ...
 * io.opentracing.propagation.HttpHeaders httpCarrier = new AnHttpHeaderCarrier(httpRequest);
 * SpanContext spanCtx = tracer.extract(Format.Builtin.HTTP_HEADERS, httpCarrier);
 * </code></pre>
 *
 * @see Tracer#inject(SpanContext, Format, Object)
 * @see Tracer#extract(Format, Object)
 */
public interface Format<C> {
    final class Builtin<C> implements Format<C> {
        private final String name;

        private Builtin(String name) {
            this.name = name;
        }

        /**
         * The TEXT_MAP format allows for arbitrary String-&gt;String map encoding of SpanContext state for
         * Tracer.inject and Tracer.extract.
         *
         * Unlike HTTP_HEADERS, the builtin TEXT_MAP format expresses no constraints on keys or values.
         *
         * @see io.opentracing.Tracer#inject(SpanContext, Format, Object)
         * @see io.opentracing.Tracer#extract(Format, Object)
         * @see Format
         * @see Builtin#HTTP_HEADERS
         */
        public final static Format<TextMap> TEXT_MAP = new Builtin<TextMap>("TEXT_MAP");

        /**
         * The HTTP_HEADERS format allows for HTTP-header-compatible String-&gt;String map encoding of SpanContext state
         * for Tracer.inject and Tracer.extract.
         *
         * I.e., keys written to the TextMap MUST be suitable for HTTP header keys (which are poorly defined but
         * certainly restricted); and similarly for values (i.e., URL-escaped and "not too long").
         *
         * @see io.opentracing.Tracer#inject(SpanContext, Format, Object)
         * @see io.opentracing.Tracer#extract(Format, Object)
         * @see Format
         * @see Builtin#TEXT_MAP
         */
        public final static Format<TextMap> HTTP_HEADERS = new Builtin<TextMap>("HTTP_HEADERS");

        /**
         * The BINARY format allows for unconstrained binary encoding of SpanContext state for Tracer.inject and
         * Tracer.extract.
         *
         * @see io.opentracing.Tracer#inject(SpanContext, Format, Object)
         * @see io.opentracing.Tracer#extract(Format, Object)
         * @see Format
         */
        public final static Format<Binary> BINARY = new Builtin<Binary>("BINARY");

        /**
         * @return Short name for built-in formats as they tend to show up in exception messages.
         */
        @Override
        public String toString() {
            return Builtin.class.getSimpleName() + "." + name;
        }
    }
}
