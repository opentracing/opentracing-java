/**
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
package io.opentracing.util;

import io.opentracing.Span;
import io.opentracing.tag.Tags;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides convenience methods for logging and tagging spans.
 *
 * Follows conventions defined in https://github.com/opentracing/specification/blob/master/semantic_conventions.md
 */
public class SpanUtils {
    private SpanUtils() {
    }

    /**
     * Tags the span as error=true and logs the occurrence of any Throwable (including all appropriate fields).
     *
     * This is a shortcut for {@link SpanUtils#logException(Span, Throwable, String) logException(Span, Throwable, null)}.
     * If there is additional context you can provide for the error, consider calling that method and including that context
     * in the message argument.
     *
     * @param span the span to which the tag/log should be applied.
     * @param t throwable which will be included on the log entry
     */
    public static void logException(Span span, Throwable t) {
        logException(span, t, null);
    }

    /**
     * Tags the span as error=true and logs the occurrence of any Throwable (including all appropriate fields).
     *
     * Note: the semantic conventions also specify a field called "stack". This will not be populated here (due
     * to performance concerns), but the Tracer can choose to do so based on the value we store in "error.object".
     *
     * @param span the span to which the tag/log should be applied.
     * @param t throwable which will be included on the log entry
     * @param message message for the log entry. This will be concatenated with (and therefore should not be the
     *                same as) the exception message. Null- and empty-tolerant.
     */
    public static void logException(Span span, Throwable t, String message) {
        if (span == null) {
            throw new IllegalArgumentException("Span must not be null.");
        }
        if (t == null) {
            throw new IllegalArgumentException("Throwable must not be null.");
        }
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("event", "error");
        if (message == null || message.trim().isEmpty()) {
            fields.put("message", t.getMessage());
        } else {
            fields.put("message", String.format("%s | %s", message, t.getMessage()));
        }
        fields.put("error.kind", t.getClass().getName());
        fields.put("error.object", t);

        span.setTag(Tags.ERROR.getKey(), true);
        span.log(fields);
    }
}
