[![Build Status][ci-img]][ci] [![Coverage Status][cov-img]][cov] [![Released Version][maven-img]][maven]

# OpenTracing API for Java

This library is a Java platform API for OpenTracing.

## Required Reading

In order to understand the Java platform API, one must first be familiar with
the [OpenTracing project](http://opentracing.io) and
[terminology](http://opentracing.io/documentation/pages/spec.html) more specifically.

## Usage

### Initialization

Initialization is OpenTracing-implementation-specific. Generally speaking, the pattern is to initialize a `Tracer` once for the entire process and to use that `Tracer` for the remainder of the process lifetime. It is a best practice to _set_ the [GlobalTracer](https://github.com/opentracing/opentracing-java/blob/master/opentracing-util/src/main/java/io/opentracing/util/GlobalTracer.java), even if also making use of cleaner, more modern dependency injection. (See the next section below for rationale)

### Accessing the `Tracer`

Where possible, use some form of dependency injection (of which there are many) to access the `Tracer` instance. For vanilla application code, this is often reasonable and cleaner for all of the usual DI reasons.

That said, instrumentation for packages that are themselves statically configured (e.g., JDBC drivers) may be unable to make use of said DI mechanisms for `Tracer` access, and as such they should fall back on [GlobalTracer](https://github.com/opentracing/opentracing-java/blob/master/opentracing-util/src/main/java/io/opentracing/util/GlobalTracer.java). By and large, OpenTracing instrumentation should always allow the programmer to specify a `Tracer` instance to use for instrumentation, though the [GlobalTracer](https://github.com/opentracing/opentracing-java/blob/master/opentracing-util/src/main/java/io/opentracing/util/GlobalTracer.java) is a reasonable fallback or default value.

### Within-process propagation and the `Scope`

For any thread, at most one `Span` may be "active". Of course there may be many other `Spans` involved with the thread which are (a) started, (b) not finished, and yet (c) not "active": perhaps they are waiting for I/O, blocked on a child Span, or otherwise off of the critical path.
 
It's inconvenient to pass an active `Span` from function to function manually, so OpenTracing requires that every `Tracer` contains a `ScopeManager` that grants access to the active `Span` along with a `Scope` to signal deactivation. Any `Span` may be transferred to another callback or thread, but not `Scope`; more on this below.

#### Accessing the active Span

Access to the active span is straightforward:

```java
io.opentracing.Tracer tracer = ...;
...
Span span = tracer.scopeManager().activeSpan();
if (span != null) {
    span.log("...");
}
```

### Starting a new Span

The common case starts a `Span` and then sets it as the active instance via `ScopeManager`:

```java
io.opentracing.Tracer tracer = ...;
...
Span span = tracer.buildSpan("someWork").start();
try (Scope scope = tracer.scopeManager().activate(span)) {
    // Do things.
} catch(Exception ex) {
    Tags.ERROR.set(span, true);
    span.log(Map.of(Fields.EVENT, "error", Fields.ERROR_OBJECT, ex, Fields.MESSAGE, ex.getMessage()));
} finally {
    span.finish();
}
```

**If there is already an active `Span`, it will act as the parent to any newly started `Span`** unless
the programmer invokes `ignoreActiveSpan()` at `buildSpan()` time or specified parent context explicitly:

```java
io.opentracing.Tracer tracer = ...;
...
Span span = tracer.buildSpan("someWork").ignoreActiveSpan().start();
```

### Deferring asynchronous work

Consider the case where a `Span`'s lifetime logically starts in one thread and ends in another. For instance, the Span's own internal timing breakdown might look like this:

```
 [ ServiceHandlerSpan                                 ]
 |·FunctionA·|·····waiting on an RPC······|·FunctionB·|
            
---------------------------------------------------------> time
```

The `"ServiceHandlerSpan"` is _active_ while it's running FunctionA and FunctionB, and inactive while it's waiting on an RPC (presumably modelled as its own Span, though that's not the concern here).

**The `ScopeManager` API makes it possible to fetch the `span` in `FunctionA` and re-activate it in `FunctionB`.** Note that every `Tracer` contains a `ScopeManager`. These are the steps:

1. Start a `Span` via `start`.
2. At the beginning of the closure/`Runnable`/`Future`/etc itself, invoke `tracer.scopeManager().activate(span)` to re-activate the `Span` and get a new `Scope`, then `close()` it when the `Span` is no longer active (or use try-with-resources for less typing).
3. Invoke `span.finish()` when the work is done.

Here is an example using `CompletableFuture`:

```java
io.opentracing.Tracer tracer = ...;
...
// STEP 1 ABOVE: start the Span.
final Span span = tracer.buildSpan("ServiceHandlerSpan").start();
try (Scope scope = tracer.scopeManager().activate(span)) {
    // Do work.
    ...

    future = CompletableFuture.supplyAsync(() -> {

        // STEP 2 ABOVE: reactivate the Span in the callback.
        try (Scope scope = tracer.scopeManager().activate(span)) {
            ...
        }
    }).thenRun(() -> {
        // STEP 3 ABOVE: finish the Span when the work is done.
        span.finish();
    });
}
```

Observe that passing `Scope` to another thread or callback is not supported. Only `Span` can be used under this scenario.

In practice, all of this is most fluently accomplished through the use of an OpenTracing-aware `ExecutorService` and/or `Runnable`/`Callable` adapter; they factor out most of the typing.

## Deprecated members since 0.31

`ScopeManager.active(Span, boolean)` and `SpanBuilder.startActive()` have been deprecated as part of removing automatic `Span` finish upon `Scope` close, as doing it through try-with statements would make it hard to properly handle errors (`Span` objects would get finished before a catch block would be reached).
This improves API safety, and makes it more difficult to do the wrong thing and end up with unexpected errors.

`Scope.span()` and `ScopeManager.scope()` have been deprecated in order to prevent the anti-pattern of passing `Scope` objects between threads (`Scope` objects are not guaranteed to be thread-safe).
Now `Scope` will be responsible for `Span` deactivation only, instead of being a `Span` container.

## Instrumentation Tests

This project has a working design of interfaces for the OpenTracing API. There
is a [MockTracer](https://github.com/opentracing/opentracing-java/tree/master/opentracing-mock)
to facilitate unit-testing of OpenTracing Java instrumentation.

Packages are deployed to Maven Central under the `io.opentracing` group.

## Development

This is a maven project, and provides a wrapper, `./mvnw` to pin a consistent
version. Run `./mvnw clean install` to build, run tests, and create jars.

This wrapper was generated by `mvn -N io.takari:maven:wrapper -Dmaven=3.5.0`

## License

[Apache 2.0 License](./LICENSE).


## Contributing

See [Contributing](CONTRIBUTING.md) for matters such as license headers.


  [ci-img]: https://travis-ci.org/opentracing/opentracing-java.svg?branch=master
  [ci]: https://travis-ci.org/opentracing/opentracing-java
  [cov-img]: https://coveralls.io/repos/github/opentracing/opentracing-java/badge.svg?branch=master
  [cov]: https://coveralls.io/github/opentracing/opentracing-java?branch=master
  [maven-img]: https://img.shields.io/maven-central/v/io.opentracing/opentracing-api.svg?maxAge=2592000
  [maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-api


