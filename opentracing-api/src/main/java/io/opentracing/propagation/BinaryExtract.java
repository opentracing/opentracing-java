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
 * {@link BinaryExtract} is an interface defining the required operations for a binary carrier for
 * {@link Tracer#extract} only. {@link BinaryExtract} is defined as inbound (extraction).
 *
 * When called with {@link Tracer#extract}, {@link #extractionBuffer} will be called to retrieve the {@link ByteBuffer}
 * containing the data used for {@link SpanContext} extraction.
 *
 * @see Format.Builtin#BINARY
 * @see io.opentracing.Tracer#extract(Format, Object)
 */
public interface BinaryExtract {

    /**
     * Gets the buffer containing the data used for {@link SpanContext} extraction.
     *
     * It is an error to call this method when Binary is used
     * for {@link SpanContext} injection.
     *
     * @return The buffer used for {@link SpanContext} extraction.
     */
    ByteBuffer extractionBuffer();
}
