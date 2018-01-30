#Changes by Version

## v0.31.0 (2018-01-12) 
* `BaseSpan` and `ActiveSpan` are simplified into a single `Span` class.
* `Scope` replaces `ActiveSpan`, removing `Continuations`.
* `ScopeManager` replaces `ActiveSpanSource`.
* `Tracer.activeSpan()` returns the current value of `Tracer.scopeManager().active().span()` as a convenience.
* `startManual()` deprecated in favor of `start()`
* new examples directory
