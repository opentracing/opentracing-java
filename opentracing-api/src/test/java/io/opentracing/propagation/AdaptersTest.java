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
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AdaptersTest {

    @Test
    public void testExtractBinary() {
        ByteBuffer buff = ByteBuffer.wrap(new byte[0]);
        Binary binary = Adapters.extractBinary(buff);
        assertEquals(buff, binary.extractBuffer());
    }

    @Test(expected = NullPointerException.class)
    public void testExtractBinaryNull() {
        Adapters.extractBinary(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testExtractBinaryInjectBuffer() {
        Binary binary = Adapters.extractBinary(ByteBuffer.allocate(1));
        binary.injectBuffer();
    }

    @Test
    public void testInjectBinary() {
        Binary binary = Adapters.injectBinary();
        binary.setInjectBufferLength(1);
        assertNotNull(binary.injectBuffer());
        assertEquals(0, binary.injectBuffer().position());
        assertEquals(1, binary.injectBuffer().capacity());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInjectBinaryInvalidLength() {
        Binary binary = Adapters.injectBinary();
        binary.setInjectBufferLength(0);
    }

    @Test(expected = IllegalStateException.class)
    public void testInjectBinaryNoLength() {
        Binary binary = Adapters.injectBinary();
        binary.injectBuffer();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testInjectBinaryExtractBuffer() {
        Binary binary = Adapters.injectBinary();
        binary.extractBuffer();
    }
}
