package com.sap.sse.security.shared.dto;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.security.shared.AbstractUserGroupImpl;

public class UserGroupDTO extends AbstractUserGroupImpl<StrippedUserDTO> implements SecuredDTO {
    private static final long serialVersionUID = 1L;
    private OwnershipDTO owner;
    private AccessControlListDTO acl;

    @Deprecated
    // GWT serializer only
    UserGroupDTO() {
        super(null, null, null);
    }

    public UserGroupDTO(Set<StrippedUserDTO> users, UUID id, String name) {
        super(users, id, name);
    }

    public UserGroupDTO(UUID id, String name) {
        super(new HashSet<StrippedUserDTO>(), id, name);
    }

    @Override
    public AccessControlListDTO getAccessControlList() {
        return acl;
    }

    @Override
    public OwnershipDTO getOwnership() {
        return owner;
    }

    @Override
    public void setAccessControlList(AccessControlListDTO acl) {
        this.acl = acl;
    }

    @Override
    public void setOwnership(OwnershipDTO owner) {
        this.owner = owner;
    }

}
