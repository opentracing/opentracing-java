package io.opentracing.usecases;

import io.opentracing.Activator;
import io.opentracing.Span;
import io.opentracing.Tracer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by bhs on 8/5/17.
 */
public class AutoFinishActivator implements Activator {
    final ThreadLocal<AutoFinishScope> tlsScope = new ThreadLocal<AutoFinishScope>();

    @Override
    public AutoFinishScope activate(Span span) {
        return new AutoFinishScope(new AtomicInteger(1), span);
    }

    @Override
    public AutoFinishScope activeScope() {
        return tlsScope.get();
    }

    public class AutoFinishScope implements Scope {
        final AtomicInteger refCount;
        private final Span wrapped;
        private final AutoFinishScope toRestore;

        AutoFinishScope(AtomicInteger refCount, Span wrapped) {
            this.refCount = refCount;
            this.wrapped = wrapped;
            this.toRestore = AutoFinishActivator.this.tlsScope.get();
            tlsScope.set(this);
        }

        public class Continuation {
            public Continuation() {
                refCount.incrementAndGet();
            }

            public AutoFinishScope activate() {
                return new AutoFinishScope(refCount, wrapped);
            }
        }

        public Continuation defer() {
            return new Continuation();
        }

        @Override
        public void close() {
            if (refCount.decrementAndGet() == 0) {
                wrapped.finish();
            }
            tlsScope.set(toRestore);
        }

        @Override
        public Span span() {
            return wrapped;
        }
    }
}
