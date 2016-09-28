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
package io.opentracing.impl;

import io.opentracing.impl.AbstractSpan;
import io.opentracing.impl.AbstractSpanBuilder;


final class TestSpanBuilder extends AbstractSpanBuilder {

    public TestSpanBuilder(String operationName) {
        super(operationName);
    }

    @Override
    protected AbstractSpan createSpan() {
        return new AbstractSpan(operationName) {
            @Override
            public AbstractSpan setBaggageItem(String key, String value) {
                return this;
            }
        };
    }

    @Override
    boolean isTraceState(String key, Object value) {
        return false;
    }

    @Override
    AbstractSpanBuilder withStateItem(String key, Object value) {
        throw new AssertionError("no trace state is possible");
    }

}
