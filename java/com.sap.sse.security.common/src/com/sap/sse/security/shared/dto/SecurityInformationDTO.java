package com.sap.sse.security.shared.dto;

import java.io.Serializable;

import com.sap.sse.security.shared.impl.AccessControlList;
import com.sap.sse.security.shared.impl.Ownership;

/**
 * Data transfer object wrapping security information such as {@link AccessControlList access control list} and
 * {@link Ownership ownership} of and {@link SecuredObject secured object}.
 */
public class SecurityInformationDTO implements Serializable {

    private static final long serialVersionUID = -292250850983164293L;

    private AccessControlListDTO accessControlList;
    private OwnershipDTO ownership;
    
    public final AccessControlListDTO getAccessControlList() {
        return accessControlList;
    }

    public final OwnershipDTO getOwnership() {
        return ownership;
    }

    public final void setAccessControlList(final AccessControlListDTO accessControlList) {
        this.accessControlList = accessControlList;
    }

    public final void setOwnership(final OwnershipDTO ownership) {
        this.ownership = ownership;
    }
}
