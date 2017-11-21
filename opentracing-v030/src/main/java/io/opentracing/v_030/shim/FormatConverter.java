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
package io.opentracing.v_030.shim;

import io.opentracing.v_030.SpanContext;
import io.opentracing.v_030.Tracer;
import io.opentracing.v_030.propagation.Format;
import io.opentracing.v_030.propagation.TextMap;

final class FormatConverter {
    private FormatConverter() {}

    public static io.opentracing.propagation.Format toUpstreamFormat(Format format) {
        if (format == null)
            return null; // Bail out early.

        if (format == Format.Builtin.TEXT_MAP)
            return io.opentracing.propagation.Format.Builtin.TEXT_MAP;
        if (format == Format.Builtin.HTTP_HEADERS)
            return io.opentracing.propagation.Format.Builtin.HTTP_HEADERS;
        if (format == Format.Builtin.BINARY)
            return io.opentracing.propagation.Format.Builtin.BINARY;

        throw new UnsupportedOperationException("Format not supported");
    }

    public static <C> Object toUpstreamCarrier(Format format, C carrier) {
        if (format == Format.Builtin.TEXT_MAP || format == Format.Builtin.HTTP_HEADERS)
            return new TextMapUpstreamShim((TextMap)carrier);

        return carrier;
    }
}
