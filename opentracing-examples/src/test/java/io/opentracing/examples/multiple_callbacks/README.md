# Multiple callbacks example.

This example shows a `Span` created for a top-level operation, covering a set of asynchronous operations (representing callbacks), and have this `Span` finished when **all** of them have been executed.

`Client.send()` is used to create a new asynchronous operation (callback), and in turn every operation both restores the active `Span`, and creates a child `Span` (useful for measuring the performance of each callback). `AutoFinishScopeManager` is used so the related callbacks can be referenced, and properly finish the main `Span` when all pending work is done.

```java
// Client.send()
Scope scope = tracer.scopeManager().active();
final Continuation cont = ((AutoFinishScope)scope).capture();

return executor.submit(new Callable<Object>() {
    @Override
    public Object call() throws Exception {
	try (Scope parentScope = cont.activate()) {
	    try (Scope subtaskScope = tracer.buildSpan("subtask").startActive(false)) {
                ...

```
