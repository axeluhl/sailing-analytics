package com.sap.sailing.gwt.ui.server;

import java.util.Collections;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;

/**
 * The server side implementation of the RPC service.
 */
public class SailingServiceImpl extends RemoteServiceServlet implements SailingService {
    private static final long serialVersionUID = 9031688830194537489L;

    public EventDAO[] listEvents() throws IllegalArgumentException {
        List<RegattaDAO> regattas = Collections.emptyList();
        return new EventDAO[] { new EventDAO("Kiel Week", /* regattas */ regattas), new EventDAO("STG Training", /* regattas */ regattas) };
    }
}
