package io.opentracing.util;

import io.opentracing.Activator;
import io.opentracing.ActiveSpan;
import io.opentracing.Span;

/**
 * Created by bhs on 8/1/17.
 */
public class ThreadLocalActivator implements Activator {
    final ThreadLocal<ThreadLocalScope> tlsScope = new ThreadLocal<ThreadLocalScope>();

    public class ThreadLocalScope implements Scope {
        private final Span wrapped;
        private final ThreadLocalScope toRestore;

        ThreadLocalScope(Span wrapped) {
            this.wrapped = wrapped;
            this.toRestore = ThreadLocalActivator.this.tlsScope.get();
            tlsScope.set(this);
        }

        @Override
        public void close() {
            tlsScope.set(toRestore);
        }

        @Override
        public Span span() {
            return wrapped;
        }
    }

    @Override
    public Scope activate(Span span) {
        return new ThreadLocalScope(span);
    }

    @Override
    public Scope activeScope() {
        return tlsScope.get();
    }
}
