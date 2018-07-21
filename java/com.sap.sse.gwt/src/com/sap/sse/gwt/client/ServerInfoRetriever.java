package com.sap.sse.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServerInfoRetriever {
    void getServerInfo(AsyncCallback<ServerInfoDTO> callback);
}
