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
package io.opentracing.tag;

import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Pavol Loffay
 */
public class AbstractTagTest {

    @Test
    public void testSetTagOnSpan() {
        String value = "foo";
        String key = "bar";

        Span activeSpan = mock(Span.class);
        StringTag tag = new StringTag(key);
        tag.set(activeSpan, value);

        verify(activeSpan).setTag(key, value);
    }

    @Test
    public void testSetTagOnActiveSpan() {
        String value = "foo";
        String key = "bar";

        ActiveSpan activeSpan = mock(ActiveSpan.class);
        StringTag tag = new StringTag(key);
        tag.set(activeSpan, value);

        verify(activeSpan).setTag(key, value);
    }
}
