package com.sap.sse.security.shared.dto;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.shared.AbstractUserGroupImpl;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

public class UserGroupDTO extends AbstractUserGroupImpl<StrippedUserDTO, StrippedRoleDefinitionDTO> implements SecuredDTO {

    private static final long serialVersionUID = -2217611212963521760L;

    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();

    @Deprecated
    // GWT serializer only
    UserGroupDTO() {
        super(null, null, null, null);
    }

    public UserGroupDTO(UUID id, String name, Set<StrippedUserDTO> users,
            Map<StrippedRoleDefinitionDTO, Boolean> roleDefinitionMap) {
        super(id, name, users, roleDefinitionMap);
    }

    @Override
    public AccessControlListDTO getAccessControlList() {
        return securityInformation.getAccessControlList();
    }

    @Override
    public OwnershipDTO getOwnership() {
        return securityInformation.getOwnership();
    }

    @Override
    public void setAccessControlList(AccessControlListDTO accessControlList) {
        this.securityInformation.setAccessControlList(accessControlList);
    }

    @Override
    public void setOwnership(OwnershipDTO ownership) {
        this.securityInformation.setOwnership(ownership);
    }

    @Override
    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return new TypeRelativeObjectIdentifier(getId().toString());
    }

    @Override
    public HasPermissions getType() {
        return SecuredSecurityTypes.USER_GROUP;
    }

    public Iterable<Pair<StrippedRoleDefinitionDTO, Boolean>> getRoleDefinitions() {
        final Set<Pair<StrippedRoleDefinitionDTO, Boolean>> result = new HashSet<>();
        for (Entry<StrippedRoleDefinitionDTO, Boolean> entry : roleDefinitionMap.entrySet()) {
            result.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        return result;
    }

}
