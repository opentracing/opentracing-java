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

import java.util.HashMap;
import java.util.Map;

/**
 * The "Logs" class/container standardizes and simplifies common @link{io.opentracing.Span#log(Map)} patterns.
 *
 * @see io.opentracing.Span#log(Map)
 */
public final class Logs {
    /**
     * EVENT_KEY identifies a String log value that serves as a stable identifier for an event in a Span's lifecycle.
     */
    public static final String EVENT_KEY = "event";

    /**
     * MESSAGE_KEY identifies a String log value that's human-readable and descriptive. Messages need not be stable.
     */
    public static final String MESSAGE_KEY = "message";

    /**
     * @param eventName the value for the "event" key in the log map
     * @return a Map suitable for use with @link{io.opentracing.Span#log(Map)}
     * @see Logs#EVENT_KEY
     */
    public static Map<String, ?> event(String eventName) {
        Map<String, String> rval = new HashMap<String, String>();
        rval.put(EVENT_KEY, eventName);
        return rval;
    }

    /**
     * @param messageText the value for the "message" key in the log map
     * @return a Map suitable for use with @link{io.opentracing.Span#log(Map)}
     * @see Logs#MESSAGE_KEY
     */
    public static Map<String, ?> message(String messageText) {
        Map<String, String> rval = new HashMap<String, String>();
        rval.put(MESSAGE_KEY, messageText);
        return rval;
    }
}
