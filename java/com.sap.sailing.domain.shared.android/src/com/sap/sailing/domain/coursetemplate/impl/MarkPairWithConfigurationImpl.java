package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkPairWithConfiguration;
import com.sap.sse.common.impl.NamedImpl;

public class MarkPairWithConfigurationImpl<MarkConfigurationT extends MarkConfiguration<MarkConfigurationT>>
        extends NamedImpl implements MarkPairWithConfiguration<MarkConfigurationT> {
    private static final long serialVersionUID = 159552983009183991L;
    
    private final MarkConfigurationT right;
    private final MarkConfigurationT left;

    private final String shortName;

    public MarkPairWithConfigurationImpl(String name, MarkConfigurationT left, MarkConfigurationT right,
            String shortName) {
        super(name);
        this.left = left;
        this.right = right;
        this.shortName = shortName;
    }

    @Override
    public String getShortName() {
        return this.shortName;
    }

    @Override
    public MarkConfigurationT getLeft() {
        return left;
    }

    @Override
    public MarkConfigurationT getRight() {
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
        MarkPairWithConfigurationImpl<?> other = (MarkPairWithConfigurationImpl<?>) obj;
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
