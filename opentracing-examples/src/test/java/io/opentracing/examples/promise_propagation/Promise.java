/*
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
package io.opentracing.examples.promise_propagation;

import io.opentracing.ActiveSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.tag.Tags;
import java.util.Collection;
import java.util.LinkedList;

/** @author tylerbenson */
public class Promise<T> {
  private final PromiseContext context;
  private final MockTracer tracer;
  private final ActiveSpan activeSpan;

  private final Collection<Pair<SuccessCallback<T>>> successCallbacks = new LinkedList<>();
  private final Collection<Pair<ErrorCallback>> errorCallbacks = new LinkedList<>();

  public Promise(PromiseContext context, MockTracer tracer) {
    this.context = context;

    // Passed along here for testing. Normally should be referenced via GlobalTracer.get().
    this.tracer = tracer;
    activeSpan = tracer.activeSpan();
  }

  public void onSuccess(SuccessCallback<T> successCallback) {
    ActiveSpan.Continuation capture = activeSpan.capture();
    successCallbacks.add(new Pair<>(capture, successCallback));
  }

  public void onError(ErrorCallback errorCallback) {
    ActiveSpan.Continuation capture = activeSpan.capture();
    errorCallbacks.add(new Pair<>(capture, errorCallback));
  }

  public void success(final T result) {
    for (final Pair<SuccessCallback<T>> pair : successCallbacks) {
      context.submit(
          new Runnable() {
            @Override
            public void run() {
              try (ActiveSpan parent = pair.capture.activate()) {
                try (ActiveSpan child =
                    tracer
                        .buildSpan("success")
                        .withTag(Tags.COMPONENT.getKey(), "success")
                        .startActive()) {
                  pair.callback.accept(result);
                }
              }
              context.getPhaser().arriveAndAwaitAdvance(); // trace reported
            }
          });
    }
  }

  public void error(final Throwable error) {
    for (final Pair<ErrorCallback> pair : errorCallbacks) {
      context.submit(
          new Runnable() {
            @Override
            public void run() {
              try (ActiveSpan parent = pair.capture.activate()) {
                try (ActiveSpan child =
                    tracer
                        .buildSpan("error")
                        .withTag(Tags.COMPONENT.getKey(), "error")
                        .startActive()) {
                  pair.callback.accept(error);
                }
              }
              context.getPhaser().arriveAndAwaitAdvance(); // trace reported
            }
          });
    }
  }

  public interface SuccessCallback<T> {
    /** @param t the result of the promise */
    void accept(T t);
  }

  public interface ErrorCallback {
    /** @param t the error result of the promise */
    void accept(Throwable t);
  }

  private class Pair<C> {

    final ActiveSpan.Continuation capture;
    final C callback;

    public Pair(ActiveSpan.Continuation capture, C callback) {
      this.capture = capture;
      this.callback = callback;
    }
  }
}
