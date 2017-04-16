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
package io.opentracing.spanmanager.mdc;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.MDC;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.SpanManager;

/**
 * @author Pavol Loffay
 */
public class MDCThreadLocalSpanManager implements SpanManager {

    private final ThreadLocal<MDCLinkedVisibilityContext> activeContext = new ThreadLocal<>();

    @Override
    public Visibility bundle(Span span) {
        return span.visibility() == null ? new MDCVisibility(span) : span.visibility();
    }

    @Override
    public VisibilityContext active() {
        return activeContext.get();
    }

    class MDCVisibility implements Visibility {
        private final Span span;
        private AtomicBoolean hideSpan = new AtomicBoolean(false);

        public MDCVisibility(Span span) {
            this.span = span;
        }

        @Override
        public MDCLinkedVisibilityContext capture() {
            return new MDCLinkedVisibilityContext(this);
        }

        @Override
        public Span span() {
            return hideSpan.get() ? null : span;
        }

        @Override
        public SpanContext context() {
            return span.context();
        }

        @Override
        public void hideSpan() {
            hideSpan.set(true);
        }
    }

    class MDCLinkedVisibilityContext implements VisibilityContext {
        private final MDCVisibility visibility;
        private MDCLinkedVisibilityContext previous;
        private Map<String, String> mdcContext;

        public MDCLinkedVisibilityContext(MDCVisibility visibility) {
            this.visibility = visibility;
            this.mdcContext = MDC.getCopyOfContextMap();
        }

        @Override
        public MDCLinkedVisibilityContext on() {
            MDC.setContextMap(mdcContext);
            previous = activeContext.get();
            activeContext.set(this);
            return this;
        }

        @Override
        public MDCLinkedVisibilityContext off() {
            if (this == activeContext.get()) {
                activeContext.set(previous);
            }
            // else should not happen

            return this;
        }

        @Override
        public Visibility visibility() {
            return visibility;
        }
    }
}
