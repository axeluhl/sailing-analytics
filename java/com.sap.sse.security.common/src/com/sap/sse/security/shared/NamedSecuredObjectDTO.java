package com.sap.sse.security.shared;

/**
 * {@link NamedDTO} extension which also implements {@link SecuredObject} interface.
 */
public class NamedSecuredObjectDTO extends NamedDTO implements SecuredObject {

    private static final long serialVersionUID = 4448408121977185408L;

    private AccessControlList accessControlList;
    private Ownership ownership;

    protected NamedSecuredObjectDTO() {
    }

    protected NamedSecuredObjectDTO(String name) {
        super(name);
    }

    @Override
    public final AccessControlList getAccessControlList() {
        return accessControlList;
    }

    @Override
    public final Ownership getOwnership() {
        return ownership;
    }

    public final void setAccessControlList(final AccessControlList accessControlList) {
        this.accessControlList = accessControlList;
    }

    public final void setOwnership(final Ownership ownership) {
        this.ownership = ownership;
    }

}
