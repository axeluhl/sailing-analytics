package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;

public abstract class MarkConfigurationImpl implements MarkConfiguration {
    private static final long serialVersionUID = -1130451024516101231L;
    
    private final MarkTemplate optionalMarkTemplate;
    private final boolean storeToInventory;

    public MarkConfigurationImpl(MarkTemplate optionalMarkTemplate, boolean storeToInventory) {
        super();
        this.optionalMarkTemplate = optionalMarkTemplate;
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

}
