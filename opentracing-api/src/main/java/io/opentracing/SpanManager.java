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
package io.opentracing;

/**
 * @author Pavol Loffay
 */
public interface SpanManager {

    /**
     * @param span span to bundle into visibility, there is always only one visibility per span
     * @return visibility
     */
    Visibility bundle(Span span);

    /**
     * @return not finished active span or null
     */
    VisibilityContext active();

    interface Visibility {
        /**
         * @return visibility context which is used to activate/deactivate span
         */
        VisibilityContext capture();

        /**
         * @return associated span or null if visibility is marked as finished.
         */
        Span span();
        /**
         * @return always spanContext
         */
        SpanContext context();

        /**
         * Mark associated span as finished.
         *
         * Should be called by {@link Span#finish()} or directly if one does not want to expose span.
         * This method should be idempotent.
         *
         * review note: reverse operation should not be allowed.
         */
        void hideSpan();
    }

    interface VisibilityContext {
        /**
         * on/activate - {@link SpanManager#active()} will return this object.
         */
        VisibilityContext on();
        /**
         * off/deactivate - {@link SpanManager#active()} will not return this object.
         */
        VisibilityContext off();

        Visibility visibility();
    }
}
