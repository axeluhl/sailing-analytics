package com.sap.sse.security.shared;

import java.util.UUID;

import com.sap.sse.common.NamedWithID;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

/**
 * A group of users; equality and hash code are based solely on the {@link #getId() ID}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class SecurityUserGroupImpl implements NamedWithID, WithQualifiedObjectIdentifier, SecurityUserGroup {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;

    public SecurityUserGroupImpl(UUID id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    /* (non-Javadoc)
     * @see com.sap.sse.security.shared.SecurityUserGroup#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see com.sap.sse.security.shared.SecurityUserGroup#getId()
     */
    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public HasPermissions getType() {
        return SecuredSecurityTypes.USER_GROUP;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(id.toString());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        SecurityUserGroupImpl other = (SecurityUserGroupImpl) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
