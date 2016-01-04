package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of {@link ServerConfigurationService}
 */
public interface ServerConfigurationServiceAsync {
    void isStandaloneServer(AsyncCallback<Boolean> callback);
}
