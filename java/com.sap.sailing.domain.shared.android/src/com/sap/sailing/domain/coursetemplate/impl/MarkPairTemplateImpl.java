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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((left == null) ? 0 : left.hashCode());
        result = prime * result + ((right == null) ? 0 : right.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MarkPairTemplateImpl other = (MarkPairTemplateImpl) obj;
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName()))
            return false;
        if (left == null) {
            if (other.left != null)
                return false;
        } else if (!left.equals(other.left))
            return false;
        if (right == null) {
            if (other.right != null)
                return false;
        } else if (!right.equals(other.right))
            return false;
        return true;
    }

}
