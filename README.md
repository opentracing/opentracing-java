# api-java

Move along, nothing to see here (yet!)... Non-master branch in poor Java style will illustrate some core concepts for those who don't grok Golang.

## Notes about the io.opentracing.* sketch

This is an intentionally naive port of the [Golang API](https://github.com/opentracing/api-golang).

What's here:
- The `OpenTracer` interface, including its `TraceContextSource`/etc super-interfaces. Any software that fully implements `OpenTracer` can plug in as a backend for `opentracing` instrumentation.
- `RawSpan` and `RawLog`, which represent the underlying tracing information as it's emitted from instrumentation
- 

