package com.sap.sse.security.shared.dto;

import java.util.UUID;

import com.sap.sse.security.shared.SecurityUserGroupImpl;

public class StrippedUserGroupDTO extends SecurityUserGroupImpl {
    private static final long serialVersionUID = 1L;

    @Deprecated
    // GWT serializer only
    StrippedUserGroupDTO() {
        super(null, null);
    }

    public StrippedUserGroupDTO(UUID id, String name) {
        super(id, name);
    }

}
