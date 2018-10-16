package com.sap.sse.security.ui.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.security.Credential;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.UserImpl;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.RoleImpl;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.UnauthorizedException;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.ui.client.UserManagementService;
import com.sap.sse.security.ui.oauth.client.CredentialDTO;
import com.sap.sse.security.ui.oauth.shared.OAuthException;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;

public class UserManagementServiceImpl extends RemoteServiceServlet implements UserManagementService {
    private static final long serialVersionUID = 4458564336368629101L;
    
    private static final Logger logger = Logger.getLogger(UserManagementServiceImpl.class.getName());

    private final BundleContext context;
    private final FutureTask<SecurityService> securityService;
    private final SecurityDTOFactory securityDTOFactory;

    public UserManagementServiceImpl() {
        context = Activator.getContext();
        securityDTOFactory = new SecurityDTOFactory();
        final ServiceTracker<SecurityService, SecurityService> tracker = new ServiceTracker<>(context, SecurityService.class, /* customizer */ null);
        tracker.open();
        securityService = new FutureTask<SecurityService>(new Callable<SecurityService>() {
            @Override
            public SecurityService call() {
                SecurityService result = null;
                try {
                    logger.info("Waiting for SecurityService...");
                    result = tracker.waitForService(0);
                    logger.info("Obtained SecurityService "+result);
                    return result;
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Interrupted while waiting for UserStore service", e);
                }
                return result;
            }
        });
        new Thread("ServiceTracker in bundle com.sap.sse.security.ui waiting for SecurityService") {
            @Override
            public void run() {
                securityService.run();
                SecurityUtils.setSecurityManager(getSecurityService().getSecurityManager());
            }
        }.start();
    }

    private UserDTO getAllUser() {
        final User allUser = getSecurityService().getAllUser();
        return allUser == null ? null
                : securityDTOFactory.createUserDTOFromUser(allUser, getSecurityService());
    }

    @Override
    public RoleDefinition createRoleDefinition(String roleDefinitionIdAsString, String name) {
        SecurityUtils.getSubject().checkPermission(SecuredSecurityTypes.ROLE_DEFINITION.getStringPermissionForObjects(DefaultActions.CREATE, roleDefinitionIdAsString));
        return getSecurityService().createRoleDefinition(UUID.fromString(roleDefinitionIdAsString), name);
    }

    @Override
    public void deleteRoleDefinition(String roleIdAsString) {
        SecurityUtils.getSubject().checkPermission(SecuredSecurityTypes.ROLE_DEFINITION.getStringPermissionForObjects(DefaultActions.DELETE, roleIdAsString));
        getSecurityService().deleteRoleDefinition(getSecurityService().getRoleDefinition(UUID.fromString(roleIdAsString)));
    }

    @Override
    public void updateRoleDefinition(RoleDefinition roleDefinitionWithNewProperties) {
        SecurityUtils.getSubject().checkPermission(SecuredSecurityTypes.ROLE_DEFINITION.getStringPermissionForObjects(DefaultActions.UPDATE, roleDefinitionWithNewProperties.getId().toString()));
        getSecurityService().updateRoleDefinition(roleDefinitionWithNewProperties);
    }

    @Override
    public ArrayList<RoleDefinition> getRoleDefinitions() {
        final ArrayList<RoleDefinition> result = new ArrayList<>();
        Util.addAll(getSecurityService().getRoleDefinitions(), result);
        return result;
    }

