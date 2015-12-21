package io.opentracing;

/**
 * TraceContextMarshaler is a simple interface to marshal a TraceContext to
 * binary data or a string-to-string map.
 *
 * In marshaled form, a TraceContext has two components: the "TraceContext id" and the "trace tags". The former actually
 * identifies the trace context; for instance, in Dapper or Zipkin, it would include the trace_id and span_id. The
 * latter are trace key:value pairs to be sent in-band with the application data.
 *
 * @see TraceContext#setTraceTag(String, String)
 */
public interface TraceContextMarshaler {
    MarshaledPairBinary marshalTraceContextBinary(TraceContext tc);
    MarshaledPairKeyValue marshalTraceContextKeyValue(TraceContext tc);
}
