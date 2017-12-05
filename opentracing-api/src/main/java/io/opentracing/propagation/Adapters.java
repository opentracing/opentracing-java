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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public final class Adapters {
    private Adapters() {}

    /**
     * Creates an outbound Binary instance used for injection, backed up
     * by the specified OutputStream as output.
     *
     * @param stream The OutputStream used as the output.
     *
     * @return The new Binary carrier used for injection.
     */
    public static Binary outboundBinary(OutputStream stream) {
        if (stream == null)
            throw new IllegalArgumentException("stream");

        return new BinaryAdapter(Channels.newChannel(stream));
    }

    /**
     * Creates an outbound Binary instance used for injection, backed up
     * by the specified WritableByteChannel as the output.
     *
     * @param channel The WritableByteChannel used as output.
     *
     * @return The new Binary carrier used for injection.
     */
    public static Binary outboundBinary(WritableByteChannel channel) {
        if (channel == null)
            throw new IllegalArgumentException("channel");

        return new BinaryAdapter(channel);
    }

    /**
     * Creates an inbound Binary instance used for extraction with the
     * specified InputStream as the input.
     *
     * @param stream The InputStream used as input.
     *
     * @return The new Binary carrier used for extraction.
     */
    public static Binary inboundBinary(InputStream stream) {
        if (stream == null)
            throw new IllegalArgumentException("stream");

        return new BinaryAdapter(Channels.newChannel(stream));
    }

    /**
     * Creates an inbound Binary instance used for extraction with the
     * specified ReadableByteChannel as the input.
     *
     * @param channel The ReadableByteChannel used as input.
     *
     * @return The new Binary carrier used for extraction.
     */
    public static Binary inboundBinary(ReadableByteChannel channel) {
        if (channel == null)
            throw new IllegalArgumentException("channel");

        return new BinaryAdapter(channel);
    }
}
