package com.sap.sailing.shared.server.operations;

import java.util.UUID;

import com.sap.sailing.shared.server.impl.ReplicatingSharedSailingData;

public class CreateMarkRoleOperation implements SharedSailingDataOperation<Void> {
    private static final long serialVersionUID = 137359093505493291L;
    protected final UUID idOfNewMarkRole;
    protected final String name;
    protected final String shortName;

    public CreateMarkRoleOperation(UUID idOfNewMarkRole, String name, String shortName) {
        this.idOfNewMarkRole = idOfNewMarkRole;
        this.name = name;
        this.shortName = shortName;
    }

    @Override
    public Void internalApplyTo(ReplicatingSharedSailingData toState) throws Exception {
        toState.internalCreateMarkRole(idOfNewMarkRole, name, shortName);
        return null;
    }
}
