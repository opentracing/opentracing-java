/**
 * Copyright 2016 The OpenTracing Authors
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
 * A Reference pairs a reference type with a SpanContext referee.
 *
 * References are used by Tracer.buildSpan() to describe the relationships
 between Spans.
 */
public class Reference {
    private Type type;
    private SpanContext referee;

    public enum Type {
        CHILD_OF,
        FOLLOWS_FROM,
    }

    /**
     * Construct a new Reference that describes the relationship between two Spans: an implicit "referring" span and an explicitly-specified "referee" being referred to.
     *
     * @param type the Reference.Type that describes the referring Span in terms of the referee
     * @param referee the SpanContext being referred to
     */
    public Reference(Type type, SpanContext referee) {
        this.type = type;
        this.referee = referee;
    }

    /**
     * @return the Reference.Type for this reference.
     */
    public Type type() { return this.type; }

    /**
     * @return the SpanContext referred to by this reference.
     */
    public SpanContext referee() { return this.referee; }

    /**
     * A shorthand for constructing CHILD_OF Reference instances.
     */
    public static Reference childOf(SpanContext referee) {
        return new Reference(Type.CHILD_OF, referee);
    }

    /**
     * A shorthand for constructing FOLLOWS_FROM Reference instances.
     */
    public static Reference followsFrom(SpanContext referee) {
        return new Reference(Type.FOLLOWS_FROM, referee);
    }
}
