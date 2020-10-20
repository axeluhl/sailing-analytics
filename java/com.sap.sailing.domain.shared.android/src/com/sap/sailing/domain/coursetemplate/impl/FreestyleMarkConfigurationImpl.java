package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.CommonMarkPropertiesWithTags;
import com.sap.sailing.domain.coursetemplate.FreestyleMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkConfigurationVisitor;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;

public class FreestyleMarkConfigurationImpl<P> extends MarkConfigurationImpl<P> implements FreestyleMarkConfiguration<P> {
    private static final long serialVersionUID = 8167307943855869321L;

    private final MarkProperties optionalMarkProperties;
    private final CommonMarkPropertiesWithTags freestyleProperties;

    public FreestyleMarkConfigurationImpl(MarkTemplate optionalMarkTemplate, MarkProperties optionalMarkProperties,
            CommonMarkPropertiesWithTags freestyleProperties, P annotation) {
        super(optionalMarkTemplate, annotation);
        this.optionalMarkProperties = optionalMarkProperties;
        this.freestyleProperties = freestyleProperties;
    }

    @Override
    public <T> T accept(MarkConfigurationVisitor<T, P> visitor) {
        return visitor.visit(this);
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
    public CommonMarkPropertiesWithTags getFreestyleProperties() {
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
