package com.sap.sse.security.shared.dto;

import com.sap.sse.security.shared.impl.AbstractUserReference;

public class StrippedUserDTO extends AbstractUserReference {
    private static final long serialVersionUID = 1L;
    
    
    @Deprecated
    private StrippedUserDTO() {
        super(null);
    }

    public StrippedUserDTO(String name) {
        super(name);
    }
}
