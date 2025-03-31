package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.RemoteService;

public interface ServerConfigurationService extends RemoteService {
    boolean isStandaloneServer();
}
