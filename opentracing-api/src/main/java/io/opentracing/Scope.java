package io.opentracing;

import java.io.Closeable;

/**
 * A {@link Scope} formalizes the activation and deactivation of a {@link Span}, usually from a CPU standpoint.
 *
 * <p>
 * Many times a {@link Span} will be extant (in that {@link Span#finish()} has not been called) despite being in a
 * non-runnable state from a CPU/scheduler standpoint. For instance, a {@link Span} representing the client side of an
 * RPC will be unfinished but blocked on IO while the RPC is still outstanding. A {@link Scope} defines when a given
 * {@link Span} <em>is</em> scheduled and on the critical path.
 */
public interface Scope extends Closeable {
    /**
     * End this {@link Scope}, updating the {@link ScopeManager#active()} in the process.
     */
    @Override
    void close();

    /**
     * @return the {@link Span} that's been scoped by this {@link Scope}
     */
    Span span();
}