    @Override
    public QualifiedObjectIdentifier setOwnership(Ownership ownership, QualifiedObjectIdentifier idOfOwnedObject,
            String displayNameOfOwnedObject) {
        final QualifiedObjectIdentifier result;
        if (SecurityUtils.getSubject().isPermitted(idOfOwnedObject.getStringPermission(DefaultActions.CHANGE_OWNERSHIP))) {
            getSecurityService().setOwnership(new OwnershipAnnotation(ownership, idOfOwnedObject, displayNameOfOwnedObject));
            result = idOfOwnedObject;
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public OwnershipAnnotation getOwnership(QualifiedObjectIdentifier idOfOwnedObject) {
        return getSecurityService().getOwnership(idOfOwnedObject);
    }

    @Override
    public Collection<AccessControlListAnnotation> getAccessControlLists() throws UnauthorizedException {
        List<AccessControlListAnnotation> acls = new ArrayList<>();
        for (AccessControlListAnnotation acl : getSecurityService().getAccessControlLists()) {
            if (SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.ACCESS_CONTROL_LIST.getStringPermissionForObjects(DefaultActions.READ, acl.getIdOfAnnotatedObject().toString()))) {
                acls.add(securityDTOFactory.createAccessControlListAnnotationDTO(acl));
            }
        }
        return acls;
    }

    @Override
    public AccessControlListAnnotation getAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject) {
        SecurityUtils.getSubject().checkPermission(SecuredSecurityTypes.ACCESS_CONTROL_LIST.getStringPermissionForObjects(DefaultActions.READ, idOfAccessControlledObject.toString()));
        return securityDTOFactory.createAccessControlListAnnotationDTO(getSecurityService().getAccessControlList(idOfAccessControlledObject));
    }

