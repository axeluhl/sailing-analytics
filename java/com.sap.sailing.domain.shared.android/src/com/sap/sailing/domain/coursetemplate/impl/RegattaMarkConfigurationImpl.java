package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.coursetemplate.CommonMarkPropertiesWithOptionalPositioning;
import com.sap.sailing.domain.coursetemplate.RegattaMarkConfiguration;

public class RegattaMarkConfigurationImpl extends MarkConfigurationImpl implements RegattaMarkConfiguration {
    private static final long serialVersionUID = 329929810377510675L;

    private final Mark mark;

    public RegattaMarkConfigurationImpl(Mark mark) {
        super(/* optionalMarkTemplate */ null, /* storeToInventory */ false);
        this.mark = mark;
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
    public CommonMarkPropertiesWithOptionalPositioning getEffectiveProperties() {
        // TODO implement
        return null;
    }
}
