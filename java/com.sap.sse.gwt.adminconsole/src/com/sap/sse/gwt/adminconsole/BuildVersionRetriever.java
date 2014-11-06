package com.sap.sse.gwt.adminconsole;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface BuildVersionRetriever {
    void getBuildVersion(AsyncCallback<String> callback);
}
