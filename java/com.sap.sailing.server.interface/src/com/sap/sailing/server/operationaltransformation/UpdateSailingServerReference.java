package com.sap.sailing.server.operationaltransformation;

import java.util.Set;
import java.util.UUID;

import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

/**
 * Updates the {@link RemoteSailingServerReference} instance. {@link boolean} include parameter is used to determine the
 * type of inclusion. If it's set to <code>true</code> then selected events will be loaded, if it's set to
 * <code>false</code> then selected events will be excluded from being loaded.
 * 
 * @author Dmitry Bilyk
 *
 */
public class UpdateSailingServerReference extends AbstractRacingEventServiceOperation<RemoteSailingServerReference> {
    private static final long serialVersionUID = -8043707897960323597L;
    private final String serverName;
    private final boolean include;
    private final Set<UUID> selectedEventIds;

    public UpdateSailingServerReference(String serverName, boolean include, Set<UUID> selectedEventIds) {
        super();
        this.serverName = serverName;
        this.include = include;
        this.selectedEventIds = selectedEventIds;
    }

    @Override
    public RemoteSailingServerReference internalApplyTo(RacingEventService toState) throws Exception {
        RemoteSailingServerReference result = toState.updateRemoteSailingServerReference(serverName, include,
                selectedEventIds);
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
