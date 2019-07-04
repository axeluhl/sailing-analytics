package com.sap.sailing.domain.coursetemplate.impl;

import java.util.Arrays;
import java.util.UUID;

import com.sap.sailing.domain.coursetemplate.MarkPairTemplate;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sse.common.impl.NamedWithIDImpl;

public class MarkPairTemplateImpl extends NamedWithIDImpl implements MarkPairTemplate {
    private static final long serialVersionUID = -4966456947099578789L;
    private final MarkTemplate left;
    private final MarkTemplate right;
    
    public MarkPairTemplateImpl(String name, MarkTemplate left, MarkTemplate right) {
        super(name);
        this.left = left;
        this.right = right;
    }

    public MarkPairTemplateImpl(UUID id, String name, MarkTemplate left, MarkTemplate right) {
        super(name, id);
        this.left = left;
        this.right = right;
    }

    @Override
    public Iterable<MarkTemplate> getMarks() {
        return Arrays.asList(getLeft(), getRight());
    }

    @Override
    public MarkTemplate getLeft() {
        return left;
    }

    @Override
    public MarkTemplate getRight() {
        return right;
    }

}
