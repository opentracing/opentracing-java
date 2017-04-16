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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Pavol Loffay
 */
public class ThreadLocalSpanManager implements SpanManager {

    private final ThreadLocal<SimpleLinkedVisibilityContext> activeContext = new ThreadLocal<SimpleLinkedVisibilityContext>();

    @Override
    public Visibility bundle(Span span) {
        return span.visibility() == null ? new SimpleVisibility(span) : span.visibility();
    }

    @Override
    public SimpleLinkedVisibilityContext active() {
        return activeContext.get();
    }

    class SimpleVisibility implements Visibility {
        private final Span span;
        private AtomicBoolean hideSpan = new AtomicBoolean(false);

        public SimpleVisibility(Span span) {
            this.span = span;
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
        public SimpleLinkedVisibilityContext capture() {
            return new SimpleLinkedVisibilityContext(this);
        }

        @Override
        public void hideSpan() {
            hideSpan.set(true);
        }
    }

    class SimpleLinkedVisibilityContext implements SpanManager.VisibilityContext {

        private final SimpleVisibility visibility;
        private SimpleLinkedVisibilityContext previous;

        public SimpleLinkedVisibilityContext(SimpleVisibility visibility) {
            this.visibility = visibility;
        }

        @Override
        public SimpleLinkedVisibilityContext on() {
            previous = activeContext.get();
            activeContext.set(this);
            return this;
        }

        @Override
        public SimpleLinkedVisibilityContext off() {
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
