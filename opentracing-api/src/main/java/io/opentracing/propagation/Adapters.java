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

public final class Adapters {
    private Adapters() {}

    /**
     * Creates an outbound Binary instance used for injection, backed up
     * by a byte buffer.
     */
    public static Binary outboundBinary() {
        return new BinaryAdapter();
    }

    /**
     * Creates an inbound Binary instance used for extraction with the
     * provided byte array as the input data.
     *
     * @param payload The byte array to wrap within the Binary instance.
     */
    public static Binary inboundBinary(byte[] b) {
        if (b == null)
            throw new IllegalArgumentException("b");

        return new BinaryAdapter(b);
    }
}
