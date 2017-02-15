/**
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

import io.opentracing.SpanContext;

import java.nio.ByteBuffer;

/**
 * BinaryHolder is a built-in carrier for Tracer.inject() and Tracer.extract(). BinaryHolder allows inject to convert
 * the SpanContext to a byte encoding without a need to know the ByteBuffer size to allocate beforehand.
 *
 * @see io.opentracing.Tracer#inject(SpanContext, Format, Object)
 * @see io.opentracing.Tracer#extract(Format, Object)
 */
public final class BinaryHolder {
    private ByteBuffer carrier;

    /**
     * Creates a BinaryHolder with no initial size allocation.
     *
     * @see Format.Builtin#BINARY_HOLDER
     */
    public BinaryHolder() {
    }

    /**
     * Creates a BinaryHolder with provided ByteBuffer.
     * Useful for ensuring wrapped ByteBuffer has particular properties prior to Tracer.inject().
     *
     * @param carrier The ByteBuffer to wrap within the BinaryHolder.
     *
     * @see Format.Builtin#BINARY_HOLDER
     */
    public BinaryHolder(ByteBuffer carrier) {
        this.carrier = carrier;
    }

    /**
     * If no initial ByteBuffer was allocated for the BinaryHolder, the payload becomes the wrapped ByteBuffer.
     * Otherwise, the payload is placed in existent ByteBuffer.
     *
     * @param payload a ByteBuffer.
     */
    public void addPayload(ByteBuffer payload) {
        if (carrier == null) {
            carrier = payload;
        } else {
            carrier.put(payload);
        }
    }

    /**
     * Retrieve the wrapped ByteBuffer
     *
     * @return BinaryHolder's wrapped ByteBuffer; note that it is up to library implementation
     * to validate ByteBuffer's properties before committing additional operations.
     *
     * @see ByteBuffer#position()
     * @see ByteBuffer#limit()
     * @see ByteBuffer#capacity()
     */
    public ByteBuffer getCarrier() {
        return carrier;
    }
}
