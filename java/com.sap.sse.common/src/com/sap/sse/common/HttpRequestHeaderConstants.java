package com.sap.sse.common;

import com.sap.sse.common.Util.Pair;

/**
 * Contains constants that can be used to parameterize HTTP(S) requests with header fields,
 * to pass on the client's locale (slightly GWT specific), as well as for controlling whether
 * the request may be handled by replicas or must be handled by master.
 * 
 * @author Georg Herdt
 * @author Axel Uhl (D043530)
 *
 */
public interface HttpRequestHeaderConstants {
    String HEADER_LOCALE = "X-Client-Locale";
    String HEADER_KEY_FORWARD_TO = "X-SAPSSE-Forward-Request-To";
    String HEADER_DEFAULT_TENANT_GROUP_ID = "tenantGroupId";
    Pair<String, String> HEADER_FORWARD_TO_MASTER = new Pair<>(HEADER_KEY_FORWARD_TO, "master");
    Pair<String, String> HEADER_FORWARD_TO_REPLICA = new Pair<>(HEADER_KEY_FORWARD_TO, "replica");
}
