package com.sap.sailing.domain.coursetemplate.impl;

import java.util.Arrays;

import com.sap.sailing.domain.coursetemplate.MarkPairTemplate;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sse.common.impl.NamedImpl;

public class MarkPairTemplateImpl extends NamedImpl implements MarkPairTemplate {
    private static final long serialVersionUID = -4966456947099578789L;
    private final MarkTemplate left;
    private final MarkTemplate right;

    public MarkPairTemplateImpl(String name, MarkTemplate left, MarkTemplate right) {
        super(name);
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
