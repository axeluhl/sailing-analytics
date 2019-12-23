package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;

public abstract class MarkConfigurationImpl<P> implements MarkConfiguration<P> {
    private static final long serialVersionUID = -1130451024516101231L;
    private final MarkTemplate optionalMarkTemplate;
    private final P annotationInfo;

    public MarkConfigurationImpl(MarkTemplate optionalMarkTemplate, P annotationInfo) {
        super();
        this.optionalMarkTemplate = optionalMarkTemplate;
        this.annotationInfo = annotationInfo;
    }

    @Override
    public MarkTemplate getOptionalMarkTemplate() {
        return optionalMarkTemplate;
    }

    @Override
    public P getAnnotationInfo() {
        return annotationInfo;
    }
}
