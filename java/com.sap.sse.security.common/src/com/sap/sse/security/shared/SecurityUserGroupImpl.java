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
}
