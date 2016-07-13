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

import java.nio.ByteBuffer;
import io.opentracing.propagation.TextMapReader;
import io.opentracing.propagation.TextMapWriter;

/**
 * BuiltinFormats encapsulates the inject()/extract() carrier formats that all Tracer implementations must support.
 *
 * @see Format
 * @see io.opentracing.Tracer#inject(SpanContext, Format, Object)
 * @see io.opentracing.Tracer#extract(Format, Object)
 */
public class BuiltinFormats<R, W> implements Format<R, W> {
    /**
     * TEXT_MAP carriers support arbitrary string:string mapping with no special escaping needed on either keys or values.
     *
     * The backing store may (or may not) be shared with other non-OpenTracing writers and readers, so it's important for Tracer implementations to use a prefix to identify their own entries in the text map.
     */
    public final static Format<TextMapReader, TextMapWriter> TEXT_MAP = new BuiltinFormats<>();

    /**
     * HTTP_HEADER is much like TEXT_MAP, except that keys must be suitable for use as HTTP headers (i.e., must have a limited character set and must be retrieved in a case-insensitive manner) and values should be URL-escaped.
     */
    public final static Format<TextMapReader, TextMapWriter> HTTP_HEADER = new BuiltinFormats<>();

    /**
     * BINARY carriers represent SpanContext state as an opaque byte array controlled entirely by the Tracer implementation.
     */
    public final static Format<ByteBuffer, ByteBuffer> BINARY = new BuiltinFormats<>();
}
