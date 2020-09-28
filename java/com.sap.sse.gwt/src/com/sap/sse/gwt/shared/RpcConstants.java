package com.sap.sse.gwt.shared;

import com.sap.sse.common.Util.Pair;

public interface RpcConstants {
    public static final String HEADER_LOCALE = "X-Client-Locale";
    static final String HEADER_KEY_FORWARD_TO = "X-SAPSSE-Forward-Request-To";
    public static final Pair<String, String> HEADER_FORWARD_TO_MASTER = new Pair<>(HEADER_KEY_FORWARD_TO, "master");
    public static final Pair<String, String> HEADER_FORWARD_TO_REPLICA = new Pair<>(HEADER_KEY_FORWARD_TO, "replica");
}
