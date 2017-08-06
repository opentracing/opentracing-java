package io.opentracing;

/**
 * The {@link ScopeManager} interface abstracts both the activation of {@link Span} instances (via
 * {@link ScopeManager#activate(Span)}) and access to an active {@link Span}/{@link Scope}
 * (via {@link ScopeManager#activeScope()}).
 *
 * @see Scope
 * @see Tracer#setScopeManager(ScopeManager)
 */
public interface ScopeManager {
    Scope activate(Span span);

    Scope activeScope();
}
