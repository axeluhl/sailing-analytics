package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sap.sailing.gwt.ui.shared.EventDAO;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("sailing")
public interface SailingService extends RemoteService {
    EventDAO[] listEvents();
}
