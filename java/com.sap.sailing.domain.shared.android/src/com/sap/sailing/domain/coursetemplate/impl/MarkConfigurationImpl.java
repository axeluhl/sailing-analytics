package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.Positioning;
import com.sap.sailing.domain.coursetemplate.StorablePositioning;

public abstract class MarkConfigurationImpl implements MarkConfiguration {
    private static final long serialVersionUID = -1130451024516101231L;
    
    private final MarkTemplate optionalMarkTemplate;
    private final boolean storeToInventory;
    private final StorablePositioning optionalPositioning;
    private final Positioning storedPositioning;

    public MarkConfigurationImpl(MarkTemplate optionalMarkTemplate, StorablePositioning optionalPositioning,
            Positioning storedPositioning, boolean storeToInventory) {
        super();
        this.optionalMarkTemplate = optionalMarkTemplate;
        this.optionalPositioning = optionalPositioning;
        this.storedPositioning = storedPositioning;
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
    public StorablePositioning getOptionalPositioning() {
        return optionalPositioning;
    }
    
    @Override
    public Positioning getEffectivePositioning() {
        return optionalPositioning != null ? optionalPositioning : storedPositioning;
    }
}
