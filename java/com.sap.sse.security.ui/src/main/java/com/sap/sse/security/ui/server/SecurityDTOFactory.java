package com.sap.sse.security.ui.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sse.common.Util;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.Social;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.Account.AccountType;
import com.sap.sse.security.shared.dto.AccessControlListAnnotationDTO;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.AccountDTO;
import com.sap.sse.security.shared.dto.OwnershipAnnotationDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.RoleDTO;
import com.sap.sse.security.shared.dto.RoleDefinitionDTO;
import com.sap.sse.security.shared.dto.StrippedUserDTO;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.SocialUserAccount;
import com.sap.sse.security.shared.UsernamePasswordAccount;
import com.sap.sse.security.shared.impl.AccessControlList;
import com.sap.sse.security.shared.impl.Ownership;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;
import com.sap.sse.security.ui.oauth.client.SocialUserDTO;
import com.sap.sse.security.ui.shared.UsernamePasswordAccountDTO;

public class SecurityDTOFactory {
    private StrippedUserDTO createUserDTOFromUser(User user,
            Map<User, StrippedUserDTO> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroupDTO> fromOriginalToStrippedDownUserGroup) {
        StrippedUserDTO result;
        if (user == null) {
            result = null;
        } else {
            result = fromOriginalToStrippedDownUser.get(user);
            if (result == null) {
                final StrippedUserDTO preResult = new StrippedUserDTO(user.getName());
                result = preResult;
                fromOriginalToStrippedDownUser.put(user, result);
            }
        }
        return result;
    }

    private UserDTO createUserDTOFromUser(User user, Map<UserGroup, UserGroupDTO> fromOriginalToStrippedDownTenant,
            Map<User, StrippedUserDTO> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroupDTO> fromOriginalToStrippedDownUserGroup, SecurityService securityService) {
        UserDTO userDTO;
        Map<AccountType, Account> accounts = user.getAllAccounts();
        List<AccountDTO> accountDTOs = new ArrayList<>();
        for (Account a : accounts.values()){
            switch (a.getAccountType()) {
            case SOCIAL_USER:
                accountDTOs.add(createSocialUserDTO((SocialUserAccount) a));
                break;
    
            default:
                UsernamePasswordAccount upa = (UsernamePasswordAccount) a;
                accountDTOs.add(new UsernamePasswordAccountDTO(upa.getName(), upa.getSaltedPassword(), upa.getSalt()));
                break;
            }
        }
        userDTO = new UserDTO(user.getName(), user.getEmail(), user.getFullName(), user.getCompany(),
                user.getLocale() != null ? user.getLocale().toLanguageTag() : null, user.isEmailValidated(),
                accountDTOs, createRolesDTOs(user.getRoles(), fromOriginalToStrippedDownTenant, fromOriginalToStrippedDownUser,
                        fromOriginalToStrippedDownUserGroup), /* default tenant filled in later */ null,
                user.getPermissions(),
                createUserGroupDTOsFromUserGroups(securityService.getUserGroupsOfUser(user), fromOriginalToStrippedDownTenant,
                        fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup));
        fromOriginalToStrippedDownUser.put(user, userDTO);
        userDTO.setDefaultTenantForCurrentServer(createUserGroupDTOFromUserGroup(securityService.getDefaultTenantForCurrentUser(),
                fromOriginalToStrippedDownUser,
                fromOriginalToStrippedDownUserGroup));
        SecurityDTOUtil.addSecurityInformation(this, securityService, userDTO, user.getIdentifier());
        return userDTO;
    }


    private Iterable<RoleDTO> createRolesDTOs(Iterable<Role> roles,
            Map<UserGroup, UserGroupDTO> fromOriginalToStrippedDownTenant,
            Map<User, StrippedUserDTO> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroupDTO> fromOriginalToStrippedDownUserGroup) {
        return Util.map(roles, role->createRoleDTO(role,
                fromOriginalToStrippedDownTenant, fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup));
    }

    private RoleDTO createRoleDTO(Role role, Map<UserGroup, UserGroupDTO> fromOriginalToStrippedDownTenant,
            Map<User, StrippedUserDTO> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroupDTO> fromOriginalToStrippedDownUserGroup) {
        RoleDefinition rdef = role.getRoleDefinition();
        RoleDefinitionDTO rdefDTO = new RoleDefinitionDTO(rdef.getId(), rdef.getName());
        return new RoleDTO(rdefDTO,
                createUserGroupDTOFromUserGroup(role.getQualifiedForTenant(),
                fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup),
                createUserDTOFromUser(role.getQualifiedForUser(),
                        fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup));
    }

