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
package io.opentracing.ext;

/**
 * The following span tags are recommended for instrumentors who are trying to capture more
 * semantic information about the spans. Tracers may expose additional features based on this
 * standardized data points. Tag names follow a general structure of namespacing.
 *
 * @see http://opentracing.io/data-semantics/
 */
public class Tags {
    /**
     *  HttpUrl records the url of the incoming request.
     */
    public static final StringTag HttpUrl = new StringTag("http.url");

    /**
     *  HttpStatus records the http status code of the response
     */
    public static final IntTag HttpStatus = new IntTag("http.status_code");

    /**
     *  PeerHostIPv4 records IP v4 host address of the peer.
     */
    public static final IntTag PeerHostIPv4 = new IntTag("peer.ipv4");

    /**
     *  PeerHostIPv6 records the IP v6 hostof the peer
     */
    public static final StringTag PeerHostIPv6 = new StringTag("peer.ipv6");

    /**
     *  PeerService records the service name of the peer
     */
    public static final StringTag PeerService = new StringTag("peer.service");

    /**
     * PeerHostname records the host name of the peer
     */
    public static final StringTag PeerHostname = new StringTag("peer.hostname");

    /**
     *  PeerPort records the port number of the peer
     */
    public static final ShortTag PeerPort = new ShortTag("peer.port");

    /**
     *  SamplingPriority determines the priority of sampling this Span.
     */
    public static final ShortTag SamplingPriority = new ShortTag("sampling.priority");

    /**
     *  SpanKind hints at the relationship between spans, e.g. client/server
     */
    public static final StringTag SpanKind = new StringTag("span.kind");

    /**
     *  Component is a low-cardinality identifier of the module, library, or package that is instrumented.
     */
    public static final StringTag Component  = new StringTag("component");
}
