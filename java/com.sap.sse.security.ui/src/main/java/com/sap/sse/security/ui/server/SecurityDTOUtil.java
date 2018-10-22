package com.sap.sse.security.ui.server;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.NamedSecuredObjectDTO;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.SecuredObject;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.UserGroup;

public class SecurityDTOUtil {
    private SecurityDTOUtil() {
    }

    /**
     * Adds {@link AccessControlList access control list} and {@link Ownership ownership} information for the given
     * {@link QualifiedObjectIdentifier qualified object identifier} to the provided {@link NamedSecuredObjectDTO
     * secured object DTO}.
     * 
     * @param dto
     *            the {@link NamedSecuredObjectDTO} to add security information to
     * @param objectId
     *            the {@link QualifiedObjectIdentifier} to get security information for
     */
    public static void addSecurityInformation(SecurityService securityService, final SecuredObject dto,
            final QualifiedObjectIdentifier objectId) {
        final SecurityDTOFactory securityDTOFactory = new SecurityDTOFactory();
        final Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser = new HashMap<>();
        final Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup = new HashMap<>();
        final AccessControlListAnnotation accessControlList = securityService.getAccessControlList(objectId);
        dto.setAccessControlList(securityDTOFactory.createAccessControlListDTO(
                accessControlList == null ? null : accessControlList.getAnnotation(), fromOriginalToStrippedDownUser,
                fromOriginalToStrippedDownUserGroup));
        final OwnershipAnnotation ownership = securityService.getOwnership(objectId);
        dto.setOwnership(securityDTOFactory.createOwnershipDTO(ownership == null ? null : ownership.getAnnotation(),
                fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup));
    }
}
