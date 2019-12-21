package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.RegattaMarkConfiguration;

public class RegattaMarkConfigurationImpl<P> extends MarkConfigurationImpl<P> implements RegattaMarkConfiguration<P> {
    private static final long serialVersionUID = 329929810377510675L;

    private final Mark mark;
    private final MarkProperties optionalMarkProperties;

    public RegattaMarkConfigurationImpl(Mark mark, P additionalInfo, MarkTemplate optionalMarkTemplate,
            MarkProperties optionalMarkProperties) {
        super(optionalMarkTemplate, additionalInfo);
        this.mark = mark;
        this.optionalMarkProperties = optionalMarkProperties;
    }

    @Override
    public String getShortName() {
        return mark.getShortName();
    }

    @Override
    public String getName() {
        return mark.getName();
    }

    @Override
    public Mark getMark() {
        return mark;
    }

    @Override
    public CommonMarkProperties getEffectiveProperties() {
        return mark;
    }
    
    @Override
    public MarkProperties getOptionalMarkProperties() {
        return optionalMarkProperties;
    }
}
