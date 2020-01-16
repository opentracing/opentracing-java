/*
 * Copyright 2016-2020 The OpenTracing Authors
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BuiltinFormatTest {

    @Test
    public void test_HTTP_HEADERS_toString() {
        assertEquals("Builtin.HTTP_HEADERS", Format.Builtin.HTTP_HEADERS.toString());
    }

    @Test
    public void test_TEXT_MAP_toString() {
        assertEquals("Builtin.TEXT_MAP", Format.Builtin.TEXT_MAP.toString());
        assertEquals("Builtin.TEXT_MAP_INJECT", Format.Builtin.TEXT_MAP_INJECT.toString());
        assertEquals("Builtin.TEXT_MAP_EXTRACT", Format.Builtin.TEXT_MAP_EXTRACT.toString());
    }

    @Test
    public void test_BINARY_toString() {
        assertEquals("Builtin.BINARY", Format.Builtin.BINARY.toString());
        assertEquals("Builtin.BINARY_INJECT", Format.Builtin.BINARY_INJECT.toString());
        assertEquals("Builtin.BINARY_EXTRACT", Format.Builtin.BINARY_EXTRACT.toString());
    }

}
