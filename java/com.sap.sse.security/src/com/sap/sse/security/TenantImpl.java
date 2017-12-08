package com.sap.sse.security;

import java.util.Set;
import java.util.UUID;

import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.Tenant;
import com.sap.sse.security.shared.UserGroup;

/**
 * A special user group that can be used as "owning tenant" for objects in the context of the
 * {@link SecurityService}.
 * 
 * @see SecurityService#createOwnership(String, UserImpl, Tenant, String)
 * @see SecurityService#getOwnership(String)
 * @see Ownership
 * @author Axel Uhl (d043530)
 *
 */
public class TenantImpl extends UserGroupImpl implements Tenant {
    private static final long serialVersionUID = -8831840409264252279L;

    public TenantImpl(UserGroup group) {
        super(group.getId(), group.getName(), group.getUsers());
    }
    
    public TenantImpl(UUID id, String name) {
        super(id, name);
    }
    
    public TenantImpl(UUID id, String name, Set<SecurityUser> users) {
        super(id, name, users);
    }

    @Override
    public String toString() {
        return "TenantImpl [getId()=" + getId() + ", getName()=" + getName() + ", getUsers()=" + getUsers() + "]";
    }
}