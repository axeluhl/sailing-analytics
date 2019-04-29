package com.sap.sse.security.shared;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.sap.sse.common.NamedWithID;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.UserGroupImpl;

/**
 * A group of users; equality and hash code are based solely on the {@link #getId() ID}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class SecurityUserGroupImpl<RD extends RoleDefinition> implements NamedWithID, WithQualifiedObjectIdentifier, SecurityUserGroup<RD> {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;
    protected Map<RD, Boolean> roleDefinitionMap;

    public SecurityUserGroupImpl(UUID id, String name, Map<RD, Boolean> roleDefinitionMap) {
        super();
        this.id = id;
        this.name = name;
        this.roleDefinitionMap = roleDefinitionMap;
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
    public Map<RD, Boolean> getRoleDefinitionMap() {
        return Collections.unmodifiableMap(roleDefinitionMap);
    }

    @Override
    public HasPermissions getType() {
        return SecuredSecurityTypes.USER_GROUP;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }


    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return UserGroupImpl.getTypeRelativeObjectIdentifier(getId());
    }

    @Override
    public String toString() {
        return "SecurityUserGroupImpl [id=" + id + ", name=" + name + ", roleDefinitionMap=" + roleDefinitionMap + "]";
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
        SecurityUserGroupImpl<?> other = (SecurityUserGroupImpl<?>) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
