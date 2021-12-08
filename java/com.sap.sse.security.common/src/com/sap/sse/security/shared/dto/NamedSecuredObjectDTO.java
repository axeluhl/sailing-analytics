package com.sap.sse.security.shared.dto;

import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;

/**
 * {@link NamedDTO} extension which also implements {@link SecuredObject} interface.
 */
public abstract class NamedSecuredObjectDTO extends NamedDTO implements SecuredDTO {

    private static final long serialVersionUID = 2642220699434177353L;

    private SecurityInformationDTO securityInformation;

    @Deprecated
    protected NamedSecuredObjectDTO() {} // for GWT RPC serialization only

    protected NamedSecuredObjectDTO(String name) {
        super(name);
        securityInformation = new SecurityInformationDTO();
    }
    
    protected NamedSecuredObjectDTO(String name, SecurityInformationDTO securityInformation) {
        this(name);
        this.securityInformation = securityInformation;
    }

    protected SecurityInformationDTO getSecurityInformation() {
        return securityInformation;
    }
    
    @Override
    public final AccessControlListDTO getAccessControlList() {
        return securityInformation.getAccessControlList();
    }

    @Override
    public final OwnershipDTO getOwnership() {
        return securityInformation.getOwnership();
    }

    @Override
    public final void setAccessControlList(final AccessControlListDTO accessControlList) {
        this.securityInformation.setAccessControlList(accessControlList);
    }

    @Override
    public final void setOwnership(final OwnershipDTO ownership) {
        this.securityInformation.setOwnership(ownership);
    }
    
    public static NamedSecuredObjectDTO create(String name, HasPermissions type, TypeRelativeObjectIdentifier objectId) {
        return new NamedSecuredObjectDTO(name) {
            private static final long serialVersionUID = 7803271077711791212L;

            @Override
            public QualifiedObjectIdentifier getIdentifier() {
                return type.getQualifiedObjectIdentifier(objectId);
            }
        
            @Override
            public HasPermissions getPermissionType() {
                return type;
            }
        };
    }

}
