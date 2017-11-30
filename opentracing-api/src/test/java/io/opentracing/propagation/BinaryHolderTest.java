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

public class BinaryHolderTest {

    @Test
    public void createInjectHolderTest() throws IOException {
        BinaryHolder holder = BinaryHolder.createInjectHolder();
        assertEquals(false, holder.isExtractHolder());
        assertEquals(true, holder.isInjectHolder());

        holder.write(new byte [] { 1, 2, 3, 4 });
        holder.write(new byte [] { 4, 3, 2, 1 });

        assertArrayEquals(new byte[] { 1, 2, 3, 4, 4, 3, 2, 1 }, holder.getInjectPayload());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getInjectPayloadUnsupportedTest () {
        BinaryHolder holder = BinaryHolder.createExtractHolder(new byte[4]);
        holder.getInjectPayload();
    }

    @Test
    public void createExtractHolderTest() throws IOException {
        byte[] ctx = new byte[] { 1, 2, 3, 4, 4, 3, 2, 1 };
        BinaryHolder holder = BinaryHolder.createExtractHolder(ctx);
        assertEquals(true, holder.isExtractHolder());
        assertEquals(false, holder.isInjectHolder());

        byte[] buff = new byte[4];

        assertEquals(4, holder.read(buff));
        assertArrayEquals(new byte[] { 1, 2, 3, 4 }, buff);

        assertEquals(4, holder.read(buff));
        assertArrayEquals(new byte[] { 4, 3, 2, 1 }, buff);

        assertEquals(-1, holder.read(buff));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createExtractHolderNullArrayTest() throws IOException {
        BinaryHolder.createExtractHolder(null);
    }
}
