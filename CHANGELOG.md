# Changes by Version

## v0.32.0 (2019-03-20)

* Trace Identifiers added to `SpanContext`.
* Finishing `Span` upon Scope close is deprecated overall.
* `ScopeManager.active()` is deprecated.
* `SpanBuilder.startActive()` is deprecated.
* `AutoFinishScopeManager` is deprecated.
* Added a new `Binary` format on top of `ByteBuffer`.
* Added generic typed `setTag()`/`withTag()` versions.
* Split Inject and Extract builtin interfaces.
* `Tracer` implements `Closable`.
* Added `GlobalTracer.registerIfAbsent()`.
* Added `GlobalTracer.isRegistered()`.

## v0.31.0 (2018-01-12) 
* `BaseSpan` and `ActiveSpan` are simplified into a single `Span` class.
* `Scope` replaces `ActiveSpan`, removing `Continuations`.
* `ScopeManager` replaces `ActiveSpanSource`.
* `Tracer.activeSpan()` returns the current value of `Tracer.scopeManager().active().span()` as a convenience.
* `startManual()` deprecated in favor of `start()`
* new examples directory
