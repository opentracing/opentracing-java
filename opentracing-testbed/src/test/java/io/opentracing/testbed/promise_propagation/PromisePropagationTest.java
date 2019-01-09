/*
 * Copyright 2016-2019 The OpenTracing Authors
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
package io.opentracing.testbed.promise_propagation;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.tag.Tags;
import io.opentracing.util.ThreadLocalScopeManager;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;

import static io.opentracing.testbed.TestUtils.getByTag;
import static io.opentracing.testbed.TestUtils.getOneByTag;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * These tests are intended to simulate the kind of async models that are common in java async
 * frameworks.
 *
 * For improved readability, ignore the phaser lines as those are there to ensure deterministic
 * execution for the tests without sleeps.
 *
 * @author tylerbenson
 */
public class PromisePropagationTest {

  private final MockTracer tracer =
          new MockTracer(new ThreadLocalScopeManager(), Propagator.TEXT_MAP);
  private Phaser phaser;

  @Before
  public void before() {
    phaser = new Phaser();
  }

  @Test
  public void testPromiseCallback() {
    phaser.register(); // register test thread
    final AtomicReference<String> successResult1 = new AtomicReference<>();
    final AtomicReference<String> successResult2 = new AtomicReference<>();
    final AtomicReference<Throwable> errorResult = new AtomicReference<>();
    try (PromiseContext context = new PromiseContext(phaser, 3)) {
      Span parentSpan = tracer
          .buildSpan("promises")
          .withTag(Tags.COMPONENT.getKey(), "example-promises")
          .start();
      try (Scope parentScope = tracer.activateSpan(parentSpan)) {
        Promise<String> successPromise = new Promise<>(context, tracer);

        successPromise.onSuccess(
            new Promise.SuccessCallback<String>() {
              @Override
              public void accept(String s) {
                tracer.activeSpan().log("Promised 1 " + s);
                successResult1.set(s);
                phaser.arriveAndAwaitAdvance(); // result set
              }
            });
        successPromise.onSuccess(
                new Promise.SuccessCallback<String>() {
                  @Override
                  public void accept(String s) {
                    tracer.activeSpan().log("Promised 2 " + s);
                    successResult2.set(s);
                    phaser.arriveAndAwaitAdvance(); // result set
                  }
                });

        Promise errorPromise = new Promise(context, tracer);

        errorPromise.onError(
            new Promise.ErrorCallback() {
              @Override
              public void accept(Throwable t) {
                errorResult.set(t);
                phaser.arriveAndAwaitAdvance(); // result set
              }
            });
        assertThat(tracer.finishedSpans().size()).isEqualTo(0);
        successPromise.success("success!");
        errorPromise.error(new Exception("some error."));
      } finally {
        parentSpan.finish();
      }

      phaser.arriveAndAwaitAdvance(); // wait for results to be set
      assertThat(successResult1.get()).isEqualTo("success!");
      assertThat(successResult2.get()).isEqualTo("success!");
      assertThat(errorResult.get()).hasMessage("some error.");

      phaser.arriveAndAwaitAdvance(); // wait for traces to be reported
      List<MockSpan> finished = tracer.finishedSpans();
      assertThat(finished.size()).isEqualTo(4);
      assertThat(getOneByTag(finished, Tags.COMPONENT, "example-promises")).isNotNull();
      assertThat(getOneByTag(finished, Tags.COMPONENT, "example-promises").parentId()).isEqualTo(0);
      long parentId = getOneByTag(finished, Tags.COMPONENT, "example-promises").context().spanId();
      assertThat(getByTag(finished, Tags.COMPONENT, "success")).hasSize(2);
      for (MockSpan span : getByTag(finished, Tags.COMPONENT, "success")) {
        assertThat(span.parentId()).isEqualTo(parentId);
      }
      assertThat(getOneByTag(finished, Tags.COMPONENT, "error")).isNotNull();
      assertThat(getOneByTag(finished, Tags.COMPONENT, "error").parentId()).isEqualTo(parentId);
    }
  }
}
