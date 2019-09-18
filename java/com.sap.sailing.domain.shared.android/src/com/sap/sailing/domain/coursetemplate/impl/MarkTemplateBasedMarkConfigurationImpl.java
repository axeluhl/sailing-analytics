package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.MarkTemplateBasedMarkConfiguration;

public class MarkTemplateBasedMarkConfigurationImpl extends MarkConfigurationImpl
        implements MarkTemplateBasedMarkConfiguration {
    private static final long serialVersionUID = -6683111363486434715L;

    public MarkTemplateBasedMarkConfigurationImpl(MarkTemplate markTemplate) {
        super(markTemplate, /* optionalPositioning */ null, /* storeToInventory */ false);
    }

    @Override
    public CommonMarkProperties getEffectiveProperties() {
        return getOptionalMarkTemplate();
    }

    @Override
    public String getName() {
        return getOptionalMarkTemplate().getName();
    }

}
