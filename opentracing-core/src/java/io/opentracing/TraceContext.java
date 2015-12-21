package io.opentracing;

public interface TraceContext {

    // XXX: not a Java-esque way to model this!
    public class ChildContextAndTags {
        public TraceContext childContext;
        public Tags initialTags;
    }

    /**
     * newChild creates a child context for this TraceContext, and returns both that child's own TraceContext as well as any Tags that should be added to the child's Span.
     *
     * The returned TraceContext type must be the same as the type of the TraceContext implementation itself.
     */
    ChildContextAndTags newChild();

    /**
     * setTraceTag sets a tag on this TraceContext that also propagates to
     * future TraceContext children per TraceContext.newChild().
     *
     * setTraceTag() enables powerful functionality given a full-stack
     * opentracing integration (e.g., arbitrary application data from a mobile
     * app can make it, transparently, all the way into the depths of a storage
     * system), and with it some powerful costs: use this feature with care.
     *
     * IMPORTANT NOTE #1: setTraceTag() will only propagate trace tags to
     * *future* children of the TraceContext (see newChild()) and/or the
     * Span that references it.
     *
     * IMPORTANT NOTE #2: Use this thoughtfully and with care. Every key and
     * value is copied into every local *and remote* child of this
     * TraceContext, and that can add up to a lot of network and cpu
     * overhead.
     *
     * IMPORTANT NOTE #3: Trace tags are case-insensitive: implementations may
     * wish to use them as HTTP header keys (or key suffixes), and of course
     * HTTP headers are case insensitive.
     *
     * @param caseInsensitiveKey a case-insensitive map key
     * @param value an arbitrary string value
     * @return this TraceContext (for chaining, etc)
     */
    TraceContext setTraceTag(String caseInsensitiveKey, String value);
    String getTraceTag(String caseInsensitiveKey);

}