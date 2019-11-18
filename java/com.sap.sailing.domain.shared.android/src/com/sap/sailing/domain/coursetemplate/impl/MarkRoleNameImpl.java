package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.coursetemplate.MarkRoleName;
import com.sap.sse.common.impl.NamedImpl;

public class MarkRoleNameImpl extends NamedImpl implements MarkRoleName {

    private static final long serialVersionUID = 3661478108460413196L;

    public MarkRoleNameImpl(String name) {
        super(name);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
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
        NamedImpl other = (NamedImpl) obj;
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName()))
            return false;
        return true;
    }
}
