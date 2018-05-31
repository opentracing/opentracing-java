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

import java.nio.ByteBuffer;

public final class BinaryAdapters {

    private BinaryAdapters() {}

    /**
     * Creates an inbound Binary instance used for extraction with the
     * specified ByteBuffer as input.
     *
     * @param buffer The ByteBuffer used as input.
     *
     * @return The new Binary carrier used for extraction.
     */
    public static Binary extractionCarrier(ByteBuffer buffer) {
        if (buffer == null) {
            throw new NullPointerException();
        }

        return new BinaryExtractAdapter(buffer);
    }

    /**
     * Creates an outbound Binary instance used for injection,
     * allocating a new ByteBuffer instance when
     * setInjectBufferLength() is called. The ByteBuffer can
     * be later retrieved using injectBuffer().
     *
     * @return The new Binary carrier used for injection.
     */
    public static Binary injectionCarrier() {
        return new BinaryInjectAdapter();
    }

    static class BinaryExtractAdapter implements Binary {
        ByteBuffer buffer;

        public BinaryExtractAdapter(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void setInjectionBufferLength(int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ByteBuffer injectionBuffer() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ByteBuffer extractionBuffer() {
            return buffer;
        }
    }

    static class BinaryInjectAdapter implements Binary {
        ByteBuffer buffer;

        @Override
        public void setInjectionBufferLength(int length) {
            if (length < 1) {
                throw new IllegalArgumentException("length needs to be larger than 0");
            }
            if (buffer != null) {
                throw new IllegalStateException("injectBuffer() length has already been set.");
            }

            buffer = ByteBuffer.allocate(length);
        }

        @Override
        public ByteBuffer injectionBuffer() {
            if (buffer == null) {
                throw new IllegalStateException("setInjectBufferLength() needs to be called before injectBuffer()");
            }

            return buffer;
        }

        @Override
        public ByteBuffer extractionBuffer() {
            throw new UnsupportedOperationException();
        }
    }
}
