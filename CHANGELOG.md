Changes by Version
==================

v0.31.0 (2018-01-12)
--------------------

- `BaseSpan` and `ActiveSpan` are simplified into a single `Span` class.
- `Scope` replaces `ActiveSpan`, removing the continuation concept. There is no longer a capture phase. Scopes are only activated and then closed.
- `ScopeManager` replaces `ActiveSpanSource`, and is expected to be passed to the tracer at initialization time.
- `ScopeManager.activate(Span,bool)` creates a `Scope` for the current context. The `finishOnClose` flag optionally calls `Span.finish()` on `Scope.close()`.
- `ScopeManager.active()` returns the `Scope` associated with the current context.
- `Tracer.activeSpan()` returns the current value of `Tracer.scopeManager().active().span()` as a convenience.
- `ThreadLocalScopeManager` replaces `ThreadLocalActiveSpanSource`. It still uses thread-local storage for tracking active spans, but removes the reference counting used previously for span lifetime handling.
