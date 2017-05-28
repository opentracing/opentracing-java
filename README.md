[![Build Status][ci-img]][ci] [![Coverage Status][cov-img]][cov] [![Released Version][maven-img]][maven]

# OpenTracing API for Java

This library is a Java platform API for OpenTracing.

## Required Reading

In order to understand the Java platform API, one must first be familiar with
the [OpenTracing project](http://opentracing.io) and
[terminology](http://opentracing.io/documentation/pages/spec.html) more specifically.

## Status

This project has a working design of interfaces for the OpenTracing API. There
is a [MockTracer](https://github.com/opentracing/opentracing-java/tree/master/opentracing-mock)
to facilitate unit-testing of OpenTracing Java instrumentation.

Packages are deployed to Maven Central under the `io.opentracing` group.

## Usage

### Initialization

Initialization is OpenTracing-implementation-specific. Generally speaking, the pattern is to initialize a `Tracer` once for the entire process and to use that `Tracer` for the remainder of the process lifetime. It is a best practice to _set_ the [GlobalTracer](https://github.com/opentracing/opentracing-java/blob/master/opentracing-util/src/main/java/io/opentracing/util/GlobalTracer.java), even if also making use of cleaner, more modern dependency injection. (See the next section below for rationale)

### Accessing the `Tracer`

Where possible, use dependency injection (of which there are many) to access the `Tracer` instance. For vanilla application code, this is often reasonable and cleaner for all of the usual DI reasons.

That said, instrumentation for packages that are themselves statically configured (e.g., JDBC drivers) may be unable to make use of said DI mechanisms for `Tracer` access, and as such they should fall back on [GlobalTracer](https://github.com/opentracing/opentracing-java/blob/master/opentracing-util/src/main/java/io/opentracing/util/GlobalTracer.java). By and large, OpenTracing instrumentation should always allow the programmer to specify a `Tracer` instance to use for instrumentation, though the [GlobalTracer](https://github.com/opentracing/opentracing-java/blob/master/opentracing-util/src/main/java/io/opentracing/util/GlobalTracer.java) is a reasonable fallback or default value.

### `ActiveSpan`s, `Continuation`s, and within-process propagation

For any thread, at most one `Span` may be "active". Of course there may be many other `Spans` involved with the thread which are (a) started, (b) not finished, and yet (c) not "active": perhaps they are waiting for I/O, blocked on a child Span, or otherwise off of the critical path.
 
It's inconvenient to pass an active `Span` from function to function manually, so OpenTracing requires that every `Tracer` implement an `ActiveSpanSource` interface that grants access to an `ActiveSpan`. Any `ActiveSpan` may be transferred to another callback or thread via `ActiveSpan#defer()` and `Continuation#activate()`; more on this below.

#### Accessing the `ActiveSpan`

Access to the active span is straightforward:

```
io.opentracing.Tracer tracer = ...;
...
ActiveSpan span = tracer.activeSpan();
if (span != null) {
    span.log("...");
}
```

### Starting a new Span

The common case starts an `ActiveSpan` that's automatically registered for intra-process propagation via `ActiveSpanSource`. The best practice is to use a try-with-resources pattern which handles Exceptions and early returns:

```
io.opentracing.Tracer tracer = ...;
...
try (ActiveSpan activeSpan = tracer.buildSpan("someWork").startActive()) {
    // Do things.
    //
    // If we create async work, `activeSpan.capture()` allows us to pass the `ActiveSpan` along as well.
}
```

The above is semantically equivalent to the more explicit try-finally version:

```
io.opentracing.Tracer tracer = ...;
...
ActiveSpan activeSpan = tracer.buildSpan("someWork").startActive();
try {
    // Do things.
} finally {
    activeSpan.deactivate();
}
```

To manually step around the `ActiveSpanSource` registration, use `startManual()`, like this:

```
io.opentracing.Tracer tracer = ...;
...
Span span = tracer.buildSpan("someWork").startManual();
try {
    // (do things / record data to `span`)
} finally {
    span.finish();
}
```

**If there is an `ActiveSpan`, it will act as the parent to any newly started `Span`** unless the programmer invokes `ignoreActiveSpan()` at `buildSpan()` time, like so:

```
io.opentracing.Tracer tracer = ...;
...
ActiveSpan span = tracer.buildSpan("someWork").ignoreActiveSpan().startActive();
```

### Deferring asynchronous work

Consider the case where a `Span`'s lifetime logically starts in one thread and ends in another. For instance, the Span's own internal timing breakdown might look like this:

```
 [ ServiceHandlerSpan                                 ]
 |·FunctionA·|·····waiting on an RPC······|·FunctionB·|
            
---------------------------------------------------------> time
```

The `"ServiceHandlerSpan"` is _active_ while it's running FunctionA and FunctionB, and inactive while it's waiting on an RPC (presumably modelled as its own Span, though that's not the concern here).

**The `ActiveSpanSource` API makes it easy to `capture()` the Span and execution context in `FunctionA` and re-activate it in `FunctionB`.** Note that every `Tracer` implements `ActiveSpanSource`. These are the steps:

1. Start an `ActiveSpan` via `Tracer.startActive()` (or, if the `Span` was already started manually via `startManual()`, call `ActiveSpanSource#makeActive(span)`)
2. In the method that *allocates* the closure/`Runnable`/`Future`/etc, call `ActiveSpan#capture()` to obtain an `ActiveSpan.Continuation`
3. In the closure/`Runnable`/`Future`/etc itself, invoke `ActiveSpan.Continuation#activate` to re-activate the `ActiveSpan`, then `deactivate()` it when the Span is no longer active (or use try-with-resources for less typing).

For example:

```
io.opentracing.Tracer tracer = ...;
...
// STEP 1 ABOVE: start the ActiveSpan
try (ActiveSpan serviceSpan = tracer.buildSpan("ServiceHandlerSpan").startActive()) {
    ...

    // STEP 2 ABOVE: capture the ActiveSpan
    final ActiveSpan.Continuation cont = serviceSpan.capture();
    doAsyncWork(new Runnable() {
        @Override
        public void run() {

            // STEP 3 ABOVE: use the Continuation to reactivate the Span in the callback.
            try (ActiveSpan activeSpan = cont.activate()) {
                ...
            }
        }
    });
}
```

In practice, all of this is most fluently accomplished through the use of an OpenTracing-aware `ExecutorService` and/or `Runnable`/`Callable` adapter; they factor out most of the typing.

#### Automatic `finish()`ing via `ActiveSpan` reference counts

When an `ActiveSpan` is created (either via `Tracer.SpanBuilder#startActive` or `ActiveSpanSource#makeActive(Span)`), the reference count associated with the `ActiveSpan` is `1`.

- When an `ActiveSpan.Continuation` is created via `ActiveSpan#capture`, the reference count **increments**
- When an `ActiveSpan.Continuation` is `ActiveSpan.Continuation#activate()`d and thus transformed back into an `ActiveSpan`, the reference count **is unchanged**
- When an `ActiveSpan` is `ActiveSpan#deactivate()`d, the reference count **decrements**

When the reference count decrements to zero, **the `Span`'s `finish()` method is invoked automatically.**

When used as designed, the programmer lets `ActiveSpan` and `ActiveSpan.Continuation` finish the `Span` as soon as the last active or deferred `ActiveSpan` is deactivated.

# Development

This is a maven project, and provides a wrapper, `./mvnw` to pin a consistent
version. For example, `./mvnw clean install`.

This wrapper was generated by `mvn -N io.takari:maven:wrapper -Dmaven=3.5.0`

## Building

Execute `./mvnw clean install` to build, run tests, and create jars.

## Contributing

See [Contributing](CONTRIBUTING.md) for matters such as license headers.


  [ci-img]: https://travis-ci.org/opentracing/opentracing-java.svg?branch=master
  [ci]: https://travis-ci.org/opentracing/opentracing-java
  [cov-img]: https://coveralls.io/repos/github/opentracing/opentracing-java/badge.svg?branch=master
  [cov]: https://coveralls.io/github/opentracing/opentracing-java?branch=master
  [maven-img]: https://img.shields.io/maven-central/v/io.opentracing/opentracing-api.svg?maxAge=2592000
  [maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-api
