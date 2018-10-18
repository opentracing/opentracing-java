/*
 * Copyright 2016-2018 The OpenTracing Authors
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

import io.opentracing.ScopeManager;
import io.opentracing.Span;

import java.util.concurrent.atomic.AtomicInteger;

public class AutoFinishScopeManager implements ScopeManager {
    final ThreadLocal<AutoFinishScope> tlsScope = new ThreadLocal<AutoFinishScope>();

    @Override
    public AutoFinishScope activate(Span span, boolean finishOnClose) {
        return new AutoFinishScope(this, new AtomicInteger(1), span);
    }

    @Override
    public AutoFinishScope activate(Span span) {
        return new AutoFinishScope(this, new AtomicInteger(1), span);
    }

    @Override
    public AutoFinishScope active() {
        return tlsScope.get();
    }

    @Override
    public Span activeSpan() {
        AutoFinishScope scope = tlsScope.get();
        return scope == null ? null : scope.span();
    }

    @Override
    public void clear() {
        // Set a null value instead of calling remove(),
        // to prevent unnecessary allocation of new ThreadLocal-related objects.
        tlsScope.set(null);
    }
}
