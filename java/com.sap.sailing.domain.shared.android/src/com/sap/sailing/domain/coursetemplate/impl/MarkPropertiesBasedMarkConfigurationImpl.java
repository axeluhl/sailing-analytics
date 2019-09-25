package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkPropertiesBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.Positioning;

public class MarkPropertiesBasedMarkConfigurationImpl extends MarkConfigurationImpl
        implements MarkPropertiesBasedMarkConfiguration {
    private static final long serialVersionUID = -1371019872646970791L;

    private final MarkProperties markProperties;

    public MarkPropertiesBasedMarkConfigurationImpl(MarkProperties markProperties, MarkTemplate optionalMarkTemplate, Positioning optionalPositioning) {
        super(optionalMarkTemplate, optionalPositioning, /* storeToInventory */ false);
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
    public CommonMarkProperties getEffectiveProperties() {
        return markProperties;
    }
}
