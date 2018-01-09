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
import java.io.InputStream;
import java.io.OutputStream;

/**
 * BinaryAdapter is a built-in carrier for Tracer.inject() and Tracer.extract(). BinaryAdapter
 * is backed up by either an InputStream or an OutputStream, depending
 * on whether it's defined as injection or extraction, respectively.
 */
final class BinaryAdapter implements Binary {
    private final InputStream inputStream;
    private final OutputStream outputStream;

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

    InputStream inputStream() {
        return inputStream;
    }

    OutputStream outputStream() {
        return outputStream;
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (outputStream == null) {
            throw new UnsupportedOperationException();
        }

        outputStream.write(b, off, len);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (inputStream == null) {
            throw new UnsupportedOperationException();
        }

        return inputStream.read(b, off, len);
    }
}
