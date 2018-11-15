package com.sap.sse.security.shared;

import java.io.Serializable;

import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.SecuredObject;

/**
 * Data transfer object wrapping security information such as {@link AccessControlList access control list} and
 * {@link Ownership ownership} of and {@link SecuredObject secured object}.
 */
public class SecurityInformationDTO implements SecuredObject, Serializable {

    private static final long serialVersionUID = -292250850983164293L;

    private AccessControlList accessControlList;
    private Ownership ownership;

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
