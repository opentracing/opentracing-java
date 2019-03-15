/*
 * Copyright 2016-2019 The OpenTracing Authors
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

/**
 * @deprecated use {@link ThreadLocalScopeManager} instead.
 * The operation mode of this class contrasts with the 0.32
 * deprecation of auto finishing {@link Span}s upon {@link Scope#close()}.
 * See https://github.com/opentracing/opentracing-java/issues/291
 *
 * A {@link ScopeManager} implementation that uses ref-counting to automatically finish {@link Span}s.
 *
 * @see AutoFinishScope
 */
@Deprecated
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

    public AutoFinishScope.Continuation captureScope() {
        AutoFinishScope scope = tlsScope.get();
        return scope == null ? null : scope.capture();
    }
}
