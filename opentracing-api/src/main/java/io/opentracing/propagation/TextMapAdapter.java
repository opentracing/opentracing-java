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

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import java.util.Map;

/**
 * A {@link TextMap} carrier for use with {@link Tracer#inject} and {@link Tracer#extract}.
 *
 * @see Tracer#inject(SpanContext, Format, Object)
 * @see Tracer#extract(Format, Object)
 */
public class TextMapAdapter extends TextMapExtractAdapter implements TextMap {
    public TextMapAdapter(Map<String, String> map) {
        super(map);
    }

    @Override
    public void put(String key, String value) {
        map.put(key, value);
    }
}
