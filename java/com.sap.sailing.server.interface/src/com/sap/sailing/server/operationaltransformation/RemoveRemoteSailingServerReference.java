package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

/**
 * Removes a {@link RemoteSailingServerReference} instance.
 * 
 * @author Frank Mittag (c5163874)
 *
 */
public class RemoveRemoteSailingServerReference extends AbstractRacingEventServiceOperation<Void> {
	private static final long serialVersionUID = -1283971355735610505L;
	private final String serverName; 
	
    public RemoveRemoteSailingServerReference(String serverName) {
        super();
        this.serverName = serverName;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.removeRemoteSailingServerReference(serverName);
        return null;
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
