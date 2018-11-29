package com.sap.sse.security.shared.dto;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UserGroupWithSecurityDTO extends StrippedUserGroupDTO implements SecuredDTO {
    private static final long serialVersionUID = 1L;

    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();

    private Set<StrippedUserDTO> users = new HashSet<>();

    @Deprecated
    // GWT serializer only
    UserGroupWithSecurityDTO() {
        super();
    }

    public UserGroupWithSecurityDTO(HashSet<StrippedUserDTO> users, UUID id, String name) {
        super(id, name);
        this.users.addAll(users);
    }

    public UserGroupWithSecurityDTO(UUID id, String name) {
        super(id, name);
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

    public Set<StrippedUserDTO> getUsers() {
        return users;
    }

    public void add(StrippedUserDTO user) {
        users.add(user);
    }

    public void remove(StrippedUserDTO userToRemoveFromTenant) {
        users.remove(userToRemoveFromTenant);
    }
}