    private RoleDefinitionDTO createRoleDefinitionDTO(final RoleDefinition roleDefinition,
            final SecurityService securityService, final Map<User, StrippedUserDTO> fromOriginalToStrippedDownUser,
            final Map<UserGroup, UserGroupDTO> fromOriginalToStrippedDownUserGroup) {
        final RoleDefinitionDTO roleDefDTO = new RoleDefinitionDTO(roleDefinition.getId(), roleDefinition.getName());
        roleDefDTO.setPermissions(roleDefinition.getPermissions());
        SecurityDTOUtil.addSecurityInformation(this, securityService, roleDefDTO, roleDefinition.getIdentifier());
        return roleDefDTO;
    }

    public RoleDefinitionDTO createRoleDefinitionDTO(final RoleDefinition roleDefinition,
            final SecurityService securityService) {
        return createRoleDefinitionDTO(roleDefinition, securityService, new HashMap<>(), new HashMap<>());
    }

    public Iterable<RoleDefinitionDTO> createRoleDefinitionDTOs(final Iterable<RoleDefinition> roleDefinitions,
            final SecurityService securityService) {
        final Map<User, StrippedUserDTO> fromOriginalToStrippedDownUser = new HashMap<>();
        final Map<UserGroup, UserGroupDTO> fromOriginalToStrippedDownUserGroup = new HashMap<>();
        return Util.map(roleDefinitions, roleDefinition -> createRoleDefinitionDTO(roleDefinition, securityService,
                fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup));
    }

    public SocialUserDTO createSocialUserDTO(SocialUserAccount socialUser) {
        SocialUserDTO socialUserDTO = new SocialUserDTO(socialUser.getProperty(Social.PROVIDER.name()));
        socialUserDTO.setSessionId(socialUser.getSessionId());
        for (Social s : Social.values()){
            socialUserDTO.setProperty(s.name(), socialUser.getProperty(s.name()));
        }

        return socialUserDTO;
    }

    public UserDTO createUserDTOFromUser(User user, SecurityService securityService) {
        return createUserDTOFromUser(user, new HashMap<>(), new HashMap<>(), new HashMap<>(), securityService);
    }

    private Iterable<UserGroupDTO> createUserGroupDTOsFromUserGroups(Iterable<UserGroup> userGroups,
            Map<UserGroup, UserGroupDTO> fromOriginalToStrippedDownTenant,
            Map<User, StrippedUserDTO> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroupDTO> fromOriginalToStrippedDownUserGroup) {
        final List<UserGroupDTO> result;
        if (userGroups == null) {
            result = null;
        } else {
            result = new ArrayList<>();
            for (final UserGroup userGroup : userGroups) {
                result.add(createUserGroupDTOFromUserGroup(userGroup, fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup));
            }
        }
        return result;
    }

    /**
     * Produces a stripped-down {@link UserGroup} object that has stripped-down {@link User} objects
     * with their default tenants stripped down and mapped by this same method recursively where
     * for a single {@link User} object only a single stripped-down user object will be created,
     * as will for tenants.
     */
    UserGroupDTO createUserGroupDTOFromUserGroup(UserGroup userGroup,
            Map<User, StrippedUserDTO> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroupDTO> fromOriginalToStrippedDownUserGroup) {
        final UserGroupDTO result;
        if (userGroup == null) {
            result = null;
        } else {
            if (fromOriginalToStrippedDownUserGroup.containsKey(userGroup)) {
                result = fromOriginalToStrippedDownUserGroup.get(userGroup);
            } else {
                result = new UserGroupDTO(userGroup.getId(), userGroup.getName());
                fromOriginalToStrippedDownUserGroup.put(userGroup, result);
                for (final User user : userGroup.getUsers()) {
                    result.add(createUserDTOFromUser(user, fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup));
                }
            }
        }
        return result;
    }


    // public OwnershipAnnotation createOwnershipAnnotationDTO(OwnershipAnnotation ownershipAnnotation) {
    // return new OwnershipAnnotation(createOwnershipDTO(ownershipAnnotation.getAnnotation()),
    // ownershipAnnotation.getIdOfAnnotatedObject(),
    // ownershipAnnotation.getDisplayNameOfAnnotatedObject());
    // }
    
    // public OwnershipAnnotation createOwnershipAnnotationDTO(OwnershipAnnotation ownershipAnnotation,
    // Map<UserGroup, UserGroup> fromOriginalToStrippedDownTenant,
    // Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser,
    // Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup) {
    // return new OwnershipAnnotation(createOwnershipDTO(ownershipAnnotation.getAnnotation(),
    // fromOriginalToStrippedDownUser,
    // fromOriginalToStrippedDownUserGroup),
    // ownershipAnnotation.getIdOfAnnotatedObject(),
    // ownershipAnnotation.getDisplayNameOfAnnotatedObject());
    // }
    
