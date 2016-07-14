/**
 * Copyright 2016 The OpenTracing Authors
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

import java.util.Map;

/**
 * TextMapReader is a built-in carrier for Tracer.extract(). TextMapReader implementations allows Tracers to read key:value String pairs from arbitrary underlying sources of data.
 *
 * @see io.opentracing.Tracer#extract(Object)
 */
public interface TextMapReader {
    /**
     * Gets an iterator over arbitrary key:value pairs from the TextMapReader.
     *
     * @return entries in the TextMapReader backing store
     *
     * @see io.opentracing.Tracer#extract(Object)
     * @see io.opentracing.propagation.HttpHeaderReader
     */
    Iterable<Map.Entry<String,String>> getEntries();
}
