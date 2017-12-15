package com.sap.sse.security.ui.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sse.security.SecurityService;
import com.sap.sse.security.Social;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.Account.AccountType;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.SocialUserAccount;
import com.sap.sse.security.shared.Tenant;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.UsernamePasswordAccount;
import com.sap.sse.security.shared.impl.AccessControlListImpl;
import com.sap.sse.security.shared.impl.OwnershipImpl;
import com.sap.sse.security.shared.impl.SecurityUserImpl;
import com.sap.sse.security.shared.impl.TenantImpl;
import com.sap.sse.security.shared.impl.UserGroupImpl;
import com.sap.sse.security.ui.oauth.client.SocialUserDTO;
import com.sap.sse.security.ui.shared.AccountDTO;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UsernamePasswordAccountDTO;

public class SecurityDTOFactory {
    private SecurityUser createUserDTOFromUser(SecurityUser user, Map<Tenant, Tenant> fromOriginalToStrippedDownTenant,
            Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup) {
        SecurityUser result = fromOriginalToStrippedDownUser.get(user);
        if (result == null) {
            final SecurityUserImpl preResult = new SecurityUserImpl(user.getName(), /* default tenant to be set later: */ null);
            result = preResult;
            fromOriginalToStrippedDownUser.put(user, result);
            preResult.setDefaultTenant(createTenantDTOFromTenant(user.getDefaultTenant(), fromOriginalToStrippedDownTenant, fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup));
        }
        return result;
    }

    private UserDTO createUserDTOFromUser(User user, Map<Tenant, Tenant> fromOriginalToStrippedDownTenant,
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
                accountDTOs, user.getRoles(), /* default tenant filled in later */ null,
                user.getPermissions(),
                createUserGroupDTOsFromUserGroups(securityService.getUserGroupsOfUser(user), fromOriginalToStrippedDownTenant,
                        fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup));
        fromOriginalToStrippedDownUser.put(user, userDTO);
        userDTO.setDefaultTenant(createTenantDTOFromTenant(user.getDefaultTenant(), fromOriginalToStrippedDownTenant, fromOriginalToStrippedDownUser,
                fromOriginalToStrippedDownUserGroup));
        return userDTO;
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
            Map<Tenant, Tenant> fromOriginalToStrippedDownTenant,
            Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup) {
        final List<UserGroup> result;
        if (userGroups == null) {
            result = null;
        } else {
            result = new ArrayList<>();
            for (final UserGroup userGroup : userGroups) {
                result.add(createUserGroupDTOFromUserGroup(userGroup, fromOriginalToStrippedDownTenant, fromOriginalToStrippedDownUser,
                        fromOriginalToStrippedDownUserGroup));
            }
        }
        return result;
    }

    private UserGroup createUserGroupDTOFromUserGroup(UserGroup userGroup, Map<Tenant, Tenant> fromOriginalToStrippedDownTenant,
            Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup) {
        final UserGroup result;
        if (fromOriginalToStrippedDownUserGroup.containsKey(userGroup)) {
            result = fromOriginalToStrippedDownUserGroup.get(userGroup);
        } else {
            result = new UserGroupImpl(userGroup.getId(), userGroup.getName());
            fromOriginalToStrippedDownUserGroup.put(userGroup, result);
            for (final SecurityUser user : userGroup.getUsers()) {
                result.add(createUserDTOFromUser(user, fromOriginalToStrippedDownTenant, fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup));
            }
        }
        return result;
    }

    private Tenant createTenantDTOFromTenant(Tenant tenant, Map<Tenant, Tenant> fromOriginalToStrippedDownTenant,
            Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup) {
        final Tenant result;
        if (tenant == null) {
            result = null;
        } else if (fromOriginalToStrippedDownTenant.containsKey(tenant)) {
            result = fromOriginalToStrippedDownTenant.get(tenant);
        } else {
            result = new TenantImpl(tenant.getId(), tenant.getName());
            fromOriginalToStrippedDownTenant.put(tenant, result);
            for (final SecurityUser user : tenant.getUsers()) {
                result.add(createUserDTOFromUser(user, fromOriginalToStrippedDownTenant, fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup));
            }
        }
        return result;
    }

    /**
     * Produces a stripped-down {@link Tenant} object that has stripped-down {@link User} objects
     * with their default tenants stripped down and mapped by this same method recursively where
     * for a single {@link User} object only a single stripped-down user object will be created,
     * as will for tenants.
     */
    public Tenant createTenantDTOFromTenant(Tenant tenant) {
        return createTenantDTOFromTenant(tenant, new HashMap<>(), new HashMap<>(), new HashMap<>());
    }
    
    public Ownership createOwnershipDTO(Ownership ownership) {
        return createOwnershipDTO(ownership, new HashMap<>(), new HashMap<>(), new HashMap<>());
    }
    
    public Ownership createOwnershipDTO(Ownership ownership, Map<Tenant, Tenant> fromOriginalToStrippedDownTenant,
            Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup) {
        final Ownership result;
        if (ownership == null) {
            result = null;
        } else {
            result = new OwnershipImpl(ownership.getIdOfOwnedObjectAsString(),
                    createUserDTOFromUser(ownership.getUserOwner(), fromOriginalToStrippedDownTenant, fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup),
                    createTenantDTOFromTenant(ownership.getTenantOwner(), fromOriginalToStrippedDownTenant, fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup),
                    ownership.getDisplayNameOfOwnedObject());
        }
        return result;
    }
    
    public AccessControlList createAccessControlListDTO(AccessControlList acl) {
        return createAccessControlListDTO(acl, new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public AccessControlList createAccessControlListDTO(AccessControlList acl,
            Map<Tenant, Tenant> fromOriginalToStrippedDownTenant,
            Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser,
            Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup) {
        final AccessControlList result;
        if (acl == null) {
            result = null;
        } else {
            Map<UserGroup, Set<String>> permissionMapDTO = new HashMap<>();
            for (final Entry<UserGroup, Set<String>> actionForGroup : acl.getActionsByUserGroup().entrySet()) {
                permissionMapDTO.put(
                        createUserGroupDTOFromUserGroup(actionForGroup.getKey(), fromOriginalToStrippedDownTenant,
                                fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup),
                        actionForGroup.getValue());
            }
            result = new AccessControlListImpl(acl.getIdOfAccessControlledObjectAsString(),
                    acl.getDisplayNameOfAccessControlledObject(), permissionMapDTO);
        }
        return result;
    }
}
