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

public final class Adapters {
    private Adapters() {}

    /**
     * Creates an outbound Binary instance used for injection,
     * allocating a new ByteBuffer instance when
     * setInjectBufferLength() is called. The ByteBuffer can
     * be later retrieved using injectBuffer().
     *
     * @return The new Binary carrier used for injection.
     */
    public static Binary injectBinary() {
        return new BinaryAdapters.BinaryInjectAdapter();
    }

    /**
     * Creates an inbound Binary instance used for extraction with the
     * specified ByteBuffer as input.
     *
     * @param buffer The ByteBuffer used as input.
     *
     * @return The new Binary carrier used for extraction.
     */
    public static Binary extractBinary(ByteBuffer buffer) {
        if (buffer == null) {
            throw new NullPointerException();
        }

        return new BinaryAdapters.BinaryExtractAdapter(buffer);
    }
}
