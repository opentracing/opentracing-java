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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class TextMapExtractAdapterTest {

    @Test
    public void testIterator() {
        Map<String, String> headers = new LinkedHashMap<String, String>();
        headers.put("foo", "bar");
        TextMapExtractAdapter extractAdapter = new TextMapExtractAdapter(headers);

        Iterator<Entry<String, String>> iterator = extractAdapter.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("bar", iterator.next().getValue());
        assertFalse(iterator.hasNext());
    }
}
