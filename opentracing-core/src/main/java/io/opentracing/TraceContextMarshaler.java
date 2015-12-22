/**
 * Copyright 2015 The OpenTracing Authors
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
 * TraceContextMarshaler is a simple interface to marshal a TraceContext to binary data or a
 * string-to-string map.
 *
 * In marshaled form, a TraceContext has two components: the "TraceContext id" and the "trace tags".
 * The former actually identifies the trace context; for instance, in Dapper or Zipkin, it would
 * include the trace_id and span_id. The latter are trace key:value pairs to be sent in-band with
 * the application data.
 *
 * @see TraceContext#setTraceTag(String, String)
 */
public interface TraceContextMarshaler {
  MarshaledPairBinary marshalTraceContextBinary(TraceContext tc);

  MarshaledPairKeyValue marshalTraceContextKeyValue(TraceContext tc);
}
