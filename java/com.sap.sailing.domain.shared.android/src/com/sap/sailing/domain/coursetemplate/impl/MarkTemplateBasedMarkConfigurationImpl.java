package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkPropertiesBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.MarkTemplateBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.StorablePositioning;

public class MarkTemplateBasedMarkConfigurationImpl extends MarkConfigurationImpl
        implements MarkTemplateBasedMarkConfiguration {
    private static final long serialVersionUID = -6683111363486434715L;

    public MarkTemplateBasedMarkConfigurationImpl(MarkTemplate markTemplate, StorablePositioning optionalPositioning,
            boolean storeToInventory) {
        super(markTemplate, optionalPositioning, /* storedPositioning */ null, storeToInventory);
    }

    @Override
    public CommonMarkProperties getEffectiveProperties() {
        return getOptionalMarkTemplate();
    }

    @Override
    public String getName() {
        return getOptionalMarkTemplate().getName();
    }

    @Override
    public String getShortName() {
        return getOptionalMarkTemplate().getShortName();
    }

    /**
     * When configuring a mark from a {@link MarkTemplate} using an instance of this type, no
     * {@link MarkProperties} can be specified, and therefore this method always returns {@code null}.
     * To provide {@link MarkProperties} that configure a mark and connect it with other properties
     * that are based on a {@link MarkTemplate}, use {@link MarkPropertiesBasedMarkConfiguration}
     * with a non-{@code null} {@link MarkPropertiesBasedMarkConfiguration#getOptionalMarkTemplate()}
     * return value.
     */
    @Override
    public MarkProperties getOptionalMarkProperties() {
        return null;
    }
}
