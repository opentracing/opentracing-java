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
import io.opentracing.mock.MockTracer;
import io.opentracing.tag.Tags;

/** @author tylerbenson */
public class SuspendResume {

  private final int id;
  private final MockTracer tracer;
  private ActiveSpan.Continuation continuation;

  public SuspendResume(int id, MockTracer tracer) {
    this.id = id;

    // Passed along here for testing. Normally should be referenced via GlobalTracer.get().
    this.tracer = tracer;

    try (ActiveSpan span =
        tracer
            .buildSpan("job " + id)
            .withTag(Tags.COMPONENT.getKey(), "suspend-resume")
            .startActive()) {
      continuation = span.capture();
    }
  }

  public void doPart(String name) {
    try (ActiveSpan span = continuation.activate()) {
      continuation = span.capture(); // prepare for the next part.
      span.log("part: " + name);
    }
  }

  public void done() {
    continuation.activate().deactivate();
  }
}
