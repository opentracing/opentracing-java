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
package io.opentracing.impl;

import io.opentracing.propagation.BinaryHolder;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public final class TestBinaryHolder {

    private byte[] randomPayload(int size) {
        byte[] randomBytes = new byte[size];
        new Random().nextBytes(randomBytes);
        return randomBytes;
    }

    @Test(expected = BufferOverflowException.class)
    public void testBinaryHolderWithInsufficientCapacity() {
        System.out.println("binary holder with insufficient capacity");
        BinaryHolder binaryHolder = new BinaryHolder(ByteBuffer.allocate(100));
        ByteBuffer randomBytes51 = ByteBuffer.wrap(randomPayload(51));

        for (int i = 0; i < 2; i++) {
            binaryHolder.addPayload(randomBytes51);
            randomBytes51.rewind();
        }
    }

    @Test
    public void testBinaryHolderWithSufficientCapacity() {
        System.out.println("binary holder with sufficient capacity");
        ByteBuffer buffer = ByteBuffer.allocate(100);
        BinaryHolder binaryHolder = new BinaryHolder(buffer);
        byte[] randomBytes51 = randomPayload(51);
        binaryHolder.addPayload(ByteBuffer.wrap(randomBytes51));

        ByteBuffer carrier = binaryHolder.getPayload();
        byte[] actual = new byte[51];
        carrier.rewind();
        carrier.get(actual);

        assertArrayEquals(randomBytes51, actual);
    }

    @Test
    public void testVariableBinaryHolder() {
        System.out.println("variable binary holder");
        BinaryHolder binaryHolder = new BinaryHolder();
        ByteBuffer randomBytes51 = ByteBuffer.wrap(randomPayload(51));
        binaryHolder.addPayload(randomBytes51);
        assertEquals(randomBytes51, binaryHolder.getPayload());
    }
}
