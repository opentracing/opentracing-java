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
package io.opentracing.examples.actor_propagation;

import io.opentracing.Scope;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.tag.Tags;
import io.opentracing.util.ThreadLocalScopeManager;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;

import static io.opentracing.examples.TestUtils.getOneByTag;
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
public class ActorPropagationTest {

  private final MockTracer tracer =
      new MockTracer(new ThreadLocalScopeManager(), Propagator.TEXT_MAP);
  private Phaser phaser;

  @Before
  public void before() {
    phaser = new Phaser();
  }

  @Test
  public void testActorTell() {
    try (Actor actor = new Actor(tracer, phaser)) {
      phaser.register();
      try (Scope parent =
          tracer
              .buildSpan("actorTell")
              .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_PRODUCER)
              .withTag(Tags.COMPONENT.getKey(), "example-actor")
              .startActive(false)) {
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
      assertThat(tracer.scopeManager().active()).isNull();
    }
  }

  @Test
  public void testActorAsk() throws ExecutionException, InterruptedException {
    try (Actor actor = new Actor(tracer, phaser)) {
      phaser.register();
      Future<String> future;
      try (Scope parent =
          tracer
              .buildSpan("actorAsk")
              .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_PRODUCER)
              .withTag(Tags.COMPONENT.getKey(), "example-actor")
              .startActive(false)) {
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
      assertThat(tracer.scopeManager().active()).isNull();
    }
  }
}
