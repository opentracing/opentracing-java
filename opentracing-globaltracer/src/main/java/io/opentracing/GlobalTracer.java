/**
 * Copyright 2016-2017 The OpenTracing Authors
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
package io.opentracing;

import io.opentracing.propagation.Format;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Forwards all methods to another tracer that can be configured in one of two ways:
 * <ol>
 * <li>Explicitly, calling {@link #register(Tracer)} with a configured tracer, or:</li>
 * <li>Automatically using the Java {@link ServiceLoader} SPI mechanism to load a {@link Tracer} from the classpath.</li>
 * </ol>
 * <p>
 * When the tracer is needed it is lazily looked up using the following rules:
 * <ol type="a">
 * <li>The last {@link #register(Tracer) registered} or {@link #update(UpdateFunction) updated} tracer
 * always takes precedence.</li>
 * <li>If no tracer was registered, one is looked up from the {@link ServiceLoader}.<br>
 * The {@linkplain GlobalTracer} will not attempt to choose between implementations:</li>
 * <li>If no single tracer service is found, the {@link io.opentracing.NoopTracer NoopTracer} will be used.</li>
 * </ol>
 */
public final class GlobalTracer implements Tracer {
    private static final Logger LOGGER = Logger.getLogger(GlobalTracer.class.getName());

    /**
     * Function to update the global tracer.
     * <p>
     * In Java 8 terms, this would be a {@code Function<Tracer, Tracer>}.
     *
     * @see GlobalTracer#update(UpdateFunction)
     */
    public interface UpdateFunction {
        /**
         * Update the registered global {@link Tracer} instance.
         * <p>
         * This allows updating the {@linkplain GlobalTracer} with a {@linkplain Tracer}
         * that is 'based on' the current registered tracer, allowing delegation or wrapping
         * tracers to be registered independently from underlying implementaitons.
         *
         * @param current The current GlobalTracer implementation (never <code>null</code>).
         * @return The tracer to become the new GlobalTracer implementation, must be non-null.
         */
        Tracer apply(Tracer current);
    }

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
     * The resolved {@link Tracer} to delegate to.
     * <p>
     * This can be either an {@link #register(Tracer) explicitly registered} or
     * the {@link #loadSingleSpiImplementation() automatically resolved} Tracer
     * (or <code>null</code> during initialization).
     */
    private final AtomicReference<Tracer> globalTracer = new AtomicReference<Tracer>();

    private GlobalTracer() {
    }

    private Tracer lazyTracer() {
        Tracer tracer = globalTracer.get();
        if (tracer == null) {
            final Tracer resolved = loadSingleSpiImplementation();
            while (tracer == null && resolved != null) { // handle rare race condition
                globalTracer.compareAndSet(null, resolved);
                tracer = globalTracer.get();
            }
            LOGGER.log(Level.INFO, "Using GlobalTracer: {0}.", tracer);
        }
        return tracer;
    }

    /**
     * Returns the constant {@linkplain GlobalTracer}.
     * <p>
     * All methods are forwarded to the currently configured tracer.<br>
     * Until a tracer is {@link #register(Tracer) explicitly configured},
     * one is looked up from the {@link ServiceLoader},
     * falling back to the {@link io.opentracing.NoopTracer NoopTracer}.<br>
     * A tracer can be re-configured at any time.
     * For example, the tracer used to extract a span may be different than the one that injects it.
     *
     * @return The global tracer constant.
     * @see #register(Tracer)
     */
    public static Tracer get() {
        return INSTANCE;
    }

    /**
     * Explicitly configures a {@link Tracer} to back the behaviour of the {@link #get() global tracer}.
     * <p>
     * The previous global tracer is returned so it can be restored later if necessary.
     *
     * @param tracer Tracer to use as global tracer.
     * @return The previous global tracer or <code>null</code> if there was none.
     */
    public static Tracer register(final Tracer tracer) {
        if (tracer instanceof GlobalTracer) {
            LOGGER.log(Level.FINE, "Attempted to register the GlobalTracer as delegate of itself.");
            return INSTANCE.globalTracer.get(); // no-op, return 'previous' tracer.
        }
        Tracer previous = INSTANCE.globalTracer.getAndSet(tracer);
        LOGGER.log(Level.INFO, "Registered GlobalTracer {0} (previously {1}).", new Object[]{tracer, previous});
        return previous;
    }

    /**
     * Updates the global tracer using the specified {@link UpdateFunction}.
     * <p>
     * If the update encounters a race condition, the 'losing' update function is re-applied with the 'winning' tracer.
     * To avoid concurrency 'hotspins', please make sure the {@linkplain UpdateFunction} is reasonably quick.
     * <p>
     * If there are concurrent modifications of the globaltracer (e.g. by a race-condition with a call to
     * {@link #register(Tracer) register}, the update function will be)
     *
     * @param updateFunction The function to update the globaltracer implementation with.
     */
    public static void update(UpdateFunction updateFunction) {
        if (updateFunction != null) {
            Tracer current, updated;
            do {
                current = INSTANCE.lazyTracer();
                updated = updateFunction.apply(current);
                if (updated == null) throw new NullPointerException("Updated tracer may not be <null>.");
                else if (updated instanceof GlobalTracer) {
                    LOGGER.log(Level.FINE, "Attempted to update the GlobalTracer with itself.");
                    return;
                }
            } while (!INSTANCE.globalTracer.compareAndSet(current, updated)); // 'while' handles race condition.
            LOGGER.log(Level.INFO, "Updated GlobalTracer {0} (previously {1}).", new Object[]{updated, current});
        }
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return lazyTracer().buildSpan(operationName);
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        lazyTracer().inject(spanContext, format, carrier);
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        return lazyTracer().extract(format, carrier);
    }

    /**
     * Loads a single service implementation from {@link ServiceLoader}.
     *
     * @return The single service or a NoopTracer.
     */
    private static Tracer loadSingleSpiImplementation() {
        // Use the ServiceLoader to find the declared Tracer implementation.
        Iterator<Tracer> spiImplementations =
                ServiceLoader.load(Tracer.class, Tracer.class.getClassLoader()).iterator();
        if (spiImplementations.hasNext()) {
            Tracer foundImplementation = spiImplementations.next();
            if (!spiImplementations.hasNext()) {
                return foundImplementation;
            }
            LOGGER.log(Level.WARNING, "More than one Tracer service found. " +
                    "Falling back to NoopTracer implementation.");
        }
        return NoopTracerFactory.create();
    }

}