    @Override
    public AccessControlList updateAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, Map<String, Set<String>> permissionStrings) throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.ACCESS_CONTROL_LIST.getStringPermissionForObjects(DefaultActions.UPDATE, idOfAccessControlledObject.toString()))) {
            Map<UserGroup, Set<String>> permissionMap = new HashMap<>();
            for (String group : permissionStrings.keySet()) {
                permissionMap.put(getSecurityService().getUserGroupByName(group), permissionStrings.get(group));
            }
            return securityDTOFactory.createAccessControlListDTO(getSecurityService().updateAccessControlList(idOfAccessControlledObject, permissionMap));
        } else {
            throw new UnauthorizedException("Not permitted to grant and revoke permissions for user");
        }
    }

    @Override
    public AccessControlList addToAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, String groupIdAsString, String action) throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.ACCESS_CONTROL_LIST.getStringPermissionForObjects(DefaultActions.UPDATE, idOfAccessControlledObject.toString()))) {
            UserGroup userGroup = getUserGroup(groupIdAsString);
            return securityDTOFactory.createAccessControlListDTO(getSecurityService().addToAccessControlList(idOfAccessControlledObject, userGroup, action));
        } else {
            throw new UnauthorizedException("Not permitted to grant permission for user");
        }
    }

    private UserGroup getUserGroup(String groupIdAsString) {
        UUID groupId = UUID.fromString(groupIdAsString);
        UserGroup userGroup = getSecurityService().getUserGroup(groupId);
        return userGroup;
    }

    @Override
    public AccessControlList removeFromAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, String groupOrTenantIdAsString, String permission) throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("tenant:revoke_permission:" + groupOrTenantIdAsString)) {
            UserGroup userGroup = getUserGroup(groupOrTenantIdAsString);
            return securityDTOFactory.createAccessControlListDTO(getSecurityService().removeFromAccessControlList(idOfAccessControlledObject, userGroup, permission));
        } else {
            throw new UnauthorizedException("Not permitted to revoke permission for user");
        }
    }

    @Override
    public Collection<UserGroup> getUserGroups() {
        List<UserGroup> userGroups = new ArrayList<>();
        final Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser = new HashMap<>();
        final Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup = new HashMap<>();
        for (UserGroup g : getSecurityService().getUserGroupList()) {
            if (SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.USER_GROUP.getStringPermissionForObjects(DefaultActions.READ, g.getId().toString()))) {
                UserGroup userGroupDTO = securityDTOFactory.createUserGroupDTOFromUserGroup(g, fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup);
                userGroups.add(userGroupDTO);
            }
        }
        return userGroups;
    }

    @Override
    public UserGroup getUserGroupByName(String userGroupName) throws UnauthorizedException {
        final UserGroup userGroup = getSecurityService().getUserGroupByName(userGroupName);
        if (userGroup == null || SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.USER_GROUP.getStringPermissionForObjects(DefaultActions.READ, userGroup.getId().toString()))) {
            final Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser = new HashMap<>();
            final Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup = new HashMap<>();
            return userGroup==null?null:securityDTOFactory.createUserGroupDTOFromUserGroup(userGroup, fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup);
        } else {
            throw new UnauthorizedException("Not permitted to read user group "+userGroupName);
        }
    }

    @Override
    public UserDTO getUserByName(String username) throws UnauthorizedException {
        final User user = getSecurityService().getUserByName(username);
        if (user == null || SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.USER.getStringPermissionForObjects(DefaultActions.READ, user.getName()))) {
            return user==null?null:securityDTOFactory.createUserDTOFromUser(user, getSecurityService());
        } else {
            throw new UnauthorizedException("Not permitted to read user "+username);
        }
    }

    @Override
    public UserGroup createUserGroup(String name, String nameOfTenantOwner) throws UnauthorizedException, UserGroupManagementException {
        if (SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.USER_GROUP.getStringPermission(DefaultActions.CREATE))) {
            UUID newTenantId = UUID.randomUUID();
            UserGroup userGroup;
            try {
                userGroup = getSecurityService().createUserGroup(newTenantId, name);
            } catch (UserGroupManagementException e) {
                throw new UserGroupManagementException(e.getMessage());
            }
            getSecurityService().setOwnership(SecuredSecurityTypes.USER_GROUP.getQualifiedObjectIdentifier(newTenantId.toString()),
                    getSecurityService().getCurrentUser(), getSecurityService().getUserGroupByName(nameOfTenantOwner), name);
            final Map<SecurityUser, SecurityUser> fromOriginalToStrippedDownUser = new HashMap<>();
            final Map<UserGroup, UserGroup> fromOriginalToStrippedDownUserGroup = new HashMap<>();
            return securityDTOFactory.createUserGroupDTOFromUserGroup(userGroup, fromOriginalToStrippedDownUser, fromOriginalToStrippedDownUserGroup);
        } else {
            throw new UnauthorizedException("Not permitted to create user groups");
        }
    }

    @Override
    public SuccessInfo deleteUserGroup(String userGroupIdAsString) throws UnauthorizedException {
        try {
        return getSecurityService().checkPermissionAndDeleteOwnershipForObjectRemoval(SecuredSecurityTypes.USER_GROUP,
                userGroupIdAsString, () -> {
                    try {
                        final UUID userGroupId = UUID.fromString(userGroupIdAsString);
                        final UserGroup userGroup = getSecurityService().getUserGroup(userGroupId);
                        getSecurityService().deleteUserGroup(userGroup);
                        return new SuccessInfo(true, "Deleted user group: " + userGroup.getName() + ".",
                                /* redirectURL */ null, null);
                    } catch (UserGroupManagementException e) {
                        return new SuccessInfo(false, "Could not delete user group.", /* redirectURL */ null, null);
                    }
                });
        } catch (AuthorizationException e) {
            return new SuccessInfo(false, "You are not permitted to delete user group " + userGroupIdAsString, /* redirectURL */ null,
                    null);
        }
    }

    @Override
    public void addUserToUserGroup(String userGroupIdAsString, String username) throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.USER_GROUP.getStringPermissionForObjects(DefaultActions.UPDATE, userGroupIdAsString))) {
            final UserGroup tenant = getSecurityService().getUserGroup(UUID.fromString(userGroupIdAsString));
            getSecurityService().addUserToUserGroup(tenant, getSecurityService().getUserByName(username));
        } else {
            throw new UnauthorizedException("Not permitted to add user to group");
        }
    }

    @Override
    public void removeUserFromUserGroup(String userGroupIdAsString, String username) throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.USER_GROUP.getStringPermissionForObjects(DefaultActions.DELETE, userGroupIdAsString))) {
            final UserGroup userGroup = getSecurityService().getUserGroup(UUID.fromString(userGroupIdAsString));
            getSecurityService().removeUserFromUserGroup(userGroup, getSecurityService().getUserByName(username));
        } else {
            throw new UnauthorizedException("Not permitted to remove user from group");
        }
    }

    @Override
    public Collection<UserDTO> getUserList() throws UnauthorizedException {
        List<UserDTO> users = new ArrayList<>();
        for (User u : getSecurityService().getUserList()) {
            if (SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.USER.getStringPermissionForObjects(DefaultActions.READ, u.getName()))) {
                UserDTO userDTO = securityDTOFactory.createUserDTOFromUser(u, getSecurityService());
                users.add(userDTO);
            }
        }
        return users;
    }

    @Override
    public Pair<UserDTO, UserDTO> getCurrentUser() throws UnauthorizedException {
        logger.fine("Request: " + getThreadLocalRequest().getRequestURL());
        User user = getSecurityService().getCurrentUser();
        if (user == null) {
            return new Pair<UserDTO, UserDTO>(null, getAllUser());
        }
        if (SecurityUtils.getSubject().isPermitted("user:view:" + user.getName())) {
            return new Pair<UserDTO, UserDTO>(securityDTOFactory.createUserDTOFromUser(user, getSecurityService()),
                    getAllUser());
        } else {
            throw new UnauthorizedException("Not permitted to view current user");
        }
    }

    @Override
    public SuccessInfo login(String username, String password) {
        try {
            String redirectURL = getSecurityService().login(username, password);
            UserDTO user = securityDTOFactory.createUserDTOFromUser(getSecurityService().getUserByName(username),
                    getSecurityService());
            return new SuccessInfo(true, "Success. Redirecting to " + redirectURL, redirectURL,
                    new Pair<UserDTO, UserDTO>(user, getAllUser()));
        } catch (UserManagementException | AuthenticationException e) {
            return new SuccessInfo(false, SuccessInfo.FAILED_TO_LOGIN, /* redirectURL */ null, null);
        }
    }

    @Override
    public SuccessInfo logout() {
        logger.info("Logging out user: " + SecurityUtils.getSubject());
        getSecurityService().logout();
        getHttpSession().invalidate();
        final Cookie cookie = new Cookie(UserManagementConstants.LOCALE_COOKIE_NAME, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        getThreadLocalResponse().addCookie(cookie);
        logger.info("Invalidated HTTP session");
        return new SuccessInfo(true, "Logged out.", /* redirectURL */ null, null);
    }

    public UserDTO createSimpleUser(String username, String email, String password, String fullName, String company,
            String localeName, String validationBaseURL, String tenantOwnerName)
            throws UserManagementException, MailException, UnauthorizedException {
        final UserImpl u = getSecurityService().setOwnershipCheckCreatePermissionAndRevertOnError(tenantOwnerName,
                SecuredSecurityTypes.USER, username, username, () -> {
                    try {
                        return getSecurityService().createSimpleUser(username, email, password, fullName, company,
                                getLocaleFromLocaleName(localeName), validationBaseURL, tenantOwnerName);
                    } catch (UserManagementException | UserGroupManagementException e) {
                        logger.log(Level.SEVERE, "Error creating user " + username, e);
                        throw new UserManagementException(e.getMessage());
                    }
                });
        return securityDTOFactory.createUserDTOFromUser(u, getSecurityService());
    }


    @Override
    public void updateSimpleUserPassword(final String username, String oldPassword, String passwordResetSecret, String newPassword) throws UserManagementException {
        if (SecurityUtils.getSubject().isPermitted("user:edit:" + username)
            // someone knew a username and the correct password for that user
         || (oldPassword != null && getSecurityService().checkPassword(username, oldPassword))
            // someone provided the correct password reset secret for the correct username
         || (passwordResetSecret != null && getSecurityService().checkPasswordResetSecret(username, passwordResetSecret))) {
            getSecurityService().updateSimpleUserPassword(username, newPassword);
            new Thread("sending updated password to user "+username+" by e-mail") {
                @Override public void run() {
                    try {
                        getSecurityService().sendMail(username, "Password Changed", "Somebody changed your password for your user named "+username+".\nIf that wasn't you, please contact sailing_analytics@sap.com via email.");
                    } catch (MailException e) {
                        logger.log(Level.SEVERE, "Error sending new password to user "+username+" by e-mail", e);
                    }
                }
            }.start();
        } else {
            throw new UserManagementException(UserManagementException.INVALID_CREDENTIALS);
        }
    }

    @Override
    public UserDTO updateUserProperties(final String username, String fullName, String company, String localeName) throws UserManagementException {
        SecurityUtils.getSubject().checkPermission("user:edit:" + username);
        getSecurityService().updateUserProperties(username, fullName, company,
                getLocaleFromLocaleName(localeName));
        return securityDTOFactory.createUserDTOFromUser(getSecurityService().getUserByName(username), getSecurityService());
    }

    private Locale getLocaleFromLocaleName(String localeName) {
        try {
            return localeName == null || localeName.isEmpty() ? null : Locale.forLanguageTag(localeName);
        } catch (Exception e) {
            logger.log(Level.WARNING, e, () -> "Error while parsing locale with name '" + localeName + "'");
            return null;
        }
    }
    
    @Override
    public void updateSimpleUserEmail(String username, String newEmail, String validationBaseURL) throws UserManagementException, MailException {
        SecurityUtils.getSubject().checkPermission("user:edit:" + username);
        getSecurityService().updateSimpleUserEmail(username, newEmail, validationBaseURL);
    }
    
    @Override
    public void resetPassword(String username, String email, String passwordResetBaseURL) throws UserManagementException, MailException {
        SecurityUtils.getSubject().checkPermission("user:edit:" + username);
        if (username == null || username.isEmpty()) {
            username = getSecurityService().getUserByEmail(email).getName();
        }
        getSecurityService().resetPassword(username, passwordResetBaseURL);
    }

    @Override
    public boolean validateEmail(String username, String validationSecret) throws UserManagementException {
        return getSecurityService().validateEmail(username, validationSecret);
    }

    @Override
    public Collection<UserDTO> getFilteredSortedUserList(String filter) throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("users:manage")) {
            List<UserDTO> users = new ArrayList<>();
            for (User u : getSecurityService().getUserList()) {
                if (filter != null && filter.isEmpty()) {
                    if (u.getName().contains(filter)) {
                        users.add(securityDTOFactory.createUserDTOFromUser(u, getSecurityService()));
                    }
                } else {
                    users.add(securityDTOFactory.createUserDTOFromUser(u, getSecurityService()));
                }
            }
            Collections.sort(users, new Comparator<UserDTO>() {
                private final NaturalComparator naturalComparator = new NaturalComparator(/* caseSensitive */ false);
                @Override
                public int compare(UserDTO u1, UserDTO u2) {
                    return naturalComparator.compare(u1.getName(), u2.getName());
                }
            });
            return users;
        } else {
            throw new UnauthorizedException("Not permitted to manage users");
        }
    }

    @Override
    public SuccessInfo setRolesForUser(String username,
            Iterable<Triple<UUID, String, String>> roleDefinitionIdAndTenantQualifierNameAndUsernames)
            throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("user:grant_permission,revoke_permission:" + username)) {
            User u = getSecurityService().getUserByName(username);
            if (u == null) {
                return new SuccessInfo(false, "User does not exist.", /* redirectURL */ null, null);
            }
            Set<Role> rolesToSet = new HashSet<>();
            for (final Triple<UUID, String, String> roleDefinitionIdAndTenantQualifierNameAndUsernameOfRoleToSet : roleDefinitionIdAndTenantQualifierNameAndUsernames) {
                final UserGroup tenant;
                if (roleDefinitionIdAndTenantQualifierNameAndUsernameOfRoleToSet.getB() == null || roleDefinitionIdAndTenantQualifierNameAndUsernameOfRoleToSet.getB().trim().isEmpty()) {
                    tenant = null;
                } else {
                    tenant = getSecurityService().getUserGroupByName(roleDefinitionIdAndTenantQualifierNameAndUsernameOfRoleToSet.getB());
                    if (tenant == null) {
                        return new SuccessInfo(false, "Tenant "+roleDefinitionIdAndTenantQualifierNameAndUsernameOfRoleToSet.getB()+
                                " does not exist.", /* redirectURL */ null, /* userDTO */ null);
                    }
                }
                try {
                    rolesToSet.add(createRoleFromIDs(roleDefinitionIdAndTenantQualifierNameAndUsernameOfRoleToSet.getA(),
                            tenant==null?null:tenant.getId(), roleDefinitionIdAndTenantQualifierNameAndUsernameOfRoleToSet.getC()));
                } catch (UserManagementException e) {
                    return new SuccessInfo(false, e.getMessage(), /* redirectURL */ null, /* userDTO */ null);
                }
            }
            Set<Role> roleDefinitionsToRemove = new HashSet<>();
            Util.addAll(u.getRoles(), roleDefinitionsToRemove);
            Util.removeAll(rolesToSet, roleDefinitionsToRemove);
            for (Role roleToRemove : roleDefinitionsToRemove) {
                getSecurityService().removeRoleFromUser(u, roleToRemove);
            }
            Set<Role> rolesToAdd = new HashSet<>();
            Util.addAll(rolesToSet, rolesToAdd);
            Util.removeAll(u.getRoles(), rolesToAdd);
            for (Role roleToAdd : rolesToAdd) {
                getSecurityService().addRoleForUser(u, roleToAdd);
            }
            final String message = "Set roles " + roleDefinitionIdAndTenantQualifierNameAndUsernames + " for user " + username;
            logger.info(message);
            final UserDTO userDTO = securityDTOFactory.createUserDTOFromUser(u, getSecurityService());
            return new SuccessInfo(true, message, /* redirectURL */null,
                    new Pair<UserDTO, UserDTO>(userDTO, getAllUser()));
        } else {
            throw new UnauthorizedException("Not permitted to grant permissions to user");
        }
    }
    
    private Role createRoleFromIDs(UUID roleDefinitionId, UUID qualifyingTenantId, String qualifyingUsername) throws UserManagementException {
        final User user;
        if (qualifyingUsername == null || qualifyingUsername.trim().isEmpty()) {
            user = null;
        } else {
            user = getSecurityService().getUserByName(qualifyingUsername);
            if (user == null) {
                throw new UserManagementException("User "+qualifyingUsername+" not found for role qualification");
            }
        }
        return new RoleImpl(
                getSecurityService().getRoleDefinition(roleDefinitionId),
                qualifyingTenantId == null ? null : getSecurityService().getUserGroup(qualifyingTenantId), user);
    }

    @Override
    public SuccessInfo setPermissionsForUser(String username, Iterable<WildcardPermission> permissions) throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("user:grant_permission,revoke_permission:" + username)) {
            User u = getSecurityService().getUserByName(username);
            if (u == null) {
                return new SuccessInfo(false, "User does not exist.", /* redirectURL */null, null);
            }
            Set<WildcardPermission> permissionsToRemove = new HashSet<>();
            Util.addAll(u.getPermissions(), permissionsToRemove);
            Util.removeAll(permissions, permissionsToRemove);
            for (WildcardPermission permissionToRemove : permissionsToRemove) {
                getSecurityService().removePermissionFromUser(username, permissionToRemove);
            }
            Set<WildcardPermission> permissionsToAdd = new HashSet<>();
            Util.addAll(permissions, permissionsToAdd);
            Util.removeAll(u.getPermissions(), permissionsToAdd);
            for (WildcardPermission permissionToAdd : permissionsToAdd) {
                getSecurityService().addPermissionForUser(username, permissionToAdd);
            }
            final String message = "Set roles " + permissions + " for user " + username;
            final UserDTO userDTO = securityDTOFactory.createUserDTOFromUser(u, getSecurityService());
            return new SuccessInfo(true, message, /* redirectURL */null,
                    new Pair<UserDTO, UserDTO>(userDTO, getAllUser()));
        } else {
            throw new UnauthorizedException("Not permitted to grant or revoke permissions for user "+username);
        }
    }

    @Override
    public SuccessInfo deleteUser(String username) throws UnauthorizedException {
        try {
            return getSecurityService().checkPermissionAndDeleteOwnershipForObjectRemoval(SecuredSecurityTypes.USER,
                    username, () -> {
                        try {
                            getSecurityService().deleteUser(username);
                            return new SuccessInfo(true, "Deleted user: " + username + ".", /* redirectURL */ null,
                                    null);
                        } catch (UserManagementException e) {
                            return new SuccessInfo(false, "Could not delete user.", /* redirectURL */ null, null);
                        }
                    });
        } catch (AuthorizationException e) {
            return new SuccessInfo(false, "You are not permitted to delete user " + username, /* redirectURL */ null,
                    null);
        }
    }

    @Override
    public Map<String, String> getSettings() {
        Map<String, String> settings = new TreeMap<String, String>();
        for (Entry<String, Object> e : getSecurityService().getAllSettings().entrySet()){
            settings.put(e.getKey(), e.getValue().toString());
        }
        return settings;
    }

    @Override
    public void setSetting(String key, String clazz, String setting) {
        if (clazz.equals(Boolean.class.getName())){
            getSecurityService().setSetting(key, Boolean.parseBoolean(setting));
        }
        else if (clazz.equals(Integer.class.getName())){
            getSecurityService().setSetting(key, Integer.parseInt(setting));
        }
        else {
            getSecurityService().setSetting(key, setting);
        }
        getSecurityService().refreshSecurityConfig(getServletContext());
    }

    @Override
    public Map<String, String> getSettingTypes() {
        Map<String, String> settingTypes = new TreeMap<String, String>();
        for (Entry<String, Class<?>> e : getSecurityService().getAllSettingTypes().entrySet()) {
            settingTypes.put(e.getKey(), e.getValue().getName());
        }
        return settingTypes;
    }
    
    //--------------------------------------------------------- OAuth Implementations -------------------------------------------------------------------------

    @Override
    public String getAuthorizationUrl(CredentialDTO credential) throws OAuthException {
        logger.info("callback url: " + credential.getRedirectUrl());
        String authorizationUrl = null;
        try {
            authorizationUrl = getSecurityService().getAuthenticationUrl(createCredentialFromDTO(credential));
        } catch (UserManagementException e) {
            throw new OAuthException(e.getMessage());
        }
        return authorizationUrl;
    }

    @Override
    public Pair<UserDTO, UserDTO> verifySocialUser(CredentialDTO credentialDTO) {
        User user = null;
        try {
            user = getSecurityService().verifySocialUser(createCredentialFromDTO(credentialDTO));
        } catch (UserManagementException e) {
            e.printStackTrace();
        }
        final UserDTO userDTO = securityDTOFactory.createUserDTOFromUser(user, getSecurityService());
        return new Pair<UserDTO, UserDTO>(userDTO, getAllUser());
    }

    private HttpSession getHttpSession() {
        return getThreadLocalRequest().getSession();
    }

    private Credential createCredentialFromDTO(CredentialDTO credentialDTO){
        Credential credential = new Credential();
        credential.setAuthProvider(credentialDTO.getAuthProvider());
        credential.setAuthProviderName(credentialDTO.getAuthProviderName());
        credential.setEmail(credentialDTO.getEmail());
        credential.setLoginName(credentialDTO.getLoginName());
        credential.setPassword(credentialDTO.getPassword());
        credential.setRedirectUrl(credentialDTO.getRedirectUrl());
        credential.setState(credentialDTO.getState());
        credential.setVerifier(credentialDTO.getVerifier());
        credential.setOauthToken(credentialDTO.getOauthToken());
        return credential;
    }
    
    @Override
    public void addSetting(String key, String clazz, String setting) {
        try {
            getSecurityService().addSetting(key, Class.forName(clazz));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (UserManagementException e) {
            e.printStackTrace();
        }
        if (clazz.equals(Boolean.class.getName())){
            getSecurityService().setSetting(key, Boolean.parseBoolean(setting));
        }
        else if (clazz.equals(Integer.class.getName())){
            getSecurityService().setSetting(key, Integer.parseInt(setting));
        }
        else {
            getSecurityService().setSetting(key, setting);
        }
        getSecurityService().refreshSecurityConfig(getServletContext());
    }

    SecurityService getSecurityService() {
        try {
            return securityService.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setPreference(String username, String key, String value) throws UserManagementException, UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("user:edit:" + username)) {
            try {
                getSecurityService().setPreference(username, key, value);
            } catch (AuthorizationException e) {
                throw new UserManagementException(UserManagementException.USER_DOESNT_HAVE_PERMISSION);
            }
        } else {
            throw new UnauthorizedException("Not permitted to edit user");
        }
    }
    
    @Override
    public void setPreferences(String username, Map<String, String> keyValuePairs) throws UserManagementException, UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("user:edit:" + username)) {
            try {
                for (Entry<String, String> entry : keyValuePairs.entrySet()) {
                    getSecurityService().setPreference(username, entry.getKey(), entry.getValue());
                }
            } catch (AuthorizationException e) {
                throw new UserManagementException(UserManagementException.USER_DOESNT_HAVE_PERMISSION);
            }
        } else {
            throw new UnauthorizedException("Not permitted to edit user");
        }
    }

    @Override
    public void unsetPreference(String username, String key) throws UserManagementException, UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("user:edit:" + username)) {
            try {
                getSecurityService().unsetPreference(username, key);
            } catch (AuthorizationException e) {
                throw new UserManagementException(UserManagementException.USER_DOESNT_HAVE_PERMISSION);
            }
        } else {
            throw new UnauthorizedException("Not permitted to edit user");
        }
    }

    @Override
    public String getPreference(String username, String key) throws UserManagementException, UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("user:view:" + username)) {
            try {
                return getSecurityService().getPreference(username, key);
            } catch (AuthorizationException e) {
                throw new UserManagementException(UserManagementException.USER_DOESNT_HAVE_PERMISSION);
            }
        } else {
            throw new UnauthorizedException("Not permitted to view user");
        }
    }
    
    @Override
    public Map<String, String> getPreferences(String username, List<String> keys) throws UserManagementException, UnauthorizedException {
        Map<String, String> requestedPreferences = new HashMap<>();
        for (String key : keys) {
            requestedPreferences.put(key, getPreference(username, key));
        }
        return requestedPreferences;
    }
    
    @Override
    public Map<String, String> getAllPreferences(String username) throws UserManagementException, UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("user:view:" + username)) {
            try {
                final Map<String, String> allPreferences = getSecurityService().getAllPreferences(username);
                final Map<String, String> result = new HashMap<>();
                for (Map.Entry<String, String> entry : allPreferences.entrySet()) {
                    if(!entry.getKey().startsWith("_")) {
                        result.put(entry.getKey(), entry.getValue());
                    }
                }
                return result;
            } catch (AuthorizationException e) {
                throw new UserManagementException(UserManagementException.USER_DOESNT_HAVE_PERMISSION);
            }
        } else {
            throw new UnauthorizedException("Not permitted to view user");
        }
    }

    @Override
    public String getAccessToken(String username) {
        return getSecurityService().getAccessToken(username);
    }

    @Override
    public String getOrCreateAccessToken(String username) {
        return getSecurityService().getOrCreateAccessToken(username);
    }

}
