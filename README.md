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

### `Scope`s and within-process propagation

For any thread, at most one `Span` may be "active". Of course there may be many other `Spans` involved with the thread which are (a) started, (b) not finished, and yet (c) not "active": perhaps they are waiting for I/O, blocked on a child Span, or otherwise off of the critical path.
 
It's inconvenient to pass an active `Span` from function to function manually, so OpenTracing requires that every `Tracer` contains a `ScopeManager` that grants access to the active `Span` through a `Scope`. Any `Span` may be transferred to another callback or thread, but not `Scope`; more on this below.

#### Accessing the active Span through `Scope`

Access to the active span is straightforward:

```java
io.opentracing.Tracer tracer = ...;
...
Scope scope = tracer.scopeManager().active();
if (scope != null) {
    scope.span().log("...");
}
```

### Starting a new Span

The common case starts a `Scope` that's automatically registered for intra-process propagation via `ScopeManager`.

Note that `startActive(true)` finishes the span on `Scope.close()`.
Use it carefully because the `try-with-resources` construct finishes the span before
the `catch` or `finally` blocks are executed, which makes logging exceptions and 
setting tags impossible. It is recommended to start the span and activate it later in `try-with-resources`.
This makes the span available in catch and finally blocks.

```java
io.opentracing.Tracer tracer = ...;
...
Span span = tracer.buildSpan("someWork").start();
try (Scope scope = tracer.scopeManager().activate(span, false))
    // Do things.
} catch {
    Tags.ERROR.set(scope.span(), true);
} finally {
    span.finish();
}
```

The following code uses `try-with-resources` to finish the span.

```java
io.opentracing.Tracer tracer = ...;
...
try (Scope scope = tracer.buildSpan("someWork").startActive(true)) {
    // Do things.
    //
    // `scope.span()` allows us to pass the `Span` to newly created threads.
}
```

**If there is a `Scope`, it will act as the parent to any newly started `Span`** unless 
the programmer invokes `ignoreActiveSpan()` at `buildSpan()` time or specified parent context explicitly:

```java
io.opentracing.Tracer tracer = ...;
...
Scope scope = tracer.buildSpan("someWork").ignoreActiveSpan().startActive();
```

### Deferring asynchronous work

Consider the case where a `Span`'s lifetime logically starts in one thread and ends in another. For instance, the Span's own internal timing breakdown might look like this:

```
 [ ServiceHandlerSpan                                 ]
 |·FunctionA·|·····waiting on an RPC······|·FunctionB·|
            
---------------------------------------------------------> time
```

The `"ServiceHandlerSpan"` is _active_ while it's running FunctionA and FunctionB, and inactive while it's waiting on an RPC (presumably modelled as its own Span, though that's not the concern here).

**The `ScopeManager` API makes it possible to fetch the `span()` in `FunctionA` and re-activate it in `FunctionB`.** Note that every `Tracer` contains a `ScopeManager`. These are the steps:

1. Start a `Span` via either `startManual` or `startActive(false)` to prevent the `Span` from being finished upon `Scope` deactivation.
2. In the closure/`Runnable`/`Future`/etc itself, invoke `tracer.scopeManager().activate(span, false)` to re-activate the `Span` and get a new `Scope`, then `deactivate()` it when the `Span` is no longer active (or use try-with-resources for less typing).
3. In the closure/`Runnable`/`Future`/etc where the end of the task is reached, invoke `tracer.scopeManager().activate(span, true)` to re-activate the `Span` and have the new `Scope` close the `Span` automatically.

For example:

```java
io.opentracing.Tracer tracer = ...;
...
// STEP 1 ABOVE: start the Scope/Span
try (Scope scope = tracer.buildSpan("ServiceHandlerSpan").startActive(false)) {
    ...
    final Span span = scope.span();
    doAsyncWork(new Runnable() {
        @Override
        public void run() {

            // STEP 2 ABOVE: reactivate the Span in the callback, passing true to
            // startActive() if/when the Span must be finished.
            try (Scope scope = tracer.scopeManager().activate(span, false)) {
                ...
            }
        }
    });
}
```

Observe that passing `Scope` to another thread or callback is not supported. Only `Span` can be used under this scenario.

In practice, all of this is most fluently accomplished through the use of an OpenTracing-aware `ExecutorService` and/or `Runnable`/`Callable` adapter; they factor out most of the typing.

## Instrumentation Tests

This project has a working design of interfaces for the OpenTracing API. There
is a [MockTracer](https://github.com/opentracing/opentracing-java/tree/master/opentracing-mock)
to facilitate unit-testing of OpenTracing Java instrumentation.

Packages are deployed to Maven Central under the `io.opentracing` group.

## Development

This is a maven project, and provides a wrapper, `./mvnw` to pin a consistent
version. Run `./mvnw clean install` to build, run tests, and create jars.

This wrapper was generated by `mvn -N io.takari:maven:wrapper -Dmaven=3.5.0`

## Contributing

See [Contributing](CONTRIBUTING.md) for matters such as license headers.


  [ci-img]: https://travis-ci.org/opentracing/opentracing-java.svg?branch=master
  [ci]: https://travis-ci.org/opentracing/opentracing-java
  [cov-img]: https://coveralls.io/repos/github/opentracing/opentracing-java/badge.svg?branch=master
  [cov]: https://coveralls.io/github/opentracing/opentracing-java?branch=master
  [maven-img]: https://img.shields.io/maven-central/v/io.opentracing/opentracing-api.svg?maxAge=2592000
  [maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-api
