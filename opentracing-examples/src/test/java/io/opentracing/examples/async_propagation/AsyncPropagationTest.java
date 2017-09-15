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
package io.opentracing.examples.async_propagation;

import io.opentracing.ActiveSpan;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.tag.Tags;
import io.opentracing.util.ThreadLocalActiveSpanSource;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicReference;

import static io.opentracing.examples.TestUtils.getOneByTag;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * These tests are intended to simulate the kind of async models that are common in java async
 * frameworks.
 *
 * <ul>
 *   <li>Actor ask/tell
 *   <li>Promises with callbacks
 *   <li>Work split by suspend/resume
 * </ul>
 *
 * For improved readability, ignore the phaser lines as those are there to ensure deterministic
 * execution for the tests without sleeps.
 *
 * @author tylerbenson
 */
public class AsyncPropagationTest {

  private final MockTracer tracer =
      new MockTracer(new ThreadLocalActiveSpanSource(), Propagator.TEXT_MAP);
  private Phaser phaser;

  @Before
  public void before() {
    phaser = new Phaser();
  }

  @Test
  public void testActorTell() {
    try (Actor actor = new Actor(tracer, phaser)) {
      phaser.register();
      try (ActiveSpan parent =
          tracer
              .buildSpan("actorTell")
              .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_PRODUCER)
              .withTag(Tags.COMPONENT.getKey(), "example-actor")
              .startActive()) {
        actor.tell("my message");
      }
      phaser.arriveAndAwaitAdvance(); // child tracer started
      assertThat(tracer.finishedSpans().size()).isEqualTo(0);
      phaser.arriveAndAwaitAdvance(); // continue...

      phaser.arriveAndAwaitAdvance(); // child tracer finished
      assertThat(tracer.finishedSpans().size()).isEqualTo(1);
      assertThat(getOneByTag(tracer.finishedSpans(), Tags.SPAN_KIND, Tags.SPAN_KIND_CONSUMER))
          .isNotNull();
      phaser.arriveAndAwaitAdvance(); // continue...

      phaser.arriveAndAwaitAdvance(); // parent tracer finished
      List<MockSpan> finished = tracer.finishedSpans();
      phaser.arriveAndDeregister(); // continue...

      assertThat(finished.size()).isEqualTo(2);
      assertThat(finished.get(0).context().traceId())
          .isEqualTo(finished.get(1).context().traceId());
      assertThat(getOneByTag(finished, Tags.SPAN_KIND, Tags.SPAN_KIND_CONSUMER)).isNotNull();
      assertThat(getOneByTag(finished, Tags.SPAN_KIND, Tags.SPAN_KIND_PRODUCER)).isNotNull();
      assertThat(tracer.activeSpan()).isNull();
    }
  }

  @Test
  public void testActorAsk() throws ExecutionException, InterruptedException {
    try (Actor actor = new Actor(tracer, phaser)) {
      phaser.register();
      Future<String> future;
      try (ActiveSpan parent =
          tracer
              .buildSpan("actorAsk")
              .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_PRODUCER)
              .withTag(Tags.COMPONENT.getKey(), "example-actor")
              .startActive()) {
        future = actor.ask("my message");
      }
      phaser.arriveAndAwaitAdvance(); // child tracer started
      assertThat(tracer.finishedSpans().size()).isEqualTo(0);
      phaser.arriveAndAwaitAdvance(); // continue...

      phaser.arriveAndAwaitAdvance(); // child tracer finished
      assertThat(tracer.finishedSpans().size()).isEqualTo(1);
      assertThat(getOneByTag(tracer.finishedSpans(), Tags.SPAN_KIND, Tags.SPAN_KIND_CONSUMER))
          .isNotNull();
      phaser.arriveAndAwaitAdvance(); // continue...

      phaser.arriveAndAwaitAdvance(); // parent tracer finished
      List<MockSpan> finished = tracer.finishedSpans();
      phaser.arriveAndDeregister(); // continue...

      String message = future.get(); // This really should be a non-blocking callback...
      assertThat(message).isEqualTo("received my message");
      assertThat(finished.size()).isEqualTo(2);
      assertThat(finished.get(0).context().traceId())
          .isEqualTo(finished.get(1).context().traceId());
      assertThat(getOneByTag(finished, Tags.SPAN_KIND, Tags.SPAN_KIND_CONSUMER)).isNotNull();
      assertThat(getOneByTag(finished, Tags.SPAN_KIND, Tags.SPAN_KIND_PRODUCER)).isNotNull();
      assertThat(tracer.activeSpan()).isNull();
    }
  }

  @Test
  public void testPromiseCallback() {
    phaser.register(); // register test thread
    final AtomicReference<String> successResult = new AtomicReference<>();
    final AtomicReference<Throwable> errorResult = new AtomicReference<>();
    try (PromiseContext context = new PromiseContext(phaser, 2)) {
      try (ActiveSpan parent =
          tracer
              .buildSpan("promises")
              .withTag(Tags.COMPONENT.getKey(), "example-promises")
              .startActive()) {

        Promise<String> promise1 = new Promise<>(context, tracer);

        promise1.onSuccess(
            new Promise.SuccessCallback<String>() {
              @Override
              public void accept(String s) {
                successResult.set(s);
                phaser.arriveAndAwaitAdvance(); // result set
              }
            });

        Promise promise2 = new Promise(context, tracer);

        promise2.onError(
            new Promise.ErrorCallback() {
              @Override
              public void accept(Throwable t) {
                errorResult.set(t);
                phaser.arriveAndAwaitAdvance(); // result set
              }
            });
        assertThat(tracer.finishedSpans().size()).isEqualTo(0);
        promise1.success("success!");
        promise2.error(new Exception("some error."));
      }

      phaser.arriveAndAwaitAdvance(); // wait for results to be set
      assertThat(successResult.get()).isEqualTo("success!");
      assertThat(errorResult.get()).hasMessage("some error.");

      phaser.arriveAndAwaitAdvance(); // wait for traces to be reported
      List<MockSpan> finished = tracer.finishedSpans();
      assertThat(finished.size()).isEqualTo(3);
      assertThat(getOneByTag(finished, Tags.COMPONENT, "example-promises")).isNotNull();
      assertThat(getOneByTag(finished, Tags.COMPONENT, "example-promises").parentId()).isEqualTo(0);
      long parentId = getOneByTag(finished, Tags.COMPONENT, "example-promises").context().spanId();
      assertThat(getOneByTag(finished, Tags.COMPONENT, "success")).isNotNull();
      assertThat(getOneByTag(finished, Tags.COMPONENT, "success").parentId()).isEqualTo(parentId);
      assertThat(getOneByTag(finished, Tags.COMPONENT, "error")).isNotNull();
      assertThat(getOneByTag(finished, Tags.COMPONENT, "error").parentId()).isEqualTo(parentId);
    }
  }

  @Test
  public void testContinuationInterleaving() {
    SuspendResume job1 = new SuspendResume(1, tracer);
    SuspendResume job2 = new SuspendResume(2, tracer);

    // Pretend that the framework is controlling actual execution here.
    job1.doPart("some work for 1");
    job2.doPart("some work for 2");
    job1.doPart("other work for 1");
    job2.doPart("other work for 2");
    job2.doPart("more work for 2");
    job1.doPart("more work for 1");

    job1.done();
    job2.done();

    List<MockSpan> finished = tracer.finishedSpans();
    assertThat(finished.size()).isEqualTo(2);

    assertThat(finished.get(0).operationName()).isEqualTo("job 1");
    assertThat(finished.get(1).operationName()).isEqualTo("job 2");

    assertThat(finished.get(0).parentId()).isEqualTo(0);
    assertThat(finished.get(1).parentId()).isEqualTo(0);
  }
}
