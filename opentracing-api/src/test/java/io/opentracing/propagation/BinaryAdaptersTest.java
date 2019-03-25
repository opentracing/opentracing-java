/*
 * Copyright 2016-2019 The OpenTracing Authors
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
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BinaryAdaptersTest {

    @Test
    public void testExtractBinary() {
        ByteBuffer buff = ByteBuffer.wrap(new byte[0]);
        BinaryExtract binary = BinaryAdapters.extractionCarrier(buff);
        assertEquals(buff, binary.extractionBuffer());
    }

    @Test(expected = NullPointerException.class)
    public void testExtractBinaryNull() {
        BinaryAdapters.extractionCarrier(null);
    }

    @Test
    public void testInjectBinary() {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        BinaryInject binary = BinaryAdapters.injectionCarrier(buffer);
        assertEquals(buffer, binary.injectionBuffer(1));
        assertEquals(0, buffer.position());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInjectBinaryInvalidLength() {
        BinaryInject binary = BinaryAdapters.injectionCarrier(ByteBuffer.allocate(1));
        binary.injectionBuffer(0);
    }

    @Test(expected = AssertionError.class)
    public void testInjectBinaryLargerLength() {
        BinaryInject binary = BinaryAdapters.injectionCarrier(ByteBuffer.allocate(1));
        binary.injectionBuffer(2);
    }
}
