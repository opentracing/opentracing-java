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
package io.opentracing.propagation;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Binary is an interface defining the required operations for a binary carrier for
 * Tracer.inject() and Tracer.extract(). Binary can be defined either as inbound (extraction)
 * or outbound (injection).
 *
 * When Binary is defined as inbound, read() must be used to read data,
 * and it is an error to call read().
 *
 * When Binary is defined as outbound, write() must be used to write data,
 * and it is an error to call write().
 *
 * @see Format.Builtin#BINARY
 * @see io.opentracing.Tracer#inject(SpanContext, Format, Object)
 * @see io.opentracing.Tracer#extract(Format, Object)
 */
public interface Binary {
    /**
     * Writes a sequence of bytes to this channel from the given buffer.
     * The internal buffer is expected to grow as more data is written.
     *
     * The behavior of this method is expected to be the same as WritableByteChannel.write().
     *
     * @param buffer The buffer from which bytes are to be retrieved.
     *
     * @return The number of bytes written, possibly zero.
     */
    int write(ByteBuffer buffer) throws IOException;

    /**
     * Reads a sequence of bytes into the given buffer.
     *
     * The behavior of this method is expected to be the same as ReadableByteChannel.read().
     *
     * @param buffer The buffer into which bytes are to be transferred.
     *
     * @return The number of bytes read, possibly zero, or -1 if the channel has reached end-of-stream.
     */
    int read(ByteBuffer buffer) throws IOException;
}
