# Active Span replacement example.

This example shows a `Span` being created and then passed to an asynchronous task, which will temporary activate it to finish its processing, and further restore the previously active `Span`.

```java
// Create a new Span for this task
try (Scope taskScope = tracer.buildSpan("task").startActive(true)) {

    // Simulate work strictly related to the initial Span
    // and finish it.
    try (Scope initialScope = tracer.scopeManager().activate(initialSpan, true)) {
    }
}
```
