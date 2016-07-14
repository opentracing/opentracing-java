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
 * HttpHeaderReader is a built-in carrier for Tracer.inject().
 *
 * HttpHeaderReader implementations allows Tracers to write key:value String pairs into arbitrary HTTP header representations. In this way, HttpHeaderReader prevents a tight coupling between OpenTracing and any particular Java HTTP Header representation.
 *
 * @see io.opentracing.Tracer#extract(Object)
 */
public interface HttpHeaderReader {
    /**
     * Gets HTTP headers from the implementations backing store.
     *
     * Note that these headers will often be a superset of whatever was injected via an HttpHeaderWriter in the peer. As such, Tracers should use a unique prefix or substring to identify their header map entries.
     *
     * @return all entries in the HTTP header map; note that keys may appear multiple times (just as they may with HTTP headers)
     *
     * @see io.opentracing.Tracer#extract(Object)
     */
    Iterable<Map.Entry<String,String>> getEntries();
}
