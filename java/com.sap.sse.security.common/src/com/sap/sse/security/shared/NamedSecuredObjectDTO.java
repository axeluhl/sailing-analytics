package com.sap.sse.security.shared;

/**
 * {@link NamedDTO} extension which also implements {@link SecuredObject} interface.
 */
public class NamedSecuredObjectDTO extends NamedDTO implements SecuredObject {

    private static final long serialVersionUID = 2642220699434177353L;

    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();

    protected NamedSecuredObjectDTO() {
    }

    protected NamedSecuredObjectDTO(String name) {
        super(name);
    }

    @Override
    public final AccessControlList getAccessControlList() {
        return securityInformation.getAccessControlList();
    }

    @Override
    public final Ownership getOwnership() {
        return securityInformation.getOwnership();
    }

    @Override
    public final void setAccessControlList(final AccessControlList accessControlList) {
        this.securityInformation.setAccessControlList(accessControlList);
    }

    @Override
    public final void setOwnership(final Ownership ownership) {
        this.securityInformation.setOwnership(ownership);
    }
}
