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

import java.io.PrintWriter;
import java.io.StringWriter;
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
     * @param span the span on which the error should be tagged/logged
     * @param t throwable which will be included on the log entry including stacktrace
     * @param message message for the log entry.
     */
    public static void logException(Span span, Throwable t, String message) {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("error.kind", "Exception");
        fields.put("error.object", t);
        fields.put("event", "error");
        fields.put("message", message);
        fields.put("stack", getStacktraceAsString(t));

        span.setTag(Tags.ERROR.getKey(), true);
        span.log(fields);
    }

    private static String getStacktraceAsString(Throwable t) {
        StringWriter stackWriter = new StringWriter();
        t.printStackTrace(new PrintWriter(stackWriter));
        return stackWriter.toString();
    }
}
