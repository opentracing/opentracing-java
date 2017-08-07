package io.opentracing;

/**
 * The {@link ScopeManager} interface abstracts both the activation of {@link Span} instances (via
 * {@link ScopeManager#activate(Span)}) and access to an active {@link Span}/{@link Scope}
 * (via {@link ScopeManager#active()}).
 *
 * @see Scope
 * @see Tracer#setScopeManager(ScopeManager)
 */
public interface ScopeManager {
    /**
     * Make a {@link Span} instance active.
     *
     * @param span the {@link Span} that should become the {@link #active()}
     * @return a {@link Scope} instance to control the end of the active period for the {@link Span}. It is a
     * programming error to neglect to call {@link Scope#close()} on the returned instance.
     */
    Scope activate(Span span);

    /**
     * @return the currently active {@link Scope} which can be used to access the currently active
     * {@link Scope#span()}
     */
    Scope active();
}
