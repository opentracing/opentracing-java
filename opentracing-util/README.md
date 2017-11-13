# OpenTracing-Java utilities

The `opentracing-util` artifact contains utility classes that are not part of the OpenTracing API
but can provide useful features to OpenTracing users.

## GlobalTracer

The `io.opentracing.util.GlobalTracer` has a static `Tracer` reference that can be
used throughout an application or from independent instrumentation libraries.

It defines the following methods:
- `GlobalTracer.get()` : Returns the constant global tracer.  
   All methods are forwarded to the currently configured tracer.  
   Until a tracer is explicitly registered, the `NoopTracer` will be used.
- `GlobalTracer.register(Tracer)` : Register a `Tracer` to become the global tracer.  
   Registration is a one-time operation, attempting to call it more often will result in a runtime exception.
   Every application intending to use the global tracer is responsible for registering it during its initialization.
- `GlobalTracer.isRegistered` : Identify whether a `Tracer` has previously been registered.  
   This check is useful in scenarios where more than one component may be responsible
   for registering a tracer. For example, when using a Java Agent, it will need to determine
   if the application has already registered a tracer, and if not attempt to resolve and
   register one itself.

### Using the GlobalTracer

**Application intialization**

Initialize a new tracer from the application configuration and let it to become the GlobalTracer for the application:

```java
Tracer configuredTracer = applicationConfiguration.buildConfiguredTracer();
GlobalTracer.register(configuredTracer);
```

**Tracing some block of code**

Once initialized, all application code can instrument tracing by starting new spans like:

```java
try (Scope scope = GlobalTracer.get().buildSpan("someOperation").startActive()) {
    // ... Traced block of code ...
}
```

If no GlobalTracer is configured, this code will not throw any exceptions. Tracing is simply delegated to the NoopTracer instead.
