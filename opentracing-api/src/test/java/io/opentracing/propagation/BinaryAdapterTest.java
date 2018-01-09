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

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class BinaryAdapterTest {

    @Test
    public void testRead() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 4, 3, 2, 1 });
        BinaryAdapter binary = new BinaryAdapter(Channels.newChannel(stream));
        assertNotNull(binary.readChannel());
        assertNull(binary.writeChannel());

        ByteBuffer buffer = ByteBuffer.allocate(4);
        assertEquals(4, binary.read(buffer));
        assertArrayEquals(new byte[] { 1, 2, 3, 4 }, buffer.array());

        buffer.rewind();
        assertEquals(4, binary.read(buffer));
        assertArrayEquals(new byte[] { 4, 3, 2, 1 }, buffer.array());

        buffer.rewind();
        assertEquals(-1, binary.read(buffer));
    }

    @Test
    public void testWrite() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BinaryAdapter binary = new BinaryAdapter(Channels.newChannel(stream));
        assertNotNull(binary.writeChannel());
        assertNull(binary.readChannel());

        assertEquals(4, binary.write(ByteBuffer.wrap(new byte [] { 1, 2, 3, 4 })));
        assertEquals(4, binary.write(ByteBuffer.wrap(new byte [] { 4, 3, 2, 1 })));

        assertArrayEquals(new byte[] { 1, 2, 3, 4, 4, 3, 2, 1 }, stream.toByteArray());
    }
}
