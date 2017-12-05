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
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * BinaryAdapter is a built-in carrier for Tracer.inject() and Tracer.extract(). BinaryAdapter
 * is backed up by either a ReadableByteChannel or a WritableByteChannel, depending
 * on whether it's defined as injection or extraction, respectively.
 */
final class BinaryAdapter implements Binary {
    private final ReadableByteChannel readChannel;
    private final WritableByteChannel writeChannel;

    /**
     * Create an outbound BinaryAdapter backed by the specified write channel.
     */
    BinaryAdapter(WritableByteChannel writeChannel) {
        this.writeChannel = writeChannel;
        this.readChannel = null;
    }

    /**
     * Create an inbound BinaryAdapter backed by the specified read channel.
     */
    BinaryAdapter(ReadableByteChannel readChannel) {
        this.readChannel = readChannel;
        this.writeChannel = null;
    }

    ReadableByteChannel readChannel() {
        return readChannel;
    }

    WritableByteChannel writeChannel() {
        return writeChannel;
    }

    public int write(ByteBuffer buffer) throws IOException {
        if (writeChannel == null)
            throw new UnsupportedOperationException();

        return writeChannel.write(buffer);
    }

    public int read(ByteBuffer buffer) throws IOException {
        if (readChannel == null)
            throw new UnsupportedOperationException();

        return readChannel.read(buffer);
    }
}
