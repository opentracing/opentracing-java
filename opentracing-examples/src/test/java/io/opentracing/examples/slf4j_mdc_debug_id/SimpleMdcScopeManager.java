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
package io.opentracing.examples.slf4j_mdc_debug_id;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import org.slf4j.MDC;

/**
 * Puts debug_id into MDC when scope is activated. When the active scope closes restores the previous value.
 * It's designed to work with ThreadLocalScopeManager. For this ScopeManager to work scope must be activated and closed
 * inside the same thread - ideally using try-finally or try-with-resource.
 */
public class SimpleMdcScopeManager implements ScopeManager {
    private final String mdcKey;
    private final ScopeManager wrapped;
    private final DebugIdProvider debugId;

    interface DebugIdProvider {
        String get(Span span);
    }

    /**
     * @param mdcKey        MDC key which value will be set to debug_id
     * @param debugId debug_id provider - specific to a Tracer implementation
     * @param wrapped       ScopeManager to wrap
     */
    public SimpleMdcScopeManager(String mdcKey, DebugIdProvider debugId, ScopeManager wrapped) {
        this.mdcKey = mdcKey;
        this.wrapped = wrapped;
        this.debugId = debugId;
    }

    @Override
    public Scope activate(Span span) {
        return activate(span, true);
    }

    @Override
    public Scope activate(Span span, boolean finishSpanOnClose) {
        Scope scope = wrapped.activate(span, finishSpanOnClose);
        String debugId = MDC.get(mdcKey);
        MDC.put(mdcKey, this.debugId.get(span));
        return new MdcScope(scope, debugId);
    }

    @Override
    public Scope active() {
        return wrapped.active();
    }

    class MdcScope implements Scope {
        private final Scope wrapped;
        private final String previousDebugId;

        MdcScope(Scope wrapped, String previousDebugId) {
            this.wrapped = wrapped;
            this.previousDebugId = previousDebugId;
        }

        @Override
        public void close() {
            try {
                wrapped.close();
            } finally {
                MDC.put(mdcKey, previousDebugId);
            }
        }

        @Override
        public Span span() {
            return wrapped.span();
        }
    }
}
