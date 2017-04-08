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

package io.opentracing.mdcdemo;

import io.opentracing.ActiveSpanSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TracedExecutorService implements ExecutorService {
    private ExecutorService executor;
    private ActiveSpanSource spanSource;

    public TracedExecutorService(ExecutorService executor, ActiveSpanSource spanSource) {
        if (executor == null) throw new NullPointerException("Executor is <null>.");
        if (spanSource == null) throw new NullPointerException("Source is <null>.");
        this.executor = executor;
        this.spanSource = spanSource;
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(new TracedRunnable(command, spanSource));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executor.submit(new TracedRunnable(task, spanSource));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return executor.submit(new TracedRunnable(task, spanSource), result);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(new TracedCallable(task, spanSource));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return executor.invokeAll(tasksWithTracing(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException {
        return executor.invokeAll(tasksWithTracing(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executor.invokeAny(tasksWithTracing(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        return executor.invokeAny(tasksWithTracing(tasks), timeout, unit);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return executor.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executor.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }

    private <T> Collection<? extends Callable<T>> tasksWithTracing(
        Collection<? extends Callable<T>> tasks) {
        if (tasks == null) throw new NullPointerException("Collection of tasks is <null>.");
        Collection<Callable<T>> result = new ArrayList<Callable<T>>(tasks.size());
        for (Callable<T> task : tasks) result.add(new TracedCallable(task, spanSource));
        return result;
    }
}
