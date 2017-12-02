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
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class BinaryAdapterTest {

    @Test
    public void outboundTest() throws IOException {
        Binary binary = Adapters.outboundBinary();

        binary.write(new byte [] { 1, 2, 3, 4 });
        binary.write(new byte [] { 4, 3, 2, 1 });

        assertArrayEquals(new byte[] { 1, 2, 3, 4, 4, 3, 2, 1 }, binary.getOutboundData());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getOutboundDataUnsupportedTest () {
        Binary binary = Adapters.inboundBinary(new byte[4]);
        binary.getOutboundData();
    }

    @Test
    public void inboundTest() throws IOException {
        byte[] ctx = new byte[] { 1, 2, 3, 4, 4, 3, 2, 1 };
        Binary binary = Adapters.inboundBinary(ctx);

        byte[] buff = new byte[4];

        assertEquals(4, binary.read(buff));
        assertArrayEquals(new byte[] { 1, 2, 3, 4 }, buff);

        assertEquals(4, binary.read(buff));
        assertArrayEquals(new byte[] { 4, 3, 2, 1 }, buff);

        assertEquals(-1, binary.read(buff));
    }

    @Test(expected = IllegalArgumentException.class)
    public void inboundNullArrayTest() throws IOException {
        Adapters.inboundBinary(null);
    }
}
