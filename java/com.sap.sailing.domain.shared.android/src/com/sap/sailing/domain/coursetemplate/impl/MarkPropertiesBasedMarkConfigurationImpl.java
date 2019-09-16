package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.coursetemplate.CommonMarkPropertiesWithOptionalPositioning;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkPropertiesBasedMarkConfiguration;

public class MarkPropertiesBasedMarkConfigurationImpl extends MarkConfigurationImpl
        implements MarkPropertiesBasedMarkConfiguration {
    private static final long serialVersionUID = -1371019872646970791L;

    private final MarkProperties markProperties;

    public MarkPropertiesBasedMarkConfigurationImpl(MarkProperties markProperties) {
        super(/* optionalMarkTemplate */ null, /* storeToInventory */ false);
        this.markProperties = markProperties;
    }

    @Override
    public String getName() {
        return markProperties.getName();
    }

    @Override
    public MarkProperties getMarkProperties() {
        return markProperties;
    }

    @Override
    public CommonMarkPropertiesWithOptionalPositioning getEffectiveProperties() {
        return markProperties;
    }
}
