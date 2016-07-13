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
package io.opentracing;

/**
 * Created by bhs on 7/12/16.
 */
public interface SpanContext {
    /**
     * Set a Baggage item, represented as a simple string:string pair.
     *
     * Note that newly-set Baggage items are only guaranteed to propagate to future children of the given Span.
     */
    SpanContext setBaggageItem(String key, String value);

    /**
     * Get a Baggage item by key.
     *
     * Returns null if no entry found, or baggage is not supported in the current implementation.
     */
    String getBaggageItem(String key);
}
