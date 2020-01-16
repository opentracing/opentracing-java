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
import java.util.Iterator;
import java.util.Map;

/**
 * {@link TextMapExtract} is a built-in carrier for {@link Tracer#extract} only.
 * {@link TextMapExtract} implementations allows Tracers to write key:value String
 * pairs to arbitrary underlying sources of data.
 *
 * @see io.opentracing.Tracer#extract(Format, Object)
 */
public interface TextMapExtract extends Iterable<Map.Entry<String, String>> {
    /**
     * Gets an iterator over arbitrary key:value pairs from the TextMapReader.
     *
     * @return entries in the TextMap backing store; note that for some Formats, the iterator may include entries that
     * were never injected by a Tracer implementation (e.g., unrelated HTTP headers)
     *
     * @see io.opentracing.Tracer#extract(Format, Object)
     * @see Format.Builtin#TEXT_MAP
     * @see Format.Builtin#HTTP_HEADERS
     */
    Iterator<Map.Entry<String,String>> iterator();
}
