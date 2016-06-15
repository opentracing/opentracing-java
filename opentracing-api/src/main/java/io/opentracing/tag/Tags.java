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
package io.opentracing.tag;

/**
 * The following span tags are recommended for instrumentors who are trying to capture more
 * semantic information about the spans. Tracers may expose additional features based on these
 * standardized data points. Tag names follow a general structure of namespacing.
 *
 * @see http://opentracing.io/data-semantics/
 */

public final class Tags {
    private Tags(){}

    /**
    * A constant for setting the span kind to indicate that it represents a server span.
    */
    public static final String SPAN_KIND_SERVER = "server";

    /**
     * A constant for setting the span kind to indicate that it represents a client span.
     */
    public static final String SPAN_KIND_CLIENT = "client";

    /**
     *  HTTP_URL records the url of the incoming request.
     */
    public static final StringTag HTTP_URL = new StringTag("http.url");

    /**
     *  HTTP_STATUS records the http status code of the response.
     */
    public static final IntTag HTTP_STATUS = new IntTag("http.status_code");

    /**
     *  PEER_HOST_IPV4 records IPv4 host address of the peer.
     */
    public static final IntTag PEER_HOST_IPV4 = new IntTag("peer.ipv4");

    /**
     *  PEER_HOST_IPV6 records the IPv6 host address of the peer.
     */
    public static final StringTag PEER_HOST_IPV6 = new StringTag("peer.ipv6");

    /**
     *  PEER_SERVICE records the service name of the peer.
     */
    public static final StringTag PEER_SERVICE = new StringTag("peer.service");

    /**
     * PEER_HOSTNAME records the host name of the peer.
     */
    public static final StringTag PEER_HOSTNAME = new StringTag("peer.hostname");

    /**
     *  PEER_PORT records the port number of the peer.
     */
    public static final ShortTag PEER_PORT = new ShortTag("peer.port");

    /**
     *  SAMPLING_PRIORITY determines the priority of sampling this Span.
     */
    public static final ShortTag SAMPLING_PRIORITY = new ShortTag("sampling.priority");

    /**
     *  SPAN_KIND hints at the relationship between spans, e.g. client/server.
     */
    public static final StringTag SPAN_KIND = new StringTag("span.kind");

    /**
     *  COMPONENT is a low-cardinality identifier of the module, library, or package that is instrumented.
     */
    public static final StringTag COMPONENT  = new StringTag("component");

    /**
     * ERROR indicates whether the result of a span representing an rpc call returned an error.
     */
    public static final BooleanTag ERROR = new BooleanTag("error");
}
