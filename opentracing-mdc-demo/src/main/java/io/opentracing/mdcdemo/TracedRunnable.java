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

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpanSource;

public class TracedRunnable implements Runnable {
    private Runnable runnable;
    private ActiveSpan.Continuation continuation;

    public TracedRunnable(Runnable runnable, ActiveSpanSource spanSource) {
        this(runnable, spanSource.activeSpan());
    }

    public TracedRunnable(Runnable runnable, ActiveSpan handle) {
        if (runnable == null) throw new NullPointerException("Runnable is <null>.");
        this.runnable = runnable;
        this.continuation = handle.capture();
    }

    @Override
    public void run() {
        // NOTE: There's no way to be sure about the finishOnDeactivate parameter to activate(), so we play it safe.
        try (ActiveSpan handle = this.continuation.activate()) {
            runnable.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
