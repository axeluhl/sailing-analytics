package com.sap.sse.security.ui.server;

import java.util.ArrayList;
import java.util.Collection;
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
import com.sap.sse.security.ActionWithResult;
import com.sap.sse.security.Credential;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.AdminRole;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.UnauthorizedException;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.dto.AccessControlListAnnotationDTO;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipAnnotationDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.RoleDefinitionDTO;
import com.sap.sse.security.shared.dto.StrippedUserDTO;
import com.sap.sse.security.shared.dto.StrippedUserGroupDTO;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.shared.impl.Ownership;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.UserActions;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;
import com.sap.sse.security.ui.client.UserManagementService;
import com.sap.sse.security.ui.oauth.client.CredentialDTO;
import com.sap.sse.security.ui.oauth.shared.OAuthException;
import com.sap.sse.security.ui.shared.SuccessInfo;

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
    public RoleDefinitionDTO createRoleDefinition(String roleDefinitionIdAsString, String name) {
        RoleDefinition role = getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                SecuredSecurityTypes.ROLE_DEFINITION, roleDefinitionIdAsString, name,
                new ActionWithResult<RoleDefinition>() {

                    @Override
                    public RoleDefinition run() throws Exception {
                        return getSecurityService().createRoleDefinition(UUID.fromString(roleDefinitionIdAsString),
                                name);
                    }
                });
        return securityDTOFactory.createRoleDefinitionDTO(role, getSecurityService());
    }

    @Override
    public void deleteRoleDefinition(String roleIdAsString) {
        SecurityUtils.getSubject().checkPermission(SecuredSecurityTypes.ROLE_DEFINITION.getStringPermissionForObjects(DefaultActions.DELETE, roleIdAsString));
        getSecurityService().deleteRoleDefinition(getSecurityService().getRoleDefinition(UUID.fromString(roleIdAsString)));
    }

    @Override
    public void updateRoleDefinition(RoleDefinitionDTO roleDefinitionWithNewProperties) throws UnauthorizedException {
        SecurityUtils.getSubject().checkPermission(SecuredSecurityTypes.ROLE_DEFINITION.getStringPermissionForObjects(
                DefaultActions.UPDATE, roleDefinitionWithNewProperties.getId().toString()));
        
        RoleDefinition existingRole = getSecurityService().getRoleDefinition(roleDefinitionWithNewProperties.getId());
        if (existingRole == null) {
            throw new UnauthorizedException("Role does not exist");
        }
        Set<WildcardPermission> addedPermissions = new HashSet<>(roleDefinitionWithNewProperties.getPermissions());
        addedPermissions.removeAll(existingRole.getPermissions());
        
        if (!getSecurityService().hasUserAllWildcardPermissionsForAlreadyRealizedQualifications(existingRole, addedPermissions)) {
            throw new UnauthorizedException("Not permitted to grant permissions for role "
                    + roleDefinitionWithNewProperties.getName());
        }
        
        getSecurityService().updateRoleDefinition(roleDefinitionWithNewProperties);
    }

    @Override
    public ArrayList<RoleDefinitionDTO> getRoleDefinitions() {
        final ArrayList<RoleDefinitionDTO> result = new ArrayList<>();
        Util.addAll(securityDTOFactory.createRoleDefinitionDTOs(getSecurityService().getRoleDefinitions(),
                getSecurityService()), result);
        return result;
    }

    @Override
    public QualifiedObjectIdentifier setOwnership(OwnershipDTO ownershipDTO, QualifiedObjectIdentifier idOfOwnedObject,
            String displayNameOfOwnedObject) {
        final QualifiedObjectIdentifier result;
        if (SecurityUtils.getSubject().isPermitted(idOfOwnedObject.getStringPermission(DefaultActions.CHANGE_OWNERSHIP))) {
            Ownership ownerShip = SecurityDTOFactory.ownerFromDTO(ownershipDTO, getSecurityService());
            getSecurityService()
                    .setOwnership(new OwnershipAnnotation(ownerShip,
                    idOfOwnedObject, displayNameOfOwnedObject));
            result = idOfOwnedObject;
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public OwnershipAnnotationDTO getOwnership(QualifiedObjectIdentifier idOfOwnedObject) {
        OwnershipAnnotation annotation = getSecurityService().getOwnership(idOfOwnedObject);
        return securityDTOFactory.createOwnerShipAnnotationDTO(annotation);
    }

    @Override
    public Collection<AccessControlListAnnotationDTO> getAccessControlLists() throws UnauthorizedException {
        // TODO decide whether a global getAccessControlList functionality is needed
        List<AccessControlListAnnotationDTO> acls = new ArrayList<>();
        for (AccessControlListAnnotation acl : getSecurityService().getAccessControlLists()) {
            if (SecurityUtils.getSubject()
                    .isPermitted(acl.getIdOfAnnotatedObject().getStringPermission(DefaultActions.CHANGE_ACL))) {
                acls.add(securityDTOFactory.createAccessControlListAnnotationDTO(acl));
            }
        }
        return acls;
    }

    @Override
    public AccessControlListAnnotationDTO getAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject) {
        // skip permission check for ACL linked to null object since it is public anyway
        if (idOfAccessControlledObject != null) {
        SecurityUtils.getSubject()
                .checkPermission(idOfAccessControlledObject.getStringPermission(DefaultActions.CHANGE_ACL));
        }
        return securityDTOFactory.createAccessControlListAnnotationDTO(getSecurityService().getAccessControlList(idOfAccessControlledObject));
    }

    @Override
    public AccessControlListDTO updateAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject,
            Map<String, Set<String>> permissionStrings) throws UnauthorizedException {
        if (SecurityUtils.getSubject()
                .isPermitted(idOfAccessControlledObject.getStringPermission(DefaultActions.CHANGE_ACL))) {
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
    public AccessControlListDTO addToAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject,
            String groupIdAsString, String action) throws UnauthorizedException {
        if (SecurityUtils.getSubject()
                .isPermitted(idOfAccessControlledObject.getStringPermission(DefaultActions.CHANGE_ACL))) {
            UserGroup userGroup = getUserGroup(groupIdAsString);
            return securityDTOFactory.createAccessControlListDTO(getSecurityService().addToAccessControlList(idOfAccessControlledObject, userGroup, action));
        } else {
            throw new UnauthorizedException("Not permitted to grant permission for user");
        }
    }

    private UserGroup getUserGroup(String groupIdAsString) {
        UUID groupId = UUID.fromString(groupIdAsString);
        return getSecurityService().getUserGroup(groupId);
    }

    @Override
    public AccessControlListDTO removeFromAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject,
            String groupOrTenantIdAsString, String permission) throws UnauthorizedException {
        // FIXME replace permission check with a valid one
        if (SecurityUtils.getSubject().isPermitted("tenant:revoke_permission:" + groupOrTenantIdAsString)) {
            UserGroup userGroup = getUserGroup(groupOrTenantIdAsString);
            return securityDTOFactory.createAccessControlListDTO(getSecurityService().removeFromAccessControlList(idOfAccessControlledObject, userGroup, permission));
        } else {
            throw new UnauthorizedException("Not permitted to revoke permission for user");
        }
    }

    @Override
    public Collection<UserGroupDTO> getUserGroups() {
        Map<User, StrippedUserDTO> fromOriginalToStrippedDownUser = new HashMap<>();
        Map<UserGroup, StrippedUserGroupDTO> fromOriginalToStrippedDownUserGroup = new HashMap<>();
        return getSecurityService().mapAndFilterByReadPermissionForCurrentUser(SecuredSecurityTypes.USER_GROUP,
                getSecurityService().getUserGroupList(), ug -> ug.getId().toString(),
                ug -> securityDTOFactory.createUserGroupDTOFromUserGroup(ug, fromOriginalToStrippedDownUser,
                        fromOriginalToStrippedDownUserGroup, getSecurityService()));
    }

    @Override
    public UserGroupDTO getUserGroupByName(String userGroupName) throws UnauthorizedException {
        final UserGroup userGroup = getSecurityService().getUserGroupByName(userGroupName);
        if (userGroup == null || SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.USER_GROUP.getStringPermissionForObjects(DefaultActions.READ, userGroup.getId().toString()))) {
            Map<User, StrippedUserDTO> fromOriginalToStrippedDownUser = new HashMap<>();
            Map<UserGroup, StrippedUserGroupDTO> fromOriginalToStrippedDownUserGroup = new HashMap<>();
            return securityDTOFactory.createUserGroupDTOFromUserGroup(userGroup, fromOriginalToStrippedDownUser,
                    fromOriginalToStrippedDownUserGroup, getSecurityService());
        } else {
            throw new UnauthorizedException("Not permitted to read user group "+userGroupName);
        }
    }

    @Override
    public StrippedUserGroupDTO getStrippedUserGroupByName(String userGroupName) throws UnauthorizedException {
        final UserGroup userGroup = getSecurityService().getUserGroupByName(userGroupName);
        if (userGroup == null || SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.USER_GROUP
                .getStringPermissionForObjects(DefaultActions.READ, userGroup.getId().toString()))) {
            Map<UserGroup, StrippedUserGroupDTO> fromOriginalToStrippedDownUserGroup = new HashMap<>();
            return securityDTOFactory.createStrippedUserGroupDTOFromUserGroup(userGroup,
                    fromOriginalToStrippedDownUserGroup);
        } else {
            throw new UnauthorizedException("Not permitted to read user group " + userGroupName);
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
    public UserGroupDTO createUserGroup(String name) throws UnauthorizedException, UserGroupManagementException {
        UserGroup group = getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                SecuredSecurityTypes.USER_GROUP, name, name, () -> {
                    UUID newTenantId = UUID.randomUUID();
                    UserGroup userGroup;
                    try {
                        userGroup = getSecurityService().createUserGroup(newTenantId, name);
                    } catch (UserGroupManagementException e) {
                        throw new UserGroupManagementException(e.getMessage());
                    }
                    getSecurityService().setOwnership(
                            SecuredSecurityTypes.USER_GROUP.getQualifiedObjectIdentifier(newTenantId.toString()),
                            getSecurityService().getCurrentUser(),
                            getSecurityService().getDefaultTenantForCurrentUser(), name);
                    return userGroup;
                });

        Map<User, StrippedUserDTO> fromOriginalToStrippedDownUser = new HashMap<>();
        Map<UserGroup, StrippedUserGroupDTO> fromOriginalToStrippedDownUserGroup = new HashMap<>();
        return securityDTOFactory.createUserGroupDTOFromUserGroup(group, fromOriginalToStrippedDownUser,
                fromOriginalToStrippedDownUserGroup, getSecurityService());
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
        getSecurityService().checkCurrentUserReadPermission(user);
        return new Pair<UserDTO, UserDTO>(securityDTOFactory.createUserDTOFromUser(user, getSecurityService()),
                getAllUser());
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
            String localeName, String validationBaseURL)
            throws UserManagementException, MailException, UnauthorizedException {
        
        User user = getSecurityService().checkPermissionForObjectCreationAndRevertOnErrorForUserCreation(username,
                new ActionWithResult<User>() {
            @Override
            public User run() throws Exception {
                try {
                    return getSecurityService().createSimpleUser(username, email, password, fullName, company,
                            getLocaleFromLocaleName(localeName), validationBaseURL);
                } catch (UserManagementException | UserGroupManagementException e) {
                    logger.log(Level.SEVERE, "Error creating user " + username, e);
                    throw new UserManagementException(e.getMessage());
                }
            }
                });
        return securityDTOFactory.createUserDTOFromUser(user, getSecurityService());
    }


    @Override
    public void updateSimpleUserPassword(final String username, String oldPassword, String passwordResetSecret, String newPassword) throws UserManagementException {
        getSecurityService().checkCurrentUserUpdatePermission(getSecurityService().getCurrentUser());
        if (// someone knew a username and the correct password for that user
        (oldPassword != null && getSecurityService().checkPassword(username, oldPassword))
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
    public UserDTO updateUserProperties(final String username, String fullName, String company, String localeName,
            String defaultTenant) throws UserManagementException {
        getSecurityService().checkCurrentUserUpdatePermission(getSecurityService().getCurrentUser());
        getSecurityService().updateUserProperties(username, fullName, company,
                getLocaleFromLocaleName(localeName));
        getSecurityService().setDefaultTenantForCurrentServerForUser(username, defaultTenant);
        return securityDTOFactory.createUserDTOFromUser(getSecurityService().getUserByName(username),
                getSecurityService());
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
        getSecurityService().checkCurrentUserUpdatePermission(getSecurityService().getCurrentUser());
        getSecurityService().updateSimpleUserEmail(username, newEmail, validationBaseURL);
    }
    
    @Override
    public void resetPassword(String username, String email, String passwordResetBaseURL)
            throws UserManagementException, MailException {
        getSecurityService().checkCurrentUserUpdatePermission(getSecurityService().getCurrentUser());
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
    public SuccessInfo setRolesForUser(String username,
            Iterable<Triple<UUID, String, String>> roleDefinitionIdAndTenantQualifierNameAndUsernames)
            throws UnauthorizedException {
        final boolean isUserPermittedToGrantPermissionsForOtherUser = SecurityUtils.getSubject().isPermitted(
                SecuredSecurityTypes.USER.getStringPermissionForObjects(UserActions.GRANT_PERMISSION, username));
        final boolean isUserPermittedToRevokePermissionsForOtherUser = SecurityUtils.getSubject().isPermitted(
                SecuredSecurityTypes.USER.getStringPermissionForObjects(UserActions.REVOKE_PERMISSION, username));
        User u = getSecurityService().getUserByName(username);
        if (u == null) {
            return new SuccessInfo(false, "User does not exist.", /* redirectURL */ null, null);
        }
        Set<Role> rolesToSet = new HashSet<>();
        for (final Triple<UUID, String, String> roleDefinitionIdAndTenantQualifierNameAndUsernameOfRoleToSet : roleDefinitionIdAndTenantQualifierNameAndUsernames) {
            final UserGroup tenant;
            if (roleDefinitionIdAndTenantQualifierNameAndUsernameOfRoleToSet.getB() == null
                    || roleDefinitionIdAndTenantQualifierNameAndUsernameOfRoleToSet.getB().trim().isEmpty()) {
                tenant = null;
            } else {
                tenant = getSecurityService()
                        .getUserGroupByName(roleDefinitionIdAndTenantQualifierNameAndUsernameOfRoleToSet.getB());
                if (tenant == null) {
                    return new SuccessInfo(false, "Tenant "
                            + roleDefinitionIdAndTenantQualifierNameAndUsernameOfRoleToSet.getB() + " does not exist.",
                            /* redirectURL */ null, /* userDTO */ null);
                }
            }
            try {
                rolesToSet.add(createRoleFromIDs(roleDefinitionIdAndTenantQualifierNameAndUsernameOfRoleToSet.getA(),
                        tenant == null ? null : tenant.getId(),
                        roleDefinitionIdAndTenantQualifierNameAndUsernameOfRoleToSet.getC()));
            } catch (UserManagementException e) {
                return new SuccessInfo(false, e.getMessage(), /* redirectURL */ null, /* userDTO */ null);
            }
        }
        Set<Role> roleDefinitionsToRemove = new HashSet<>();
        Util.addAll(u.getRoles(), roleDefinitionsToRemove);
        Util.removeAll(rolesToSet, roleDefinitionsToRemove);
        if (!roleDefinitionsToRemove.isEmpty() && !isUserPermittedToRevokePermissionsForOtherUser) {
            return new SuccessInfo(false, "Not permitted to revoke permissions for user " + username,
                    /* redirectURL */null, null);
        }
        Set<Role> rolesToAdd = new HashSet<>();
        Util.addAll(rolesToSet, rolesToAdd);
        Util.removeAll(u.getRoles(), rolesToAdd);
        if (!rolesToAdd.isEmpty() && !isUserPermittedToGrantPermissionsForOtherUser) {
            return new SuccessInfo(false, "Not permitted to grant permissions for user " + username,
                    /* redirectURL */null, null);
        }
        for (Role roleToAdd : rolesToAdd) {
            for (WildcardPermission permissionOfRoleToAdd : roleToAdd.getPermissions()) {
                if (!getSecurityService().hasCurrentUserMetaPermission(permissionOfRoleToAdd,
                        roleToAdd.getQualificationAsOwnership())) {
                    return new SuccessInfo(false,
                            "Not permitted to grant role " + roleToAdd.getName() + " for user " + username,
                            /* redirectURL */null, null);
                }
            }
        }
        for (Role roleToRemove : roleDefinitionsToRemove) {
            getSecurityService().removeRoleFromUser(u, roleToRemove);
        }
        for (Role roleToAdd : rolesToAdd) {
            getSecurityService().addRoleForUser(u, roleToAdd);
        }
        final String message = "Set roles " + roleDefinitionIdAndTenantQualifierNameAndUsernames + " for user "
                + username;
        logger.info(message);
        final UserDTO userDTO = securityDTOFactory.createUserDTOFromUser(u, getSecurityService());
        return new SuccessInfo(true, message, /* redirectURL */null, new Pair<UserDTO, UserDTO>(userDTO, getAllUser()));
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
        return new Role(
                getSecurityService().getRoleDefinition(roleDefinitionId),
                qualifyingTenantId == null ? null : getSecurityService().getUserGroup(qualifyingTenantId), user);
    }

    @Override
    public SuccessInfo setPermissionsForUser(String username, Iterable<WildcardPermission> permissions) throws UnauthorizedException {
        final boolean isUserPermittedToGrantPermissionsForOtherUser = SecurityUtils.getSubject().isPermitted(
                SecuredSecurityTypes.USER.getStringPermissionForObjects(UserActions.GRANT_PERMISSION, username));
        final boolean isUserPermittedToRevokePermissionsForOtherUser = SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.USER
                .getStringPermissionForObjects(UserActions.REVOKE_PERMISSION, username));
        if (!isUserPermittedToGrantPermissionsForOtherUser
                && !isUserPermittedToRevokePermissionsForOtherUser) {
            return new SuccessInfo(false, "Not permitted to grant or revoke permissions for user " + username,
                    /* redirectURL */null, null);
        } else {
            User u = getSecurityService().getUserByName(username);
            if (u == null) {
                return new SuccessInfo(false, "User does not exist.", /* redirectURL */null, null);
            }
            Set<WildcardPermission> permissionsToRemove = new HashSet<>();
            Util.addAll(u.getPermissions(), permissionsToRemove);
            Util.removeAll(permissions, permissionsToRemove);
            if (!permissionsToRemove.isEmpty() && !isUserPermittedToRevokePermissionsForOtherUser) {
                return new SuccessInfo(false, "Not permitted to revoke permissions for user " + username,
                        /* redirectURL */null, null);
            }
            Set<WildcardPermission> permissionsToAdd = new HashSet<>();
            Util.addAll(permissions, permissionsToAdd);
            Util.removeAll(u.getPermissions(), permissionsToAdd);
            if (!permissionsToAdd.isEmpty() && !isUserPermittedToGrantPermissionsForOtherUser) {
                return new SuccessInfo(false, "Not permitted to grant permissions for user " + username,
                        /* redirectURL */null, null);
            }
            for (WildcardPermission permissionToAdd : permissionsToAdd) {
                if (!getSecurityService().hasCurrentUserMetaPermission(permissionToAdd, null)) {
                    return new SuccessInfo(false,
                            "Not permitted to grant permission " + permissionToAdd + " for user " + username,
                            /* redirectURL */null, null);
                }
            }
            for (WildcardPermission permissionToRemove : permissionsToRemove) {
                getSecurityService().removePermissionFromUser(username, permissionToRemove);
            }
            for (WildcardPermission permissionToAdd : permissionsToAdd) {
                getSecurityService().addPermissionForUser(username, permissionToAdd);
            }
            final String message = "Set roles " + permissions + " for user " + username;
            final UserDTO userDTO = securityDTOFactory.createUserDTOFromUser(u, getSecurityService());
            return new SuccessInfo(true, message, /* redirectURL */null,
                    new Pair<UserDTO, UserDTO>(userDTO, getAllUser()));
        }
    }

    @Override
    public SuccessInfo deleteUser(String username) throws UnauthorizedException {
        // In addition to checking the default delete permission we currently explicitly require admin role for the owning user/group
        if (!getSecurityService().hasCurrentUserRoleForOwnedObject(SecuredSecurityTypes.USER, username,
                AdminRole.getInstance())) {
            return new SuccessInfo(false, "You are not permitted to delete user " + username, /* redirectURL */ null,
                    null);
        }
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
    public Set<SuccessInfo> deleteUsers(Set<String> usernames) throws UnauthorizedException {
        final Set<SuccessInfo> result = new HashSet<>();
        for (String username : usernames) {
            result.add(deleteUser(username));
        }
        return result;
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
        getSecurityService().checkCurrentUserUpdatePermission(getSecurityService().getUserByName(username));
            getSecurityService().setPreference(username, key, value);
    }
    
    @Override
    public void setPreferences(String username, Map<String, String> keyValuePairs)
            throws UserManagementException, UnauthorizedException {
        getSecurityService().checkCurrentUserUpdatePermission(getSecurityService().getUserByName(username));
        for (Entry<String, String> entry : keyValuePairs.entrySet()) {
            getSecurityService().setPreference(username, entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void unsetPreference(String username, String key) throws UserManagementException, UnauthorizedException {
        getSecurityService().checkCurrentUserUpdatePermission(getSecurityService().getUserByName(username));
        getSecurityService().unsetPreference(username, key);
    }

    @Override
    public String getPreference(String username, String key) throws UserManagementException, UnauthorizedException {
        getSecurityService().checkCurrentUserReadPermission(getSecurityService().getUserByName(username));
        return getSecurityService().getPreference(username, key);
    }
    
    @Override
    public Map<String, String> getPreferences(String username, List<String> keys) throws UserManagementException, UnauthorizedException {
        getSecurityService().checkCurrentUserReadPermission(getSecurityService().getUserByName(username));
        Map<String, String> requestedPreferences = new HashMap<>();
        for (String key : keys) {
            requestedPreferences.put(key, getSecurityService().getPreference(username, key));
        }
        return requestedPreferences;
    }
    
    @Override
    public Map<String, String> getAllPreferences(String username)
            throws UserManagementException, UnauthorizedException {
        getSecurityService().checkCurrentUserReadPermission(getSecurityService().getUserByName(username));
        final Map<String, String> allPreferences = getSecurityService().getAllPreferences(username);
        final Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : allPreferences.entrySet()) {
            if (!entry.getKey().startsWith("_")) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    @Override
    public String getAccessToken(String username) {
        return getSecurityService().getAccessToken(username);
    }

    @Override
    public String getOrCreateAccessToken(String username) {
        return getSecurityService().getOrCreateAccessToken(username);
    }

    @Override
    public AccessControlListDTO overrideAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject,
            AccessControlListDTO acl) throws UnauthorizedException {
        if (SecurityUtils.getSubject()
                .isPermitted(idOfAccessControlledObject.getStringPermission(DefaultActions.CHANGE_ACL))) {
            
            Map<UserGroup, Set<String>> aclActionsByGroup = new HashMap<>();
            for (Entry<StrippedUserGroupDTO, Set<String>> entry : acl.getActionsByUserGroup().entrySet()) {
                final StrippedUserGroupDTO groupDTO = entry.getKey();
                final UserGroup userGroup;
                if (groupDTO == null) {
                    userGroup = null;
                } else {
                    userGroup = getSecurityService().getUserGroup(groupDTO.getId());
                }
                aclActionsByGroup.put(userGroup, entry.getValue());
            }

            return securityDTOFactory.createAccessControlListDTO(getSecurityService()
                    .overrideAccessControlList(idOfAccessControlledObject, aclActionsByGroup));
        } else {
            throw new UnauthorizedException("Not permitted to update the ACL for a user");
        }
    }

    @Override
    public AccessControlListDTO getAccessControlListWithoutPruning(QualifiedObjectIdentifier idOfAccessControlledObject) throws UnauthorizedException {
        if (SecurityUtils.getSubject()
                .isPermitted(idOfAccessControlledObject.getStringPermission(DefaultActions.CHANGE_ACL))) {
            AccessControlListAnnotation accessControlList = getSecurityService().getAccessControlList(idOfAccessControlledObject);
            if (accessControlList == null) {
                return null;
            }
            return securityDTOFactory.createAccessControlListDTO(
                    accessControlList.getAnnotation());
        } else {
            throw new UnauthorizedException("Not permitted to get the unpruned ACL for a user");
        }
    }

}
