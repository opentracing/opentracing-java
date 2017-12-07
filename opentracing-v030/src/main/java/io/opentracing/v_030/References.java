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
package io.opentracing.v_030;

/**
 * References is essentially a namespace for the official OpenTracing reference types.
 *
 * References are used by Tracer.buildSpan() to describe the relationships between Spans.
 *
 * @see Tracer.SpanBuilder#addReference(String, SpanContext)
 */
public final class References {
    private References(){}

    /**
     * See http://opentracing.io/spec/#causal-span-references for more information about CHILD_OF references
     */
    public static final String CHILD_OF = "child_of";

    /**
     * See http://opentracing.io/spec/#causal-span-references for more information about FOLLOWS_FROM references
     */
    public static final String FOLLOWS_FROM = "follows_from";
}
