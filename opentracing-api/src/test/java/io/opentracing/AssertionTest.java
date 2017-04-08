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

package io.opentracing;

import org.junit.Test;

public final class AssertionTest {

    @Test
    public void test_assertions_enabled() {
        boolean asserted = false;
        try {
            assert false;
        } catch (AssertionError error) {
            asserted = true;
        }
        if (!asserted) {
            throw new AssertionError("assertions are not enabled");
        }

        // XXX: We need to remove the bizarre inheritance relationship between SpanBuilder and SpanContext
        return null != marker ? new TestSpanBuilder(marker, null) : NoopSpanBuilder.INSTANCE;
    }
}
