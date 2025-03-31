package com.sap.sse.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CrossDomainStorageConfigurationServiceAsync {
    void getAcceptableCrossDomainStorageRequestOriginRegexp(AsyncCallback<String> callback);
}
