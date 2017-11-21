# OpenTracing-Java 0.30 compatibility layer.

The `opentracing-v030` artifact provides a 0.30 API compatibility layer which comprises:
1. Exposing all the the 0.30 packages under `io.opentracing.v_030` (`io.opentracing.v_030.propagation`, `io.opentracing.v_30.util`, etc).
2. A Shim layer to wrap 0.31 Tracer and expose it under the 0.30 API.

## Shim Layer

The basic shim layer is exposed through `TracerShim`, which wraps a `io.opentracing.Tracer` object and exposes it under the `io.opentracing.v_030.Tracer` interface:

```java
import io.opentracing.v_030.ActiveSpan;
import io.opentracing.v_030.Tracer;
import io.opentracing.v_030.shim.TracerShim;
```

For `ActiveSpan.capture()` and `Continuation`s support, the usage of `io.opentracing.util.AutoFinishScopeManager` as `Tracer.scopeManager()` is required (which preserves the reference-count system used in 0.30).

```java
import io.opentracing.util.AutoFinishScopeManager;
import io.opentracing.v_030.ActiveSpan;
import io.opentracing.v_030.Tracer;
import io.opentracing.v_030.shim.AutoFinishTracerShim;

io.opentracing.Tracer upstreamTracer = new CustomTracer(..., new AutoFinishScopeManager());
Tracer tracer = new TracerShim(yourUpstreamTracer);

try (ActiveSpan span = tracer.buildSpan("parent").startActive()) {
    ActiveSpan.Continuation cont = span.capture();
    ...
}
```

## Extending the Shim layer

When the Shim layer is required without the reference-count system, it's possible to provide a custom class extending `BaseTracerShim`, which will need to provide a custom `ActiveSpanShim` instance upong `Span` activation:

```java
import io.opentracing.util.AutoFinishScopeManager;
import io.opentracing.v_030.ActiveSpan;
import io.opentracing.v_030.Tracer;
import io.opentracing.v_030.shim.AutoFinishTracerShim;


public class CustomTracerShim extends BaseTracerShim {
    public CustomTracerShim(io.opentracing.Tracer tracer) {
        super(tracer);
    }

    @Override
    public ActiveSpanShim createActiveSpanShim(Scope scope) {
        return CustomActiveSpanShim(scope);
    }

    static final class CustomActiveSpanShim extends ActiveSpanShim {
        public CustomActiveSpanShim(Scope scope) {
            super(scope);
        }

        @Override
        public Continuation capture() {
            ...
        }
    }
}
```

The returned `ActiveSpanShim` instance must react properly to `ActiveSpan.capture()` and return a `ActiveSpan.Continuation` object than can later be reactivated. Observe the default implementation of `ActiveSpanShim.capture()` throws `UnsupportedOperationException`.

