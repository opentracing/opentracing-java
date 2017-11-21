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
package io.opentracing.v_030.propagation;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class TextMapInjectAdapterTest {

    @Test
    public void testPut() {
        Map<String, String> headers = new LinkedHashMap<String, String>();
        TextMapInjectAdapter injectAdapter = new TextMapInjectAdapter(headers);
        injectAdapter.put("foo", "bar");

        assertEquals("bar", headers.get("foo"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIterator() {
        Map<String, String> headers = new LinkedHashMap<String, String>();
        TextMapInjectAdapter injectAdapter = new TextMapInjectAdapter(headers);
        injectAdapter.iterator();
    }
}
