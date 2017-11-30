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

/**
 * Binary is an interface defining the required operations for a binary carrier for
 * Tracer.inject() and Tracer.extract(). Binary can be defined either as inbound (extraction)
 * or outbound (injection).
 *
 * When Binary is defined as inbound, read() must be used to read data,
 * and it is an error to call read() or getOutboundPayload().
 *
 * When Binary is defined as outbound, write() must be used to write data,
 * getOutboundPayload() to retrieve all the written data, and it is an error to call read().
 *
 * @see Format.Builtin#BINARY
 * @see io.opentracing.Tracer#inject(SpanContext, Format, Object)
 * @see io.opentracing.Tracer#extract(Format, Object)
 */
public interface Binary {
    /**
     * @return whether this instance is inbound and supports reading.
     */
    boolean isInbound();

    /**
     * @return whether this instance is outbound and supports writing.
     */
    boolean isOutbound();

    /**
     * Writes b.length bytes from the specified byte array. The internal
     * buffer should grow as data is written to it.
     *
     * @param b the data.
     */
    void write(byte[] b) throws IOException;

    /**
     * Reads some number of bytes from the input data.
     *
     * @param b the buffer into which the data is read.
     *
     * @return the total number of bytes read into the buffer, or -1 if there is no more data.
     */
    int read(byte[] b) throws IOException;

    /**
     * @return A newly allocated byte array with the written data.
     */
    byte[] getOutboundData();
}
