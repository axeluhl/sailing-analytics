package com.sap.sse.security.operations;

import java.util.UUID;

import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.impl.Ownership;

public class SetOwnershipOperation implements SecurityOperation<Ownership> {
    private static final long serialVersionUID = -7863993752357981895L;
    protected final QualifiedObjectIdentifier idOfOwnedObject;
    protected final String owningUsername;
    protected final UUID tenantOwnerId;
    protected final String displayNameOfOwnedObject;

    public SetOwnershipOperation(QualifiedObjectIdentifier idOfOwnedObject, String owningUsername, UUID tenantOwnerId,
            String displayNameOfOwnedObject) {
        this.idOfOwnedObject = idOfOwnedObject;
        this.owningUsername = owningUsername;
        this.tenantOwnerId = tenantOwnerId;
        this.displayNameOfOwnedObject = displayNameOfOwnedObject;
    }

    @Override
    public Ownership internalApplyTo(ReplicableSecurityService toState) throws Exception {
        return toState.internalSetOwnership(idOfOwnedObject, owningUsername, tenantOwnerId, displayNameOfOwnedObject);
    }
}
