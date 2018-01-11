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
     * Writes len bytes from the specified byte array starting at offset off to this carrier.
     *
     * The behavior of this method is expected to be the same as OutputStream.write().
     *
     * @param b the data.
     * @param off the start offset of the data.
     * @param len the number of bytes to write.
     */
    void write(byte[] b, int off, int len) throws IOException;

    /**
     * Reads up to len bytes of data from the carrier into an array of bytes.
     *
     * The behavior of this method is expected to be the same as InputStream.read().
     *
     * @param b the data.
     * @param off the start offset of the data.
     * @param len the number of bytes to read.
     *
     * @return the total number of bytes read into the buffer, or -1 if there is no more data
     * because the end of the carrier has been reached.
     */
    int read(byte[] b, int offset, int len) throws IOException;
}
