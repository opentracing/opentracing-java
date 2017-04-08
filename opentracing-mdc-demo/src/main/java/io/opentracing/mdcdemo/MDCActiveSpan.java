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

package io.opentracing.mdcdemo;

import io.opentracing.ActiveSpanSource;
import io.opentracing.Span;
import io.opentracing.impl.AbstractActiveSpan;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MDCActiveSpan illustrates the core ActiveSpan concepts and capabilities to a first approximation. Not
 * production-quality code.
 */
class MDCActiveSpan extends AbstractActiveSpan {
    private MDCActiveSpanSource mdcActiveSpanSource;
    private MDCActiveSpan toRestore = null;

    MDCActiveSpan(MDCActiveSpanSource mdcActiveSpanSource, Span span, Map<String, String> mdcContext, AtomicInteger refCount) {
        super(span, refCount);
        this.mdcActiveSpanSource = mdcActiveSpanSource;
        this.toRestore = mdcActiveSpanSource.tlsSnapshot.get();
        mdcActiveSpanSource.tlsSnapshot.set(this);
        MDC.setContextMap(mdcContext);
    }

    @Override
    protected void doDeactivate() {
        if (mdcActiveSpanSource.tlsSnapshot.get() != this) {
            // This shouldn't happen if users call methods in the expected order. Bail out.
            return;
        }
        mdcActiveSpanSource.tlsSnapshot.set(toRestore);
    }

    @Override
    protected ActiveSpanSource spanSource() {
        return mdcActiveSpanSource;
    }

    static class MDCContinuation extends AbstractActiveSpan.AbstractContinuation {
        private MDCActiveSpanSource mdcActiveSpanSource;
        private final Map<String, String> mdcContext;
        private final Span span;

        MDCContinuation(MDCActiveSpanSource source, Span span, AtomicInteger refCount) {
            super(refCount);
            this.mdcActiveSpanSource = source;
            this.mdcContext = MDC.getCopyOfContextMap();
            this.span = span;
        }

        @Override
        public MDCActiveSpan activate() {
            return new MDCActiveSpan(mdcActiveSpanSource, span, mdcContext, refCount);
        }
    }

}
