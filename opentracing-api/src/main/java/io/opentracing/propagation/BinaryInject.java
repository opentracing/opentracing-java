/*
 * Copyright 2016-2019 The OpenTracing Authors
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
 * {@link BinaryInject} is an interface defining the required operations for a binary carrier for
 * {@link Tracer#inject} only. {@link BinaryInject} is defined as outbound (injection).
 *
 * When called with {@link Tracer#inject}, {@link #injectionBuffer} will be called
 * to retrieve the actual {@link ByteBuffer} used for the {@link SpanContext} injection.
 *
 * @see Format.Builtin#BINARY
 * @see io.opentracing.Tracer#inject(SpanContext, Format, Object)
 */
public interface BinaryInject {
    /**
     * Gets the buffer used to store data as part of {@link SpanContext} injection.
     *
     * The lenght parameter hints the buffer length required for
     * {@link SpanContext} injection. The user may use this to allocate a new
     * ByteBuffer or resize an existing one.
     *
     * It is an error to call this method when Binary is used
     * for {@link SpanContext} extraction.
     *
     * @param length The buffer length required for {@link SpanContext} injection.
     *               It needs to be larger than zero.
     *
     * @return The buffer used for {@link SpanContext} injection.
     */
    ByteBuffer injectionBuffer(int length);
}
