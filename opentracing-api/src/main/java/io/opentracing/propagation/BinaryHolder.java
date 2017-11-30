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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * BinaryHolder is a built-in carrier for Tracer.inject() and Tracer.extract(). BinaryHolder
 * can be created either for injection or for extraction.
 *
 * When BinaryHolder is created for injection, write() must be used to write data, and
 * it is an error to call read().
 *
 * When BinaryHolder is created for extraction, read() must be used to read data, and
 * it is an error to call write().
 *
 * Depending on whether the BinaryHolder was created for injection or extraction, it will internally
 * use either ByteArrayOutputStream or ByteArrayInputStream.
 *
 * @see Format.Builtin#BINARY
 * @see io.opentracing.Tracer#inject(SpanContext, Format, Object)
 * @see io.opentracing.Tracer#extract(Format, Object)
 */
public final class BinaryHolder {
    final ByteArrayInputStream inputStream;
    final ByteArrayOutputStream outputStream;

    private BinaryHolder(ByteArrayInputStream inputStream) {
        this.inputStream = inputStream;
        this.outputStream = null;
    }

    private BinaryHolder(ByteArrayOutputStream outputStream) {
        this.outputStream = outputStream;
        this.inputStream = null;
    }

    /**
     * Creates a BinaryHolder used for extraction with the
     * provided byte array as the input data.
     *
     * @param payload The byte array to wrap within the BinaryHolder.
     *
     * @see io.opentracing.Tracer#extract(Format, Object)
     */
    public static BinaryHolder createExtractHolder(byte[] payload) {
        if (payload == null)
            throw new IllegalArgumentException("payload");

        return new BinaryHolder(new ByteArrayInputStream(payload));
    }

    /**
     * Creates a BinaryHolder used for injection. The internal
     * buffer grows as data is written to it. The data can then be
     * retrieved using getInjectPayload().
     *
     * @see io.opentracing.Tracer#inject(SpanContext, Format, Object)
     */
    public static BinaryHolder createInjectHolder() {
        return new BinaryHolder(new ByteArrayOutputStream());
    }

    /**
     * @return whether this BinaryHolder was created for injection.
     */
    public boolean isInjectHolder() {
        return outputStream != null;
    }

    /**
     * @return whether this BinaryHolder was created for extraction.
     */
    public boolean isExtractHolder() {
        return inputStream != null;
    }

    /**
     * Writes b.length bytes from the specified byte array.
     *
     * @param b the data. 
     */
    public void write(byte[] b) throws IOException {
        if (outputStream == null)
            throw new UnsupportedOperationException();

        outputStream.write(b);
    }

    /**
     * Reads some number of bytes from the input stream.
     *
     * @param b the buffer into which the data is read.
     *
     * @return the total number of bytes read into the buffer, or -1 if there is no more data.
     */
    public int read(byte [] b) throws IOException {
        if (inputStream == null)
            throw new UnsupportedOperationException();

        return inputStream.read(b);
    }

    /**
     * @return A newly allocated byte array with the written data.
     */
    public byte[] getInjectPayload() {
        if (outputStream == null)
            throw new UnsupportedOperationException();

        return outputStream.toByteArray();
    }
}
