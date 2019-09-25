package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.FreestyleMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.Positioning;

public class FreestyleMarkConfigurationImpl extends MarkConfigurationImpl implements FreestyleMarkConfiguration {
    private static final long serialVersionUID = 8167307943855869321L;

    private final MarkProperties optionalMarkProperties;
    private final CommonMarkProperties freestyleProperties;

    public FreestyleMarkConfigurationImpl(MarkTemplate optionalMarkTemplate, MarkProperties optionalMarkProperties,
            CommonMarkProperties freestyleProperties, Positioning optionalPositioning) {
        super(optionalMarkTemplate, optionalPositioning, /* storeToInventory */ false);
        this.optionalMarkProperties = optionalMarkProperties;
        this.freestyleProperties = freestyleProperties;
    }

    @Override
    public String getShortName() {
        return freestyleProperties.getShortName();
    }

    @Override
    public String getName() {
        return freestyleProperties.getName();
    }

    @Override
    public CommonMarkProperties getFreestyleProperties() {
        return freestyleProperties;
    }

    @Override
    public CommonMarkProperties getEffectiveProperties() {
        return freestyleProperties;
    }

    @Override
    public MarkProperties getOptionalMarkProperties() {
        return optionalMarkProperties;
    }
}
