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

import io.opentracing.Span;
import io.opentracing.impl.AbstractActiveSpan;
import io.opentracing.impl.AbstractActiveSpanSource;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * MDCActiveSpanSource illustrates the core Source concepts and capabilities to a first approximation. Not
 * production-quality code.
 */
public class MDCActiveSpanSource extends AbstractActiveSpanSource {
    final ThreadLocal<MDCActiveSpan> tlsSnapshot = new ThreadLocal<MDCActiveSpan>();

    @Override
    protected MDCActiveSpan.MDCContinuation makeContinuation(Span span, AtomicInteger refCount) {
        if (span instanceof AbstractActiveSpan) {
            throw new IllegalArgumentException("Should only makeActive the wrapped Span");
        }
        return new MDCActiveSpan.MDCContinuation(this, span, refCount);
    }

    @Override
    public MDCActiveSpan activeSpan() {
        return tlsSnapshot.get();
    }

}
