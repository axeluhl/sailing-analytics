package com.sap.sailing.shared.server.operations;

import java.util.UUID;

import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.shared.server.impl.ReplicatingSharedSailingData;

public class CreateMarkTemplateOperation implements SharedSailingDataOperation<Void> {
    private static final long serialVersionUID = 346198713268051379L;
    protected final UUID idOfNewMarkTemplate;
    protected final CommonMarkProperties properties;

    public CreateMarkTemplateOperation(UUID idOfNewMarkTemplate, CommonMarkProperties properties) {
        this.idOfNewMarkTemplate = idOfNewMarkTemplate;
        this.properties = properties;
    }

    @Override
    public Void internalApplyTo(ReplicatingSharedSailingData toState) throws Exception {
        toState.internalCreateMarkTemplate(idOfNewMarkTemplate, properties);
        return null;
    }
}
