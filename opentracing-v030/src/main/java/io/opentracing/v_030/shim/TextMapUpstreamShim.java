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
package io.opentracing.v_030.shim;

import io.opentracing.v_030.propagation.TextMap;

import java.util.Iterator;
import java.util.Map;

class TextMapUpstreamShim implements io.opentracing.propagation.TextMap {
    final TextMap textMap;

    public TextMapUpstreamShim(TextMap textMap) {
        this.textMap = textMap;
    }

    public TextMap textMap() {
        return textMap;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return textMap.iterator();
    }

    @Override
    public void put(String key, String value) {
        textMap.put(key, value);
    }
}
