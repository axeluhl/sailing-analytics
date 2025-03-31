package com.sap.sailing.domain.coursetemplate.impl;

import java.util.Arrays;

import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.domain.coursetemplate.MarkRolePair;
import com.sap.sse.common.impl.NamedImpl;

public class MarkRolePairImpl extends NamedImpl implements MarkRolePair {
    private static final long serialVersionUID = -4966456947099578789L;
    private final MarkRole left;
    private final MarkRole right;
    private final String shortName;

    public MarkRolePairImpl(String name, String shortName, MarkRole left, MarkRole right) {
        super(name);
        this.shortName = shortName;
        this.left = left;
        this.right = right;
    }

    @Override
    public Iterable<MarkRole> getMarkRoles() {
        return Arrays.asList(getLeft(), getRight());
    }

    @Override
    public MarkRole getLeft() {
        return left;
    }

    @Override
    public MarkRole getRight() {
        return right;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getShortName() == null) ? 0 : getShortName().hashCode());
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
        MarkRolePairImpl other = (MarkRolePairImpl) obj;
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName()))
            return false;
        if (getShortName() == null) {
            if (other.getShortName() != null)
                return false;
        } else if (!getShortName().equals(other.getShortName()))
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
