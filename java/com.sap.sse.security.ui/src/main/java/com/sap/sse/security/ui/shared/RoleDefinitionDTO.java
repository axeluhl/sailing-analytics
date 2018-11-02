package com.sap.sse.security.ui.shared;

import java.util.UUID;

import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.RoleDefinitionImpl;
import com.sap.sse.security.shared.SecuredObject;

public class RoleDefinitionDTO extends RoleDefinitionImpl implements SecuredObject {
    
    private static final long serialVersionUID = -2580755958300866126L;
    
    private AccessControlList accessControlList;
    private Ownership ownership;

    @Deprecated
    RoleDefinitionDTO() {} // for GWT serialization only
    
    public RoleDefinitionDTO(final UUID id, final String name) {
        super(id, name);
    }

    @Override
    public final AccessControlList getAccessControlList() {
        return accessControlList;
    }

    @Override
    public final Ownership getOwnership() {
        return ownership;
    }

    @Override
    public final void setAccessControlList(final AccessControlList accessControlList) {
        this.accessControlList = accessControlList;
    }

    @Override
    public final void setOwnership(final Ownership ownership) {
        this.ownership = ownership;
    }

}
