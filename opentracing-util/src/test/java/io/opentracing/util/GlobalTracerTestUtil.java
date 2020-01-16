/*
 * Copyright 2016-2020 The OpenTracing Authors
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

import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracerFactory;

import java.lang.reflect.Field;

/**
 * Utility class for manipulating the {@link GlobalTracer} when testing.
 * <p>
 * The {@linkplain GlobalTracer} has register-once semantics, but for testing purposes it is useful to
 * manipulate the globaltracer to guarantee certain preconditions.
 * <p>
 * The {@code GlobalTracerTestUtil} can be included in your own code by adding the following dependency:
 * <pre><code>
 *     &lt;dependency>
 *         &lt;groupId>io.opentracing&lt;/groupId>
 *         &lt;artifactId>opentracing-util&lt;/artifactId>
 *         &lt;version><em>version</em>&lt;/version>
 *         <strong>&lt;type>test-jar&lt;/type></strong>
 *         &lt;scope>test&lt;/scope>
 *      &lt;/dependency>
 * </code></pre>
 */
public class GlobalTracerTestUtil {

    private GlobalTracerTestUtil() {
        throw new UnsupportedOperationException("Cannot instantiate static test utility class.");
    }

    /**
     * Resets the {@link GlobalTracer} to its initial, unregistered state.
     */
    public static void resetGlobalTracer() {
        setGlobalTracerUnconditionally(NoopTracerFactory.create());

        try {
            Field isRegisteredField = GlobalTracer.class.getDeclaredField("isRegistered");
            isRegisteredField.setAccessible(true);
            isRegisteredField.set(null, false);
            isRegisteredField.setAccessible(false);
        } catch (Exception e) {
            throw new IllegalStateException("Error reflecting GlobalTracer.tracer: " + e.getMessage(), e);
        }
    }

    /**
     * Unconditionally sets the {@link GlobalTracer} to the specified {@link Tracer tracer} instance.
     *
     * @param tracer The tracer to become the GlobalTracer's delegate.
     */
    public static void setGlobalTracerUnconditionally(Tracer tracer) {
        try {
            Field globalTracerField = GlobalTracer.class.getDeclaredField("tracer");
            globalTracerField.setAccessible(true);
            globalTracerField.set(null, tracer);
            globalTracerField.setAccessible(false);

            Field isRegisteredField = GlobalTracer.class.getDeclaredField("isRegistered");
            isRegisteredField.setAccessible(true);
            isRegisteredField.set(null, true);
            isRegisteredField.setAccessible(false);
        } catch (Exception e) {
            throw new IllegalStateException("Error reflecting GlobalTracer.tracer: " + e.getMessage(), e);
        }
    }

}
