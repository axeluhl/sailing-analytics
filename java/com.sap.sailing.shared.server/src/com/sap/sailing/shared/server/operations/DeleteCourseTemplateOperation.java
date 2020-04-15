package com.sap.sailing.shared.server.operations;

import java.util.UUID;

import com.sap.sailing.shared.server.impl.ReplicatingSharedSailingData;

public class DeleteCourseTemplateOperation implements SharedSailingDataOperation<Void> {
    private static final long serialVersionUID = 7464254331725117001L;
    protected final UUID courseTemplateUUID;

    public DeleteCourseTemplateOperation(UUID courseTemplateUUID) {
        this.courseTemplateUUID = courseTemplateUUID;
    }

    @Override
    public Void internalApplyTo(ReplicatingSharedSailingData toState) throws Exception {
        toState.internalDeleteCourseTemplate(courseTemplateUUID);
        return null;
    }
}
