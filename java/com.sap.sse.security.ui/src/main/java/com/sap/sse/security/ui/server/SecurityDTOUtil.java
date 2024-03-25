package com.sap.sse.security.ui.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.NamedSecuredObjectDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.StrippedUserDTO;
import com.sap.sse.security.shared.dto.StrippedUserGroupDTO;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.shared.impl.AccessControlList;
import com.sap.sse.security.shared.impl.Ownership;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;
import com.sap.sse.security.shared.impl.UserGroupImpl;

public abstract class SecurityDTOUtil {

    private SecurityDTOUtil() {
    }

    /**
     * Adds {@link AccessControlList access control list} and {@link Ownership ownership} information for the given
     * {@link QualifiedObjectIdentifier qualified object identifier} to the provided {@link NamedSecuredObjectDTO
     * secured object DTO}. Prunes the ACLs to those parts relevant for the currently authenticated user.
     * 
     * @param securityService
     *            the {@link SecurityService} to determine access control list and ownership
     * @param securedObject
     *            the {@link NamedSecuredObjectDTO} to add security information to
     */
    public static void addSecurityInformation(final SecurityService securityService, final SecuredDTO securedObject) {
        addSecurityInformation(securityService, securedObject, /* disablePruningForCurrentUser */ false);
    }
    
    /**
     * Adds {@link AccessControlList access control list} and {@link Ownership ownership} information for the given
     * {@link QualifiedObjectIdentifier qualified object identifier} to the provided {@link NamedSecuredObjectDTO
     * secured object DTO}.
     * 
     * @param securityService
     *            the {@link SecurityService} to determine access control list and ownership
     * @param securedObject
     *            the {@link NamedSecuredObjectDTO} to add security information to
     * @param disablePruningForCurrentUser
     *            if {@code true}, the whole ACL is added. If {@code false}, only that part of the ACL is added that is
     *            relevant for the currently authenticated user. For security checks, only that part is necessary, but
     *            when editing an ACL, all contents are required to be fetched to the UI.
     */
    public static void addSecurityInformation(final SecurityService securityService, final SecuredDTO securedObject, final boolean disablePruningForCurrentUser) {
        addSecurityInformation(new SecurityDTOFactory(), securityService, securedObject, new HashMap<>(),
                new HashMap<>(), disablePruningForCurrentUser);
    }

    /**
     * Adds {@link AccessControlList access control list} and {@link Ownership ownership} information for the given
     * {@link QualifiedObjectIdentifier qualified object identifier} to the provided {@link NamedSecuredObjectDTO
     * secured object DTO}. Prunes the ACLs to those parts relevant for the currently authenticated user. 
     * 
     * @param securityDTOFactory
     *            the {@link SecurityDTOFactory} to use for DTO creation
     * @param securityService
     *            the {@link SecurityService} to determine access control list and ownership
     * @param securedObject
     *            the {@link NamedSecuredObjectDTO} to add security information to
     */
    public static void addSecurityInformation(final SecurityDTOFactory securityDTOFactory,
            final SecurityService securityService, final SecuredDTO securedObject,
            Map<User, StrippedUserDTO> fromOriginalToStrippedDownUser, Map<UserGroup, StrippedUserGroupDTO> fromOriginalToStrippedDownUserGroup) {
        addSecurityInformation(securityDTOFactory, securityService, securedObject, fromOriginalToStrippedDownUser,
                fromOriginalToStrippedDownUserGroup, false);
    }

    /**
     * Adds {@link AccessControlList access control list} and {@link Ownership ownership} information for the given
     * {@link QualifiedObjectIdentifier qualified object identifier} to the provided {@link NamedSecuredObjectDTO
     * secured object DTO} by using the provided mappings of {@link SecurityUser users} and {@link UserGroupImpl user
     * groups}.
     * <p>
     * <b>NOTE:</b> This method can be used to reuse already stripped down {@link SecurityUser users} or
     * {@link UserGroupImpl user groups} in order to avoid multiple mappings of the the same instances. However, it must be
     * used with caution, especially in the context of one or more {@link UserDTO} instances, which themselves contain
     * ownership information. Reusing an {@link UserDTO} as user owner object might cause infinite relation paths.
     * </p>
     * @param securityDTOFactory
     *            the {@link SecurityDTOFactory} to use for DTO creation
     * @param securityService
     *            the {@link SecurityService} to determine access control list and ownership
     * @param securedObject
     *            the {@link NamedSecuredObjectDTO} to add security information to
     * @param fromOriginalToStrippedDownUser
     *            the {@link Map} to stripped down {@link SecurityUser user}s to use
     * @param fromOriginalToStrippedDownUserGroup
     *            the {@link Map} to stripped down {@link UserGroupImpl user group}s to use
     * @param disablePruningForCurrentUser
     *            if {@code true}, the whole ACL is added. If {@code false}, only that part of the ACL is added that is
     *            relevant for the currently authenticated user. For security checks, only that part is necessary, but
     *            when editing an ACL, all contents are required to be fetched to the UI.
     */
    public static void addSecurityInformation(final SecurityDTOFactory securityDTOFactory,
            final SecurityService securityService, final SecuredDTO securedObject,
            final Map<User, StrippedUserDTO> fromOriginalToStrippedDownUser,
            final Map<UserGroup, StrippedUserGroupDTO> fromOriginalToStrippedDownUserGroup,
            final boolean disablePruningForCurrentUser) {
        final AccessControlListAnnotation accessControlList = securityService.getAccessControlList(securedObject.getIdentifier());
        final AccessControlListDTO accessControlListDTO = securityDTOFactory.createAccessControlListDTO(
                accessControlList == null ? null : accessControlList.getAnnotation(), fromOriginalToStrippedDownUser,
                fromOriginalToStrippedDownUserGroup);
        if (disablePruningForCurrentUser) {
            securedObject.setAccessControlList(accessControlListDTO);
        } else {
            final User user = securityService.getCurrentUser();
            final Iterable<StrippedUserGroupDTO> userGroups = user == null
                    ? Collections.emptySet()
                    : getUserGroupsForUser(securityService, securityDTOFactory, user);
            final Iterable<StrippedUserGroupDTO> allUserGroups2 = getUserGroupsForUser(securityService,
                    securityDTOFactory, securityService.getAllUser());
            securedObject.setAccessControlList(
                    securityDTOFactory.pruneAccessControlListForUser(accessControlListDTO, userGroups,
                            allUserGroups2));
        }
        final OwnershipAnnotation ownership = securityService.getOwnership(securedObject.getIdentifier());
        securedObject.setOwnership(
                securityDTOFactory.createOwnershipDTO(ownership == null ? null : ownership.getAnnotation(),
                        fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup));
    }
    
    /** Get the UserGroups for the user alluser. */
    private static Iterable<StrippedUserGroupDTO> getUserGroupsForUser(SecurityService securityService,
            SecurityDTOFactory securityDTOFactory, User user) {
        Iterable<StrippedUserGroupDTO> allUserGroups;
        if (user != null) {
            allUserGroups = securityDTOFactory.createStrippedUserGroupDTOFromUserGroups(
                    user.getUserGroups(), new HashMap<>());
        } else {
            allUserGroups = Collections.emptySet();
        }
        return allUserGroups;
    }
}
