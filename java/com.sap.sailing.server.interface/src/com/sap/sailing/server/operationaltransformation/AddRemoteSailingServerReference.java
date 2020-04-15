package com.sap.sailing.server.operationaltransformation;

import java.net.URL;

import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

/**
 * Adds a {@link RemoteSailingServerReference} instance.
 * 
 * @author Frank Mittag (c5163874)
 *
 */
public class AddRemoteSailingServerReference extends AbstractRacingEventServiceOperation<RemoteSailingServerReference> {
	private static final long serialVersionUID = -226327171479379243L;
	private final String serverName; 
	private final URL serverUrl;
	
    public AddRemoteSailingServerReference(String serverName, URL serverUrl) {
        super();
        this.serverName = serverName;
        this.serverUrl = serverUrl;
    }

    @Override
    public RemoteSailingServerReference internalApplyTo(RacingEventService toState) throws Exception {
        RemoteSailingServerReference result = toState.addRemoteSailingServerReference(serverName, serverUrl);
        return result;
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
