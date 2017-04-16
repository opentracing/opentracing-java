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
package io.opentracing.spanmanager.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.opentracing.SpanManager;

/**
 * @author Pavol Loffay
 */
public class TracedExecutorService implements ExecutorService {

    private ExecutorService wrapped;
    private SpanManager spanManager;

    public TracedExecutorService(ExecutorService wrapped, SpanManager spanManager) {
        this.wrapped = wrapped;
        this.spanManager = spanManager;
    }

    @Override
    public boolean isTerminated() {
        return wrapped.isTerminated();
    }

    @Override
    public void shutdown() {
        wrapped.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return wrapped.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return wrapped.isShutdown();
    }

    @Override
    public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
        return wrapped.awaitTermination(l, timeUnit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return wrapped.submit(new TracedCallable<T>(callable, spanManager));
    }

    @Override
    public <T> Future<T> submit(Runnable runnable, T t) {
        return wrapped.submit(new TracedRunnable(runnable, spanManager), t);
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        return wrapped.submit(new TracedRunnable(runnable, spanManager));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection) throws InterruptedException {
        return wrapped.invokeAll(tracedCallables(collection));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit)
            throws InterruptedException {
        return wrapped.invokeAll(tracedCallables(collection), l, timeUnit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> collection)
            throws InterruptedException, ExecutionException {
        return wrapped.invokeAny(tracedCallables(collection));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return wrapped.invokeAny(tracedCallables(collection), l, timeUnit);
    }

    @Override
    public void execute(Runnable runnable) {
        wrapped.submit(new TracedRunnable(runnable, spanManager));
    }

    private <T> Collection<? extends Callable<T>> tracedCallables(Collection<? extends Callable<T>> callables) {
        List<Callable<T>> tracedCallables = new ArrayList<Callable<T>>(callables.size());

        for (Callable<T> callable: callables) {
            tracedCallables.add(new TracedCallable<T>(callable, spanManager));
        }

        return tracedCallables;
    }
}
