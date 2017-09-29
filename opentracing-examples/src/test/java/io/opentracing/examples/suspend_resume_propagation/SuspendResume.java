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
package io.opentracing.examples.suspend_resume_propagation;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.mock.MockTracer;
import io.opentracing.tag.Tags;

/** @author tylerbenson */
public class SuspendResume {

  private final int id;
  private final MockTracer tracer;
  private Span span;

  public SuspendResume(int id, MockTracer tracer) {
    this.id = id;

    // Passed along here for testing. Normally should be referenced via GlobalTracer.get().
    this.tracer = tracer;

    try (Scope scope =
        tracer
            .buildSpan("job " + id)
            .withTag(Tags.COMPONENT.getKey(), "suspend-resume")
            .startActive(false)) {
      span = scope.span();
    }
  }

  public void doPart(String name) {
    try (Scope scope = tracer.scopeManager().activate(span, false)) {
      scope.span().log("part: " + name);
    }
  }

  public void done() {
    span.finish();
  }
}
