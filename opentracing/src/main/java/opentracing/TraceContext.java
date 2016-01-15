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
package opentracing;

/**
 * TraceContext encpasulates the smallest amount of state needed to describe a Span's identity
 * within a larger [potentially distributed] trace. The TraceContext is not intended to encode the
 * span's operation name, timing, or log data, but merely any unique identifiers (etc) needed to
 * contextualize it within a larger trace tree.
 *
 * <p>TraceContexts are sufficient to propagate the, well, *context* of a particular trace between
 * processes.
 *
 * <p>TraceContext also support a simple string map of "trace attributes". These trace attributes
 * are special in that they are propagated *in-band*, presumably alongside application data. See the
 * documentation for {@link #setAttribute(String, String)} for more details and some important
 * caveats.
 */
public interface TraceContext {

  /**
   * Sets a tag on this TraceContext that also propagates to future children per {@link
   * TraceContextSource#newChild(TraceContext)}.
   *
   * <p>Trace attributes enables powerful functionality given a full-stack opentracing integration
   * (e.g., arbitrary application data from a mobile app can make it, transparently, all the way
   * into the depths of a storage system), and with it some powerful costs: use this feature with
   * care.
   *
   * <p>IMPORTANT NOTE #1: This will only propagate trace attributes to *future* children of the
   * TraceContextSource#newChild(TraceContext)} and/or the Span that references it.
   *
   * <p>IMPORTANT NOTE #2: Use this thoughtfully and with care. Every key and value is copied into
   * every local *and remote* child of this TraceContext, and that can add up to a lot of network
   * and cpu overhead.
   *
   * <p>IMPORTANT NOTE #3: Trace attributes keys have a restricted format: implementations may wish
   * to use them as HTTP header keys (or key suffixes), and of course HTTP headers are case
   * insensitive.
   *
   * @param restrictedKey MUST match the regular expression `(?i:[a-z0-9][-a-z0-9]*)` and is
   * case-insensitive. That is, it must start with a letter or number, and the remaining characters
   * must be letters, numbers, or hyphens. undefined behavior results if the `restrictedKey` does
   * not meet these criteria.
   */
  TraceContext setAttribute(String restrictedKey, String value);

  /**
   * Gets the value for a trace tag given its key. Returns Null if the value isn't found in this
   * TraceContext.
   *
   * @param restrictedKey see {@link #setAttribute(String, String)} notes.
   */
  String getAttribute(String restrictedKey);
}
