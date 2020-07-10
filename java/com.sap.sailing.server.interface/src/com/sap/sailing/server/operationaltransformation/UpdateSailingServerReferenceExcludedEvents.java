package com.sap.sailing.server.operationaltransformation;

import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

/**
 * Updates the {@link RemoteSailingServerReference} instance.
 * 
 * @author Dmitry Bilyk
 *
 */
public class UpdateSailingServerReferenceExcludedEvents
        extends AbstractRacingEventServiceOperation<RemoteSailingServerReference> {
    private static final long serialVersionUID = -8043707897960323597L;
    private final String serverName;
    private final List<UUID> excludedEventIds;

    public UpdateSailingServerReferenceExcludedEvents(String serverName, List<UUID> eventIdsToExclude) {
        super();
        this.serverName = serverName;
        this.excludedEventIds = eventIdsToExclude;
    }

    @Override
    public RemoteSailingServerReference internalApplyTo(RacingEventService toState) throws Exception {
        RemoteSailingServerReference result = toState.updateRemoteSailingServerReferenceExcludedEventIds(serverName, excludedEventIds);
        return result;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return null;
    }
}
