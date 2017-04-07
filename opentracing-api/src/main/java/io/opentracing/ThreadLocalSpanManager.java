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

/**
 * @author Pavol Loffay
 */
public class ThreadLocalSpanManager implements SpanManager {

    private final ThreadLocal<ThreadLocalVisibility> activeContext = new ThreadLocal<ThreadLocalVisibility>();

    public ThreadLocalSpanManager() {}

    @Override
    public Visibility bundle(Span span) {
        return span.visibility() == null ? new ThreadLocalVisibility(span) : span.visibility();
    }

    @Override
    public Visibility active() {
        return activeContext.get() == null ? null : activeContext.get();
    }

    class ThreadLocalVisibility implements Visibility {
        private final Span span;
        private ThreadLocalVisibility previous;
        private boolean finished;

        public ThreadLocalVisibility(Span span) {
            this.span = span;
        }

        @Override
        public Span span() {
            return finished ? null : span;
        }

        @Override
        public SpanContext context() {
            return span.context();
        }

        @Override
        public void activate() {
            previous = activeContext.get();
            activeContext.set(this);
        }

        @Override
        public void deactivate() {
            if (this != activeContext.get()) {
                // should not happen
                return;
            }
            activeContext.set(previous);
        }

        @Override
        public void markAsFinished() {
            this.finished = true;
        }
    }
}
