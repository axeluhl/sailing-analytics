package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;

public abstract class MarkConfigurationImpl<MarkConfigurationT extends MarkConfiguration<MarkConfigurationT>>
implements MarkConfiguration<MarkConfigurationT> {
    private static final long serialVersionUID = -1130451024516101231L;
    private final MarkTemplate optionalMarkTemplate;

    public MarkConfigurationImpl(MarkTemplate optionalMarkTemplate) {
        super();
        this.optionalMarkTemplate = optionalMarkTemplate;
    }

    @Override
    public MarkTemplate getOptionalMarkTemplate() {
        return optionalMarkTemplate;
    }
}
