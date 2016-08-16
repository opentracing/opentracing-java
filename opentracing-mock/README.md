# OpenTracing-Java Mock Tracer

The `opentracing-mock` artifact (and `io.opentracing.mock.MockTracer` in particular) make it easy to unittest the semantics of OpenTracing instrumentation.

## Example Usage

Imagine the following (admittedly unrealistic) OpenTracing-instrumented application code:

```
public void handlePurchase(User user, Item item, Tracer tracer) {
    try (Span span = tracer.buildSpan("handlePurchase").start()) {
        span.setTag("username", user.getUsername());
    }
}
```

Using `MockTracer`, one could write a unittest for the above instrumentation like so:

```
@Test
public void testHandlePurchaseTracing() {
    // Initialize the MockTracer and call handlePurchase().
    MockTracer tracer = new MockTracer();
    foo.handlePurchase(..., ..., tracer);
    
    // Verify that the instrumentation generates the expected Span data.
    List<MockSpan> finishedSpans = tracer.finishedSpans();
    assertEquals(1, finishedSpans.size());
    MockSpan finishedSpan = finishedSpans.get(0);
    assertEquals("handlePurchase", finishedSpan.operationName());
    Map<String, Object> tags = finishedSpan.tags();
    assertEquals(1, tags.size());
    assertEquals("mockUsername", tags.get("username"));
}
```
