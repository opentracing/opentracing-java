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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AdaptersTest {

    @Test
    public void testInboundBinaryStream() {
        byte[] ctx = new byte[0];
        BinaryAdapter binary = (BinaryAdapter) Adapters.inboundBinary(new ByteArrayInputStream(ctx));
        assertNotNull(binary.readChannel());
    }

    @Test
    public void testInboundBinaryChannel() {
        byte[] ctx = new byte[0];
        ReadableByteChannel channel = Channels.newChannel(new ByteArrayInputStream(new byte[0]));
        BinaryAdapter binary = (BinaryAdapter) Adapters.inboundBinary(channel);
        assertEquals(channel, binary.readChannel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInboundNullStream() {
        Adapters.inboundBinary((InputStream)null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInboundNullChannel() {
        Adapters.inboundBinary((ReadableByteChannel)null);
    }

    @Test
    public void testOutboundBinaryStream() {
        BinaryAdapter binary = (BinaryAdapter) Adapters.outboundBinary(new ByteArrayOutputStream());
        assertNotNull(binary.writeChannel());
    }

    @Test
    public void testOutboundBinaryChannel() {
        WritableByteChannel channel = Channels.newChannel(new ByteArrayOutputStream());
        BinaryAdapter binary = (BinaryAdapter) Adapters.outboundBinary(channel);
        assertEquals(channel, binary.writeChannel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutboundNullStream() {
        Adapters.outboundBinary((OutputStream)null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutboundNullChannel() {
        Adapters.outboundBinary((WritableByteChannel)null);
    }
}
