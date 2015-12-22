# api-java

Move along, nothing to see here (yet!)... Non-master branch in poor Java style will illustrate some core concepts for those who don't grok Golang.

## Notes about the io.opentracing.* sketch

This is an intentionally naive port of the [Golang API](https://github.com/opentracing/api-golang).

What's here:
- The `OpenTracer` interface, including its `TraceContextSource`/etc super-interfaces. Any software that fully implements `OpenTracer` can plug in as a backend for `opentracing` instrumentation.
- `TraceContext`, which represents, well, the trace context. Unlike Dapper/Zipkin, a TraceContext in `opentracing` also handles "trace tags". Read the comments for more on this powerful/useful feature.
- `Span`, which represents an "active" / extant span object: an annotate-able microlog that can create children, essentially
- `RawSpan` and `RawLog`, which represent the underlying tracing information as it's emitted from instrumentation

What's not here:
- The "Standard Tracer" concept from the golang version: this absolutely should happen but is out of scope for this sketch. It basically takes an `opentracing` implementation's `Recorder` and `TraceContextSource` and builds a stock/standard `OpenTracer` implementation out of them. It handles the boilerplate of creating and managing `Span` implementation instances, creating `RawSpan` and `RawLog` instances, etc.
- Anything to bind `Span` objects to thread-local storage, helpers like a `TraceContextRunnable` standin for `java.lang.Runnable`, etc. These are absolutely necessary but can be done as a layer on top of what's here.
- Utility code to marshal/unmarshal to/from standard HTTP objects
- Helpers for global singletons
- A small working example (like `dapperish` from `api-golang`)
- Modern Java coding style :)

## Building

This is a maven project, execute `./mvnw clean install` to build, run tests, and create jars.
