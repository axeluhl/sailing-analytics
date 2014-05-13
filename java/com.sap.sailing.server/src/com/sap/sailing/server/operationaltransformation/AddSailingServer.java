package com.sap.sailing.server.operationaltransformation;

import java.net.URL;

import com.sap.sailing.domain.base.SailingServer;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

/**
 * Adds a {@link SailingServer} instance.
 * 
 * @author Frank Mittag (c5163874)
 *
 */
public class AddSailingServer extends AbstractRacingEventServiceOperation<SailingServer> {
	private static final long serialVersionUID = -226327171479379243L;
	private final String serverName; 
	private final URL serverUrl;
	
    public AddSailingServer(String serverName, URL serverUrl) {
        super();
        this.serverName = serverName;
        this.serverUrl = serverUrl;
    }

    @Override
    public SailingServer internalApplyTo(RacingEventService toState) throws Exception {
        return toState.addSailingServer(serverName, serverUrl);
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(
            RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(
            RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

}
