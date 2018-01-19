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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AdaptersTest {

    @Test
    public void testExtractBinaryStream() {
        byte[] ctx = new byte[0];
        BinaryAdapter binary = (BinaryAdapter) Adapters.extractBinary(new ByteArrayInputStream(ctx));
        assertNotNull(binary.inputStream());
    }

    @Test(expected = NullPointerException.class)
    public void testExtractNullStream() {
        Adapters.extractBinary((InputStream)null);
    }

    @Test
    public void testInjectBinaryStream() {
        BinaryAdapter binary = (BinaryAdapter) Adapters.injectBinary(new ByteArrayOutputStream());
        assertNotNull(binary.outputStream());
    }

    @Test(expected = NullPointerException.class)
    public void testInjectNullStream() {
        Adapters.injectBinary((OutputStream)null);
    }
}
