package com.sap.sse.security.shared;

import java.util.HashSet;
import java.util.UUID;

import com.sap.sse.security.shared.impl.StrippedUserDTO;

public class UserGroupDTO extends AbstractUserGroup<StrippedUserDTO> {
    private static final long serialVersionUID = 1L;

    @Deprecated
    // GWT serializer only
    UserGroupDTO() {
        super(null, null, null);
    }

    public UserGroupDTO(HashSet<StrippedUserDTO> users, UUID id, String name) {
        super(users, id, name);
    }

    public UserGroupDTO(UUID id, String name) {
        super(new HashSet<StrippedUserDTO>(), id, name);
    }

}
