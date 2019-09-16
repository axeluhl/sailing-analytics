package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.Positioning;

public abstract class MarkConfigurationImpl implements MarkConfiguration {
    private static final long serialVersionUID = -1130451024516101231L;
    
    private final MarkTemplate optionalMarkTemplate;
    private final boolean storeToInventory;
    private final Positioning optionalPositioning;

    public MarkConfigurationImpl(MarkTemplate optionalMarkTemplate, Positioning optionalPositioning, boolean storeToInventory) {
        super();
        this.optionalMarkTemplate = optionalMarkTemplate;
        this.optionalPositioning = optionalPositioning;
        this.storeToInventory = storeToInventory;
    }

    @Override
    public MarkTemplate getOptionalMarkTemplate() {
        return optionalMarkTemplate;
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
