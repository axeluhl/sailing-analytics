package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkPropertiesBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;

public class MarkPropertiesBasedMarkConfigurationImpl<P> extends MarkConfigurationImpl<P>
        implements MarkPropertiesBasedMarkConfiguration<P> {
    private static final long serialVersionUID = -1371019872646970791L;

    private final MarkProperties markProperties;

    public MarkPropertiesBasedMarkConfigurationImpl(MarkProperties markProperties, MarkTemplate optionalMarkTemplate, P annotation) {
        super(optionalMarkTemplate, annotation);
        this.markProperties = markProperties;
    }

    @Override
    public String getShortName() {
        return markProperties.getShortName();
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
