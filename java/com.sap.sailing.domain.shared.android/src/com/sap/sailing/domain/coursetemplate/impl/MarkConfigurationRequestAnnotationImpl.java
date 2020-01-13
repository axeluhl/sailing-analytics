package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.coursetemplate.MarkConfigurationRequestAnnotation;
import com.sap.sailing.domain.coursetemplate.Positioning;

public class MarkConfigurationRequestAnnotationImpl implements MarkConfigurationRequestAnnotation {
    private final boolean storeToInventory;
    private final Positioning optionalPositioning;
    
    public MarkConfigurationRequestAnnotationImpl(boolean storeToInventory, Positioning optionalPositioning) {
        super();
        this.storeToInventory = storeToInventory;
        this.optionalPositioning = optionalPositioning;
    }

    @Override
    public boolean isStoreToInventory() {
        return storeToInventory;
    }

    @Override
    public Positioning getOptionalPositioning() {
        return optionalPositioning;
    }
}
