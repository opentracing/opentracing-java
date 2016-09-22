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

import io.opentracing.Logs;
import io.opentracing.Span;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LogsTest {
    @Test
    public void testEvent() {
        Boolean value = true;
        Map<String, Object> expected = new HashMap<>();
        expected.put("event", "foo");
        Span span = mock(Span.class);
        span.log(Logs.event("foo"));
        verify(span).log(expected);
    }

    @Test
    public void testMessage() {
        Boolean value = true;
        Map<String, Object> expected = new HashMap<>();
        expected.put("message", "the quick brown fox");
        Span span = mock(Span.class);
        span.log(Logs.message("the quick brown fox"));
        verify(span).log(expected);
    }
}
