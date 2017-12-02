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
 * BinaryAdapter is a built-in carrier for Tracer.inject() and Tracer.extract(). BinaryAdapter
 * is backed up by either a ByteArrayInputStream or a ByteArrayOutputStream, depending
 * on whether it's defined as inbound or outbound, respectively.
 */
final class BinaryAdapter implements Binary {
    final ByteArrayInputStream inputStream;
    final ByteArrayOutputStream outputStream;

    /**
     * Create an outbound BinaryAdapter.
     */
    BinaryAdapter() {
        this.outputStream = new ByteArrayOutputStream();
        this.inputStream = null;
    }

    /**
     * Create an inbound BinaryAdapter backed by the specified byte array.
     */
    BinaryAdapter(byte[] b) {
        this.inputStream = new ByteArrayInputStream(b);
        this.outputStream = null;
    }

    public void write(byte[] b) throws IOException {
        if (outputStream == null)
            throw new UnsupportedOperationException();

        outputStream.write(b);
    }

    public int read(byte [] b) throws IOException {
        if (inputStream == null)
            throw new UnsupportedOperationException();

        return inputStream.read(b);
    }

    public byte[] getOutboundData() {
        if (outputStream == null)
            throw new UnsupportedOperationException();

        return outputStream.toByteArray();
    }
}
