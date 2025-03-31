package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;

public class SetEmptyAccessControlListOperation implements SecurityOperation<Void> {
    private static final long serialVersionUID = 3033676138552414329L;
    protected final QualifiedObjectIdentifier idOfAccessControlledObject;
    protected final String displayName;

    public SetEmptyAccessControlListOperation(QualifiedObjectIdentifier idOfAccessControlledObject, String displayName) {
        this.idOfAccessControlledObject = idOfAccessControlledObject;
        this.displayName = displayName;
    }

    @Override
    public Void internalApplyTo(ReplicableSecurityService toState) throws Exception {
        toState.internalSetEmptyAccessControlList(idOfAccessControlledObject, displayName);
        return null;
    }
}
