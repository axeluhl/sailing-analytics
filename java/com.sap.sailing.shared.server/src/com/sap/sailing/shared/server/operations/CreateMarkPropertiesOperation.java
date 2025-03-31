package com.sap.sailing.shared.server.operations;

import java.util.UUID;

import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.shared.server.impl.ReplicatingSharedSailingData;

public class CreateMarkPropertiesOperation implements SharedSailingDataOperation<Void> {
    private static final long serialVersionUID = -1378869814815439515L;
    protected final UUID idOfNewMarkProperties;
    protected final CommonMarkProperties properties;
    protected final Iterable<String> tags;

    public CreateMarkPropertiesOperation(UUID idOfNewMarkProperties, CommonMarkProperties properties,
            Iterable<String> tags) {
        this.idOfNewMarkProperties = idOfNewMarkProperties;
        this.properties = properties;
        this.tags = tags;
    }

    @Override
    public Void internalApplyTo(ReplicatingSharedSailingData toState) throws Exception {
        toState.internalCreateMarkProperties(idOfNewMarkProperties, properties, tags);
        return null;
    }
}
