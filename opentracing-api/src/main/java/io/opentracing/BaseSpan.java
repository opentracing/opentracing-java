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
package io.opentracing;

import java.util.Map;

/**
 * BaseSpan is carried over from 0.30.0 API to preserve backwards compatibility with instrumentation
 * that was compiled against v0.30.0. Even if that instrumentation does not refer to BaseSpan directly,
 * it is still required at runtime because most of the Span methods in v0.30.0 were inherited from BaseSpan.
 * 
 * Keeping this interface around allows for easier transition to v0.31.0.
 * 
 * @see Span
 * @deprecated since v0.31.0
 */
public interface BaseSpan {
    // SpanContext context();
    // S setTag(String key, String value);
    // S setTag(String key, boolean value);
    // S setTag(String key, Number value);
    // S log(Map<String, ?> fields);
    // S log(long timestampMicroseconds, Map<String, ?> fields);
    // S log(String event);
    // S log(long timestampMicroseconds, String event);
    // S setBaggageItem(String key, String value);
    // String getBaggageItem(String key);
    // S setOperationName(String operationName);
}