    // public Ownership createOwnershipDTO(Ownership ownership) {
    // return createOwnershipDTO(ownership, new HashMap<>(), new HashMap<>());
    // }
    
    public OwnershipDTO createOwnershipDTO(Ownership ownership,
            Map<User, StrippedUserDTO> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroupDTO> fromOriginalToStrippedDownUserGroup) {
        final OwnershipDTO result;
        if (ownership == null) {
            result = null;
        } else {
            result = new OwnershipDTO(
                    createUserDTOFromUser(ownership.getUserOwner(), fromOriginalToStrippedDownUser,
                            fromOriginalToStrippedDownUserGroup),
                    createUserGroupDTOFromUserGroup(ownership.getTenantOwner(), fromOriginalToStrippedDownUser,
                            fromOriginalToStrippedDownUserGroup));
        }
        return result;
    }
    
    public AccessControlListAnnotationDTO createAccessControlListAnnotationDTO(
            AccessControlListAnnotation aclAnnotation) {
        return new AccessControlListAnnotationDTO(createAccessControlListDTO(aclAnnotation.getAnnotation()),
                aclAnnotation.getIdOfAnnotatedObject(), aclAnnotation.getDisplayNameOfAnnotatedObject());
    }
    
    public AccessControlListAnnotationDTO createAccessControlListAnnotationDTO(
            AccessControlListAnnotation aclAnnotation,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownTenant,
            Map<User, StrippedUserDTO> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroupDTO> fromOriginalToStrippedDownUserGroup) {
        return new AccessControlListAnnotationDTO(
                createAccessControlListDTO(aclAnnotation.getAnnotation(),
                fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup),
                aclAnnotation.getIdOfAnnotatedObject(), aclAnnotation.getDisplayNameOfAnnotatedObject());
    }
        
    public AccessControlListDTO createAccessControlListDTO(AccessControlList acl) {
        return createAccessControlListDTO(acl, new HashMap<>(), new HashMap<>());
    }

    public AccessControlListDTO createAccessControlListDTO(AccessControlList acl,
            Map<User, StrippedUserDTO> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroupDTO> fromOriginalToStrippedDownUserGroup) {
        final AccessControlListDTO result;
        if (acl == null) {
            result = null;
        } else {
            Map<UserGroupDTO, Set<String>> permissionMapDTO = new HashMap<>();
            for (final Entry<UserGroup, Set<String>> actionForGroup : acl.getActionsByUserGroup().entrySet()) {
                permissionMapDTO.put(
                        createUserGroupDTOFromUserGroup(actionForGroup.getKey(), fromOriginalToStrippedDownUser,
                                fromOriginalToStrippedDownUserGroup),
                        actionForGroup.getValue());
            }
            result = new AccessControlListDTO(permissionMapDTO);
        }
        return result;
    }

    /**
     * prunes the {@link AccessControlList} for the given {@link SecurityUser filterForUser} by removing all user groups
     * the user is not in from the resulting ACL.
     * @param fromOriginalToStrippedDownUser 
     * @param fromOriginalToStrippedDownUserGroup 
     */
    public AccessControlListDTO pruneAccessControlListForUser(AccessControlListDTO acl, StrippedUserDTO filterForUser) {
        final AccessControlListDTO result;
        final Collection<UserGroupDTO> userGroups = Util.createSet(filterForUser.getUserGroups());

        final Map<UserGroupDTO, Set<String>> actionsByUserGroup = new HashMap<>();
        for (final Entry<UserGroupDTO, Set<String>> entry : acl.getActionsByUserGroup().entrySet()) {
            if (userGroups.contains(entry.getKey())) {
                UserGroupDTO key = entry.getKey();
                actionsByUserGroup.put(key, entry.getValue());
            }
        }
        result = new AccessControlListDTO(actionsByUserGroup);
        return result;
    }

    public static Ownership ownerFromDTO(OwnershipDTO ownershipDTO, SecurityService securityService) {
        User ownerUser = null;
        UserGroup ownerGroup = null;
        if (ownershipDTO.getUserOwner() != null) {
            ownerUser = securityService.getUserByName(ownershipDTO.getUserOwner().getName());
        }
        if (ownershipDTO.getTenantOwner() != null) {
            ownerGroup = securityService.getUserGroup(ownershipDTO.getTenantOwner().getId());
        }
        return new Ownership(ownerUser, ownerGroup);
    }

    public OwnershipAnnotationDTO createOwnerShipAnnotationDTO(OwnershipAnnotation annotation) {
        OwnershipDTO ownerShipDTO = createOwnershipDTO(annotation.getAnnotation(), new HashMap<>(), new HashMap<>());
        return new OwnershipAnnotationDTO(ownerShipDTO, annotation.getIdOfAnnotatedObject(),
                annotation.getDisplayNameOfAnnotatedObject());
    }
}
