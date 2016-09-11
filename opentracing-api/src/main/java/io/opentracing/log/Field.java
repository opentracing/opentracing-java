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
package io.opentracing.log;

/**
 * Field represents a single key:value pair in a Span.log(...) call.
 *
 * The key must always be a String. All Tracer implementations must support bool, numeric, and String values; some may
 * also support arbitrary Object values.
 */
public class Field<V> {
    private final String key;
    private final V value;

    /**
     * Instantiate an (immutable) Field of value type V
     *
     * @param key the Field's key; an unrestricted String
     * @param value the Field's value; all Tracer implementations should support String, numeric, and bool values.
     *              Some may also support Object values.
     * @param <V> the value type for the returned Field instance
     * @return a Field suitable for Span.log(...) invocations
     */
    public static <V> Field<V> of(String key, V value) {
        return new Field(key, value);
    }

    public String key() { return key; }
    public V value() { return value; }

    Field(String key, V value) {
        this.key = key;
        this.value = value;
    }
}
