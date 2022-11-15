package com.sap.sailing.shared.server.operations;

import java.util.UUID;

import com.sap.sailing.domain.coursetemplate.Positioning;
import com.sap.sailing.shared.server.impl.ReplicatingSharedSailingData;

public class SetPositioningInformationForMarkPropertiesOperation implements SharedSailingDataOperation<Void> {
    private static final long serialVersionUID = -1639367314017760853L;
    protected final UUID markPropertiesUUID;
    protected final Positioning positioningInformation;

    public SetPositioningInformationForMarkPropertiesOperation(UUID markPropertiesUUID,
            Positioning positioningInformation) {
        this.markPropertiesUUID = markPropertiesUUID;
        this.positioningInformation = positioningInformation;
    }

    @Override
    public Void internalApplyTo(ReplicatingSharedSailingData toState) throws Exception {
        toState.internalSetPositioningInformationForMarkProperties(markPropertiesUUID, positioningInformation);
        return null;
    }
}
