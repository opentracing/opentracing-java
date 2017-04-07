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
package io.opentracing.mock;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class Sandbox {

    @Test
    public void test() {
        MockTracer mockTracer = new MockTracer();
        MockSpan root = mockTracer.buildSpan("root").start();
        Assert.assertTrue(mockTracer.spanManager().active() == null);

        root.visibility().activate();
        Assert.assertEquals(root, mockTracer.spanManager().active());

        MockSpan child = mockTracer.buildSpan("child").start();

        child.finish();
        root.finish();

        Assert.assertEquals(root.context().spanId(), child.parentId());
    }
}
