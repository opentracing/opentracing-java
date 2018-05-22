/*
 * Copyright 2016-2018 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.util;

import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracer;
import io.opentracing.noop.NoopTracerFactory;
import io.opentracing.propagation.Format;

import java.util.concurrent.Callable;

/**
 * Global tracer that forwards all methods to another tracer that can be
 * configured by calling {@link #register(Tracer)}.
 *
 * <p>
 * The {@linkplain #register(Tracer) register} method should only be called once
 * during the application initialization phase.<br>
 * If the {@linkplain #register(Tracer) register} method is never called,
 * the default {@link NoopTracer} is used.
 *
 * <p>
 * Where possible, use some form of dependency injection (of which there are
 * many) to access the `Tracer` instance. For vanilla application code, this is
 * often reasonable and cleaner for all of the usual DI reasons.
 *
 * <p>
 * That said, instrumentation for packages that are themselves statically
 * configured (e.g., JDBC drivers) may be unable to make use of said DI
 * mechanisms for {@link Tracer} access, and as such they should fall back on
 * {@link GlobalTracer}. By and large, OpenTracing instrumentation should
 * always allow the programmer to specify a {@link Tracer} instance to use for
 * instrumentation, though the {@link GlobalTracer} is a reasonable fallback or
 * default value.
 */
public final class GlobalTracer implements Tracer {

    /**
     * Singleton instance.
     * <p>
     * Since we cannot prevent people using {@linkplain #get() GlobalTracer.get()} as a constant,
     * this guarantees that references obtained before, during or after initialization
     * all behave as if obtained <em>after</em> initialization once properly initialized.<br>
     * As a minor additional benefit it makes it harder to circumvent the {@link Tracer} API.
     */
    private static final GlobalTracer INSTANCE = new GlobalTracer();

    /**
     * The registered {@link Tracer} delegate or the {@link NoopTracer} if none was registered yet.
     * Never {@code null}.
     */
    private static volatile Tracer tracer = NoopTracerFactory.create();

    private GlobalTracer() {
    }

    /**
     * Returns the constant {@linkplain GlobalTracer}.
     * <p>
     * All methods are forwarded to the currently configured tracer.<br>
     * Until a tracer is {@link #register(Tracer) explicitly configured},
     * the {@link io.opentracing.noop.NoopTracer NoopTracer} is used.
     *
     * @return The global tracer constant.
     * @see #register(Tracer)
     */
    public static Tracer get() {
        return INSTANCE;
    }

    /**
     * Identify whether a {@link Tracer} has previously been registered.
     * <p>
     * This check is useful in scenarios where more than one component may be responsible
     * for registering a tracer. For example, when using a Java Agent, it will need to determine
     * if the application has already registered a tracer, and if not attempt to resolve and
     * register one itself.
     *
     * @return Whether a tracer has been registered
     */
    public static synchronized boolean isRegistered() {
        return !(GlobalTracer.tracer instanceof NoopTracer);
    }

    /**
     * Register a {@link Tracer} to back the behaviour of the {@link #get() global tracer}.
     * <p>
     * The tracer is provided through a {@linkplain Callable} that will only be called if the global tracer is absent.
     * Registration is a one-time operation. Once a tracer has been registered, all attempts at re-registering
     * will return {@code false}.
     * <p>
     * Every application intending to use the global tracer is responsible for registering it once
     * during its initialization.
     *
     * @param provider Provider for the tracer to use as global tracer.
     * @return {@code true} if the provided tracer was registered as a result of this call,
     * {@code false} otherwise.
     * @throws NullPointerException  if the tracer provider is {@code null} or provides a {@code null} Tracer.
     * @throws RuntimeException      any exception thrown by the provider gets rethrown,
     *                               checked exceptions will be wrapped into appropriate runtime exceptions.
     */
    public static synchronized boolean registerIfAbsent(final Callable<Tracer> provider) {
        requireNonNull(provider, "Cannot register GlobalTracer from provider <null>.");
        if (!isRegistered()) try {
            final Tracer suppliedTracer = requireNonNull(provider.call(), "Cannot register GlobalTracer <null>.");
            if (!(suppliedTracer instanceof GlobalTracer)) {
                GlobalTracer.tracer = suppliedTracer;
                return true;
            }
        } catch (RuntimeException rte) {
            throw rte; // Re-throw as-is
        } catch (Exception ex) {
            throw new IllegalStateException("Exception obtaining tracer from provider: " + ex.getMessage(), ex);
        }
        return false;
    }

    /**
     * Register a {@link Tracer} to back the behaviour of the {@link #get() global tracer}.
     * <p>
     * Registration is a one-time operation, attempting to call it more often will result in a runtime exception.
     * <p>
     * Every application intending to use the global tracer is responsible for registering it once
     * during its initialization.
     *
     * @param tracer Tracer to use as global tracer.
     * @throws RuntimeException if there is already a current tracer registered
     * @see #registerIfAbsent(Callable)
     * @deprecated Please use 'registerIfAbsent' instead which does not attempt a double registration.
     */
    @Deprecated
    public static void register(final Tracer tracer) {
        if (!registerIfAbsent(provide(tracer))
                && !tracer.equals(GlobalTracer.tracer)
                && !(tracer instanceof GlobalTracer)) {
            throw new IllegalStateException("There is already a current global Tracer registered.");
        }
    }

    @Override
    public ScopeManager scopeManager() {
        return tracer.scopeManager();
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return tracer.buildSpan(operationName);
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        tracer.inject(spanContext, format, carrier);
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        return tracer.extract(format, carrier);
    }

    @Override
    public Span activeSpan() {
        return tracer.activeSpan();
    }

    @Override
    public String toString() {
        return GlobalTracer.class.getSimpleName() + '{' + tracer + '}';
    }

    private static Callable<Tracer> provide(final Tracer tracer) {
        return new Callable<Tracer>() {
            public Tracer call() {
                return tracer;
            }
        };
    }

    private static <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
        return value;
    }
}
