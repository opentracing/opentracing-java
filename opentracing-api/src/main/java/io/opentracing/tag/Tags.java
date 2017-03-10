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
package io.opentracing.tag;

/**
 * The following span tags are recommended for instrumentors who are trying to capture more
 * semantic information about the spans. Tracers may expose additional features based on these
 * standardized data points. Tag names follow a general structure of namespacing.
 *
 * @see <a href="https://github.com/opentracing/specification/blob/master/data_conventions.yaml">https://github.com/opentracing/specification/blob/master/data_conventions.yaml</a>
 */

public final class Tags {
    private Tags() {
    }

    /**
     * A constant for setting the span kind to indicate that it represents a server span.
     */
    public static final String SPAN_KIND_SERVER = "server";

    /**
     * A constant for setting the span kind to indicate that it represents a client span.
     */
    public static final String SPAN_KIND_CLIENT = "client";

    /**
     * A constant for setting the span kind to indicate that it represents a producer span.
     */
    public static final String SPAN_KIND_PRODUCER = "producer";

    /**
     * A constant for setting the span kind to indicate that it represents a consumer span.
     */
    public static final String SPAN_KIND_CONSUMER = "consumer";

    /**
     * HTTP_URL records the url of the incoming request.
     */
    public static final StringTag HTTP_URL = new StringTag("http.url");

    /**
     * HTTP_STATUS records the http status code of the response.
     */
    public static final IntTag HTTP_STATUS = new IntTag("http.status_code");

    /**
     * HTTP_METHOD records the http method. Case-insensitive.
     */
    public static final StringTag HTTP_METHOD = new StringTag("http.method");

    /**
     * PEER_HOST_IPV4 records IPv4 host address of the peer.
     */
    public static final IntTag PEER_HOST_IPV4 = new IntTag("peer.ipv4");

    /**
     * PEER_HOST_IPV6 records the IPv6 host address of the peer.
     */
    public static final StringTag PEER_HOST_IPV6 = new StringTag("peer.ipv6");

    /**
     * PEER_SERVICE records the service name of the peer.
     */
    public static final StringTag PEER_SERVICE = new StringTag("peer.service");

    /**
     * PEER_HOSTNAME records the host name of the peer.
     */
    public static final StringTag PEER_HOSTNAME = new StringTag("peer.hostname");

    /**
     * PEER_PORT records the port number of the peer.
     */
    public static final ShortTag PEER_PORT = new ShortTag("peer.port");

    /**
     * SAMPLING_PRIORITY determines the priority of sampling this Span.
     */
    public static final ShortTag SAMPLING_PRIORITY = new ShortTag("sampling.priority");

    /**
     * SPAN_KIND hints at the relationship between spans, e.g. client/server.
     */
    public static final StringTag SPAN_KIND = new StringTag("span.kind");

    /**
     * COMPONENT is a low-cardinality identifier of the module, library, or package that is instrumented.
     */
    public static final StringTag COMPONENT = new StringTag("component");

    /**
     * ERROR indicates whether a Span ended in an error state.
     */
    public static final BooleanTag ERROR = new BooleanTag("error");

    /**
     * DB_TYPE indicates the type of Database.
     * For any SQL database, "sql". For others, the lower-case database category, e.g. "cassandra", "hbase", or "redis"
     */
    public static final StringTag DB_TYPE = new StringTag("db.type");

    /**
     * DB_INSTANCE indicates the instance name of Database.
     * If the jdbc.url="jdbc:mysql://127.0.0.1:3306/customers", instance name is "customers".
     */
    public static final StringTag DB_INSTANCE = new StringTag("db.instance");

    /**
     * DB_USER indicates the user name of Database, e.g. "readonly_user" or "reporting_user"
     */
    public static final StringTag DB_USER = new StringTag("db.user");

    /**
     * DB_STATEMENT records a database statement for the given database type.
     * For db.type="SQL", "SELECT * FROM wuser_table". For db.type="redis", "SET mykey "WuValue".
     */
    public static final StringTag DB_STATEMENT = new StringTag("db.statement");

    /**
     * MESSAGE_BUS_DESTINATION records an address at which messages can be exchanged.
     * E.g. A Kafka record has an associated "topic name" that can be extracted by the instrumented
     * producer or consumer and stored using this tag.
     */
    public static final StringTag MESSAGE_BUS_DESTINATION = new StringTag("message_bus.destination");
}
