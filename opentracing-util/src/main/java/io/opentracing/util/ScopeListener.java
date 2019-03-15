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

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

/**
 * Listener that can react on changes of currently active {@link Span}.
 * <p>
 * The {@link #onActivated} method will be called, whenever scope changes - that can be both
 * as result of a {@link ScopeManager#activate(Span, boolean)} call or when {@link Scope#close()}
 * is closed on a nested scope.
 * <p>
 * {@link #onClosed} is called when closing outermost scope - meaning no scope is currently active.
 *
 * @see ThreadLocalScopeManager
 */
public interface ScopeListener {

    /**
     * Called whenever a scope was activated (changed).
     *
     * @param span Activated span. Never null.
     */
    void onActivated(Span span);

    /**
     * Called when outermost scope was deactivated.
     */
    void onClosed();
}
