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

Initialization is OpenTracing-implementation-specific. Generally speaking, the pattern is to initialize a `Tracer` once for the entire process and to use that `Tracer` for the remainder of the process lifetime. The [GlobalTracer](https://github.com/opentracing/opentracing-java/blob/master/opentracing-util/src/main/java/io/opentracing/util/GlobalTracer.java) provides a helper for singleton access to the `Tracer`.

### `ActiveSpan`s, `Continuation`s, and within-process prapagation

For any execution context or Thread, at most one `Span` may be "active". Of course there may be many other `Spans` involved with the execution context which are (a) started, (b) not finished, and yet (c) not "active": perhaps they are waiting for I/O, blocked on a child Span, or otherwise off the critical path.
 
It's inconvenient to pass an active `Span` from function to function manually, so OpenTracing provides an `ActiveSpanSource` abstraction to provide access to an `ActiveSpan` and to pin and `defer()` it for re-activation in other execution contexts (e.g., in an async callback).

Access to the active `Span` is straightforward:

```
io.opentracing.Tracer tracer = ...;
...
ActiveSpan span = tracer.activeSpan();
if (span != null) {
    span.log("...");
}
```

### Starting a new Span

The simplest case – which does not take advantage of `ActiveSpanSource` – looks like this:

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

Or, to take advantage of `ActiveSpanSource` and automatic intra-process propagation, like this:

```
io.opentracing.Tracer tracer = ...;
...
try (ActiveSpan activeSpan = tracer.buildSpan("someWork").startActive()) {
    // Do things.
    //
    // If we create async work, `activeSpan.defer()` allows us to pass the `ActiveSpan` along as well.
}
```


**If there is an `ActiveSpan`, it will act as the parent to any newly `startActive()`ed `Span`** unless the programmer invokes `asRoot()` at `buildSpan()` time, like so:

```
io.opentracing.Tracer tracer = ...;
...
SpanContext someOtherSpanContext = ...;
ActiveSpan span = tracer.buildSpan("someWork").asRoot().startActive();
```

### Deferring asynchronous work

Consider the case where a `Span`'s lifetime logically starts in one execution context and ends in another. For instance, the intra-Span timing breakdown might look like this:

```
[ ServiceHandlerSpan                                 ]
|-FunctionA-|-----waiting on an RPC------|-FunctionB-|
            
------------------------------------------------> time
```

The `"ServiceHandlerSpan"` is _active_ when it's running FunctionA and FunctionB, and inactive while it's waiting on an RPC (presumably modelled as its own Span, though that's not the concern here).

**The `ActiveSpanSource` makes it easy to "adopt" the Span and execution context in `FunctionA` and re-activate it in `FunctionB`.** Note that every `Tracer` must also implement `ActiveSpanSource`. These are the steps:

1. Start the `ActiveSpan` via `Tracer.startActive()` rather than via `Tracer.startManual()`; or, if the `Span` was already `startManual()`ed, call `ActiveSpanSource#adopt(span)`. Either route will yield an `ActiveSpan` instance that's "adopted" the `Span`.
2. In the method/function that *allocates* the closure/`Runnable`/`Future`/etc, call `ActiveSpan#defer()` to obtain an `ActiveSpan.Continuation`
3. In the closure/`Runnable`/`Future`/etc itself, invoke `ActiveSpan.Continuation#activate` to re-activate the `ActiveSpan`, then `deactivate()` it when the Span is no longer active (or use try-with-resources for less typing).

For example:

```
io.opentracing.Tracer tracer = ...;
...
// STEP 1 ABOVE: start the ActiveSpan
try (ActiveSpan serviceSpan = tracer.buildSpan("ServiceHandlerSpan").startActive()) {
    ...

    // STEP 2 ABOVE: defer the ActiveSpan
    final ActiveSpan.Continuation cont = serviceSpan.defer();
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

In practice, all of this is most fluently accomplished through the use of an OpenTracing-aware `ExecutorService` and/or `Runnable`/`Callable` adapter; they can factor most of the typing.

#### Reference counting with `ActiveSpan`s

When an `ActiveSpan` is created (either via `Tracer.SpanBuilder#startActive` or `ActiveSpanSource#adopt(Span)`), the reference count associated with the adopted `Span` is `1`.

- When an `ActiveSpan.Continuation` is created via `ActiveSpan#defer`, the reference count **increments**
- When an `ActiveSpan.Continuation` is `ActiveSpan.Continuation#activate()`d and thus transformed back into an `ActiveSpan`, the reference count **is unchanged**
- When an `ActiveSpan` is `ActiveSpan#deactivate()`d, the reference count **decrements**

When the reference count decrements to zero, **the `Span`'s `finish()` method is invoked automatically.**

When used as designed, the programmer should not need to invoke `Span#finish()` manually, but rather should let `ActiveSpan` and `ActiveSpan.Continuation` invoke `finish()` as soon as the last active or deferred `ActiveSpan` is deactivated.

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
