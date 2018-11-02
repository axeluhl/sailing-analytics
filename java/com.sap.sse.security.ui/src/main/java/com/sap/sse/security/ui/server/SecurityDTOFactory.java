package com.sap.sse.security.ui.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sse.common.Util;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.Social;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.Account.AccountType;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.RoleImpl;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.SocialUserAccount;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.UsernamePasswordAccount;
import com.sap.sse.security.shared.impl.AccessControlListImpl;
import com.sap.sse.security.shared.impl.OwnershipImpl;
import com.sap.sse.security.shared.impl.SecurityUserImpl;
import com.sap.sse.security.shared.impl.UserGroupImpl;
import com.sap.sse.security.ui.oauth.client.SocialUserDTO;
import com.sap.sse.security.ui.shared.AccountDTO;
import com.sap.sse.security.ui.shared.RoleDefinitionDTO;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UsernamePasswordAccountDTO;

public class SecurityDTOFactory {
    private SecurityUser createUserDTOFromUser(SecurityUser user, Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup) {
        SecurityUser result;
        if (user == null) {
            result = null;
        } else {
            result = fromOriginalToStrippedDownUser.get(user);
            if (result == null) {
                final SecurityUserImpl preResult = new SecurityUserImpl(user.getName(), /* default tenant to be set later: */ null);
                result = preResult;
                fromOriginalToStrippedDownUser.put(user, result);
                preResult.setDefaultTenant(createUserGroupDTOFromUserGroup(user.getDefaultTenant(), fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup));
            }
        }
        return result;
    }

    private UserDTO createUserDTOFromUser(User user, Map<UserGroup, UserGroup> fromOriginalToStrippedDownTenant,
            Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup, SecurityService securityService) {
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
        userDTO.setDefaultTenant(createUserGroupDTOFromUserGroup(user.getDefaultTenant(), fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup));
        SecurityDTOUtil.addSecurityInformation(this, securityService, userDTO, user.getIdentifier());
        return userDTO;
    }


    private Iterable<Role> createRolesDTOs(Iterable<Role> roles, Map<UserGroup, UserGroup> fromOriginalToStrippedDownTenant,
            Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup) {
        return Util.map(roles, role->createRoleDTO(role,
                fromOriginalToStrippedDownTenant, fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup));
    }

    private Role createRoleDTO(Role role, Map<UserGroup, UserGroup> fromOriginalToStrippedDownTenant,
            Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup) {
        return new RoleImpl(role.getRoleDefinition(), createUserGroupDTOFromUserGroup(role.getQualifiedForTenant(),
                fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup),
                createUserDTOFromUser(role.getQualifiedForUser(),
                        fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup));
    }

    private RoleDefinitionDTO createRoleDefinitionDTO(final RoleDefinition roleDefinition,
            final SecurityService securityService, final Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser,
            final Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup) {
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
        final Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser = new HashMap<>();
        final Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup = new HashMap<>();
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

    private Iterable<UserGroup> createUserGroupDTOsFromUserGroups(Iterable<UserGroup> userGroups,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownTenant,
            Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup) {
        final List<UserGroup> result;
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
    UserGroup createUserGroupDTOFromUserGroup(UserGroup userGroup, Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup) {
        final UserGroup result;
        if (userGroup == null) {
            result = null;
        } else {
            if (fromOriginalToStrippedDownUserGroup.containsKey(userGroup)) {
                result = fromOriginalToStrippedDownUserGroup.get(userGroup);
            } else {
                result = new UserGroupImpl(userGroup.getId(), userGroup.getName());
                fromOriginalToStrippedDownUserGroup.put(userGroup, result);
                for (final SecurityUser user : userGroup.getUsers()) {
                    result.add(createUserDTOFromUser(user, fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup));
                }
            }
        }
        return result;
    }


    public OwnershipAnnotation createOwnershipAnnotationDTO(OwnershipAnnotation ownershipAnnotation) {
        return new OwnershipAnnotation(createOwnershipDTO(ownershipAnnotation.getAnnotation()),
                ownershipAnnotation.getIdOfAnnotatedObject(),
                ownershipAnnotation.getDisplayNameOfAnnotatedObject());
    }
    
    public OwnershipAnnotation createOwnershipAnnotationDTO(OwnershipAnnotation ownershipAnnotation,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownTenant,
            Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup) {
        return new OwnershipAnnotation(createOwnershipDTO(ownershipAnnotation.getAnnotation(), fromOriginalToStrippedDownUser,
                fromOriginalToStrippedDownUserGroup),
                ownershipAnnotation.getIdOfAnnotatedObject(),
                ownershipAnnotation.getDisplayNameOfAnnotatedObject());
    }
    
    public Ownership createOwnershipDTO(Ownership ownership) {
        return createOwnershipDTO(ownership, new HashMap<>(), new HashMap<>());
    }
    
    public Ownership createOwnershipDTO(Ownership ownership, Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup) {
        final Ownership result;
        if (ownership == null) {
            result = null;
        } else {
            result = new OwnershipImpl(
                    createUserDTOFromUser(ownership.getUserOwner(), fromOriginalToStrippedDownUser,
                            fromOriginalToStrippedDownUserGroup),
                    createUserGroupDTOFromUserGroup(ownership.getTenantOwner(), fromOriginalToStrippedDownUser,
                            fromOriginalToStrippedDownUserGroup));
        }
        return result;
    }
    
    public AccessControlListAnnotation createAccessControlListAnnotationDTO(AccessControlListAnnotation aclAnnotation) {
        return new AccessControlListAnnotation(createAccessControlListDTO(aclAnnotation.getAnnotation()),
                aclAnnotation.getIdOfAnnotatedObject(), aclAnnotation.getDisplayNameOfAnnotatedObject());
    }
    
    public AccessControlListAnnotation createAccessControlListAnnotationDTO(AccessControlListAnnotation aclAnnotation,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownTenant,
            Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup) {
        return new AccessControlListAnnotation(createAccessControlListDTO(aclAnnotation.getAnnotation(),
                fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup),
                aclAnnotation.getIdOfAnnotatedObject(), aclAnnotation.getDisplayNameOfAnnotatedObject());
    }
        
    public AccessControlList createAccessControlListDTO(AccessControlList acl) {
        return createAccessControlListDTO(acl, new HashMap<>(), new HashMap<>());
    }

    public AccessControlList createAccessControlListDTO(AccessControlList acl,
            Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup) {
        final AccessControlList result;
        if (acl == null) {
            result = null;
        } else {
            Map<UserGroup, Set<String>> permissionMapDTO = new HashMap<>();
            for (final Entry<UserGroup, Set<String>> actionForGroup : acl.getActionsByUserGroup().entrySet()) {
                permissionMapDTO.put(
                        createUserGroupDTOFromUserGroup(actionForGroup.getKey(), fromOriginalToStrippedDownUser,
                                fromOriginalToStrippedDownUserGroup),
                        actionForGroup.getValue());
            }
            result = new AccessControlListImpl(permissionMapDTO);
        }
        return result;
    }
}
