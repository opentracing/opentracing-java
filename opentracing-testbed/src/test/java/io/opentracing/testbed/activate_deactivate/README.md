# Activate-deactivate example.

This example shows the usage of the `Continuation` concept (now part of the `AutoFinishScopeManager`/`AutoScope.capture()`), as means to finish a `Span` till **all** its related subtasks have been executed. This example is similar to the **Multiple callbacks example**, but slightly more complex, and more aimed at trying out the usage of `Continuation`s.

`RunnableAction` is a class implementing `Runnable`, and represents a subtask/callback that is scheduled to run at random time in the future, increasing the reference count of the passed `Scope`/`Span`. The `ScheduledActionsTest` includes both a test case with a single subtask, and another with two of them.

```java
return new Thread(new Runnable() {
    @Override
    public void run() {
	try (Scope scope = tracer.buildSpan("parent").startActive()) {
	    Runnable action = new RunnableAction((AutoFinishScope)scope);
	    Runnable action2 = new RunnableAction((AutoFinishScope)scope);

	    Random random = new Random();

	    // Actions are executed at some time and most likely are running in parallel
	    service.schedule(action, random.nextInt(1000) + 100, TimeUnit.MILLISECONDS);
	    service.schedule(action2, random.nextInt(1000) + 100, TimeUnit.MILLISECONDS);

	}
    }
});
```
