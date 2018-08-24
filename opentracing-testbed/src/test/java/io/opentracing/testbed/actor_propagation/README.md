# Actor propagation example.

This example shows `Span` usage with `Scala` frameworks using the `Actor` paradigm (such as the **Akka** framework), with both `tell` (fire and forget) and `ask` (asynchronous, but returning a result)  message passing ways. 

`Actor` mimics the API offered by such frameworks, and `java.util.concurrent.Phaser` is used to synchronize the steps of the example.

```java
  public void tell(final String message) {
    final Span parent = tracer.scopeManager().active().span();
    phaser.register();
    executor.submit(
        new Runnable() {
          @Override
          public void run() {
            Scope child = tracer
                .buildSpan("received")
                .addReference(References.FOLLOWS_FROM, parent.context())
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER)
                .startActive();
            try {
              ...
            } finally {
              child.close();
              child.span().finish();
            }
          }
        });
```
