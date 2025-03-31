package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.coursetemplate.MarkConfigurationRequestAnnotation;
import com.sap.sailing.domain.coursetemplate.Positioning;

public class MarkConfigurationRequestAnnotationImpl implements MarkConfigurationRequestAnnotation {
    private final boolean storeToInventory;
    private final Positioning optionalPositioning;
    private final MarkRoleCreationRequest optionalMarkRoleCreationRequest;
    
    public static class MarkRoleCreationRequestImpl implements MarkRoleCreationRequest {
        private final String markRoleName;
        private final String markRoleShortName;
        public MarkRoleCreationRequestImpl(String markRoleName, String markRoleShortName) {
            super();
            this.markRoleName = markRoleName;
            this.markRoleShortName = markRoleShortName;
        }
        @Override
        public String getMarkRoleName() {
            return markRoleName;
        }
        @Override
        public String getMarkRoleShortName() {
            return markRoleShortName;
        }
    }
    
    public MarkConfigurationRequestAnnotationImpl(boolean storeToInventory, Positioning optionalPositioning, MarkRoleCreationRequest optionalMarkRoleCreationRequest) {
        super();
        this.storeToInventory = storeToInventory;
        this.optionalPositioning = optionalPositioning;
        this.optionalMarkRoleCreationRequest = optionalMarkRoleCreationRequest;
    }

    @Override
    public boolean isStoreToInventory() {
        return storeToInventory;
    }

    @Override
    public Positioning getOptionalPositioning() {
        return optionalPositioning;
    }

    @Override
    public MarkRoleCreationRequest getOptionalMarkRoleCreationRequest() {
        return optionalMarkRoleCreationRequest;
    }
}
