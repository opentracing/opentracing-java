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
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * BinaryAdapter is a built-in carrier for Tracer.inject() and Tracer.extract(). BinaryAdapter
 * is backed up by either an OutputStream or an InputStream, depending
 * on whether it's defined as injection or extraction, respectively.
 */
final class BinaryAdapter implements Binary {
    private final OutputStream outputStream;
    private final InputStream inputStream;

    /**
     * Create an outbound BinaryAdapter backed by the specified OutputStream.
     */
    BinaryAdapter(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.inputStream = null;
    }

    /**
     * Create an inbound BinaryAdapter backed by the specified InputStream.
     */
    BinaryAdapter(InputStream inputStream) {
        this.inputStream = inputStream;
        this.outputStream = null;
    }

    OutputStream outputStream() {
        return outputStream;
    }

    InputStream inputStream() {
        return inputStream;
    }

    public void write(ByteBuffer buffer) throws IOException {
        if (buffer == null) {
            throw new NullPointerException();
        }
        if (outputStream == null) {
            throw new UnsupportedOperationException();
        }
        if (buffer.remaining() == 0) { // No data to write.
            return;
        }

        byte[] b = new byte[buffer.remaining()];
        buffer.get(b);
        outputStream.write(b);
    }

    public int read(ByteBuffer buffer) throws IOException {
        if (buffer == null) {
            throw new NullPointerException();
        }
        if (inputStream == null) {
            throw new UnsupportedOperationException();
        }

        // Need to read always as:
        // 1. InputStream.available() always returns 0 for some implementations.
        // 2. Even buffer.remaining() == 0, we need to know whether we have reached
        //    the end of the stream or not.
        byte[] b = new byte[buffer.remaining()];
        int available = inputStream.read(b);
        if (available < 0)
            return -1;

        buffer.put(b, 0, available);
        return available;
    }
}
