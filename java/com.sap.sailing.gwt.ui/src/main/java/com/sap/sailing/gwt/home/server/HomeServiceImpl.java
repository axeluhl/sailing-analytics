package com.sap.sailing.gwt.home.server;

import com.sap.sailing.gwt.home.client.HomeService;
import com.sap.sailing.gwt.ui.server.ProxiedRemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
public class HomeServiceImpl extends ProxiedRemoteServiceServlet implements HomeService {
    private static final long serialVersionUID = 3947782997746039939L;

    public HomeServiceImpl() {
    }
}
