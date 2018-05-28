/*
 * Copyright The OpenTracing Authors
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

final class BinaryAdapters {

    private BinaryAdapters() {}

    public static class BinaryExtractAdapter implements Binary {
        ByteBuffer buffer;

        public BinaryExtractAdapter(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void setInjectBufferLength(int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ByteBuffer injectBuffer() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ByteBuffer extractBuffer() {
            return buffer;
        }
    }

    public static class BinaryInjectAdapter implements Binary {
        ByteBuffer buffer;

        @Override
        public void setInjectBufferLength(int length) {
            if (length < 1) {
                throw new IllegalArgumentException("length needs to be larger than 0");
            }
            if (buffer != null) {
                throw new IllegalStateException("injectBuffer() length has already been set.");
            }

            buffer = ByteBuffer.allocate(length);
        }

        @Override
        public ByteBuffer injectBuffer() {
            if (buffer == null) {
                throw new IllegalStateException("setInjectBufferLength() needs to be called before injectBuffer()");
            }

            return buffer;
        }

        @Override
        public ByteBuffer extractBuffer() {
            throw new UnsupportedOperationException();
        }
    }
}
