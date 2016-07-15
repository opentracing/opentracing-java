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

/**
 * HttpHeaderWriter is a built-in carrier for Tracer.inject().
 *
 * HttpHeaderWriter implementations allows Tracers to write key:value String pairs into arbitrary HTTP header representations. In this way, HttpHeaderWriter prevents a tight coupling between OpenTracing and any particular Java HTTP Header representation.
 *
 * @see io.opentracing.Tracer#inject(io.opentracing.SpanContext, Object)
 */
public interface HttpHeaderWriter {
    /**
     * Puts a key:value pair into the HTTP header map.
     *
     * Note that headers added via put() will often share the HTTP header map with other application data. As such, Tracers should use a unique prefix or substring to identify their header map entries.
     *
     * @param key a key suitable for use in an HTTP header (i.e., case-insensitive, no special characters, etc)
     * @param value a value suitable for use in an HTTP header (i.e., URL-escaped)
     *
     * @see io.opentracing.Tracer#inject(io.opentracing.SpanContext, Object)
     */
    void put(String key, String value);
}
