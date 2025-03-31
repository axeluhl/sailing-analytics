package com.sap.sailing.shared.server.operations;

import java.util.UUID;

import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.shared.server.impl.ReplicatingSharedSailingData;

public class UpdateMarkPropertiesOperation implements SharedSailingDataOperation<Void> {
    private static final long serialVersionUID = -8800721889706772056L;
    protected final UUID idOfNewMarkProperties;
    protected final CommonMarkProperties properties;
    protected final Iterable<String> tags;

    public UpdateMarkPropertiesOperation(UUID idOfNewMarkProperties, CommonMarkProperties properties,
            Iterable<String> tags) {
        this.idOfNewMarkProperties = idOfNewMarkProperties;
        this.properties = properties;
        this.tags = tags;
    }

    @Override
    public Void internalApplyTo(ReplicatingSharedSailingData toState) throws Exception {
        toState.internalUpdateMarkProperties(idOfNewMarkProperties, properties, tags);
        return null;
    }
}
