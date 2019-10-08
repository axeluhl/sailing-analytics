package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.Positioning;
import com.sap.sailing.domain.coursetemplate.RegattaMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.StorablePositioning;

public class RegattaMarkConfigurationImpl extends MarkConfigurationImpl implements RegattaMarkConfiguration {
    private static final long serialVersionUID = 329929810377510675L;

    private final Mark mark;
    private final MarkProperties optionalMarkProperties;

    public RegattaMarkConfigurationImpl(Mark mark, StorablePositioning optionalPositioning,
            Positioning storedPositioning, MarkTemplate optionalMarkTemplate, MarkProperties optionalMarkProperties,
            boolean storeToInventory) {
        super(optionalMarkTemplate, optionalPositioning, storedPositioning, storeToInventory);
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
