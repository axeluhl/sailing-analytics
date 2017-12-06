package com.sap.sse.security;

import java.util.Set;
import java.util.UUID;

import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.UserGroup;

/**
 * A special user group that can be used as "owning tenant" for objects in the context of the
 * {@link SecurityService}.
 * 
 * @see SecurityService#createOwnership(String, String, UUID, String)
 * @see SecurityService#getOwnership(String)
 * @see Ownership
 * @author Axel Uhl (d043530)
 *
 */
public class Tenant extends UserGroupImpl {
    private static final long serialVersionUID = -8831840409264252279L;

    public Tenant(UserGroup group) {
        super((UUID) group.getId(), group.getName(), group.getUsernames());
    }
    
    public Tenant(UUID id, String name) {
        super(id, name);
    }
    
    public Tenant(UUID id, String name, Set<String> usernames) {
        super(id, name, usernames);
    }
}