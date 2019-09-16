package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.coursetemplate.CommonMarkPropertiesWithOptionalPositioning;
import com.sap.sailing.domain.coursetemplate.FreestyleMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;

public class FreestyleMarkConfigurationImpl extends MarkConfigurationImpl implements FreestyleMarkConfiguration {
    private static final long serialVersionUID = 8167307943855869321L;

    private final MarkProperties optionalMarkProperties;
    private final CommonMarkPropertiesWithOptionalPositioning freestyleProperties;

    public FreestyleMarkConfigurationImpl(MarkTemplate optionalMarkTemplate, MarkProperties optionalMarkProperties,
            CommonMarkPropertiesWithOptionalPositioning freestyleProperties) {
        super(optionalMarkTemplate, /* storeToInventory */ false);
        this.optionalMarkProperties = optionalMarkProperties;
        this.freestyleProperties = freestyleProperties;
    }

    @Override
    public String getName() {
        return freestyleProperties.getName();
    }

    @Override
    public CommonMarkPropertiesWithOptionalPositioning getFreestyleProperties() {
        return freestyleProperties;
    }

    @Override
    public CommonMarkPropertiesWithOptionalPositioning getEffectiveProperties() {
        return freestyleProperties;
    }

    @Override
    public MarkProperties getOptionalMarkProperties() {
        return optionalMarkProperties;
    }
}
