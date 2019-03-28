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
import com.sap.sse.ServerInfo;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.gwt.client.ServerInfoDTO;
import com.sap.sse.security.Action;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.interfaces.Credential;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.UnauthorizedException;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.dto.AccessControlListAnnotationDTO;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipAnnotationDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.RoleDefinitionDTO;
import com.sap.sse.security.shared.dto.RolesAndPermissionsForUserDTO;
import com.sap.sse.security.shared.dto.StrippedUserDTO;
import com.sap.sse.security.shared.dto.StrippedUserGroupDTO;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.shared.dto.WildcardPermissionWithSecurityDTO;
import com.sap.sse.security.shared.impl.Ownership;
import com.sap.sse.security.shared.impl.PermissionAndRoleAssociation;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.UserActions;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;
import com.sap.sse.security.shared.impl.UserGroupImpl;
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
    
    private ServerInfoDTO getServerInfo() {
        ServerInfoDTO result = new ServerInfoDTO(ServerInfo.getName(), ServerInfo.getBuildVersion());
        SecurityDTOUtil.addSecurityInformation(getSecurityService(), result, result.getIdentifier());
        return result;
    }

    @Override
    public RoleDefinitionDTO createRoleDefinition(String roleDefinitionIdAsString, String name) {
        RoleDefinition role = getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                SecuredSecurityTypes.ROLE_DEFINITION, new TypeRelativeObjectIdentifier(roleDefinitionIdAsString), name,
                new Callable<RoleDefinition>() {

                    @Override
                    public RoleDefinition call() throws Exception {
                        return getSecurityService().createRoleDefinition(UUID.fromString(roleDefinitionIdAsString),
                                name);
                    }
                });
        return securityDTOFactory.createRoleDefinitionDTO(role, getSecurityService());
    }

    @Override
    public void deleteRoleDefinition(String roleIdAsString) {
        RoleDefinition role = getSecurityService().getRoleDefinition(UUID.fromString(roleIdAsString));
        if (role != null) {
            getSecurityService().checkPermissionAndDeleteOwnershipForObjectRemoval(role, new Action() {
                @Override
                public void run() throws Exception {
                    for (User user : getSecurityService().getUserList()) {
                        HashSet<Role> nonConcurrentModificationCopy = new HashSet<>();
                        Util.addAll(user.getRoles(), nonConcurrentModificationCopy);
                        for (Role roleInstance : nonConcurrentModificationCopy) {
                            if (roleInstance.getRoleDefinition().equals(role)) {
                                TypeRelativeObjectIdentifier associationTypeIdentifier = PermissionAndRoleAssociation.get(roleInstance, user);
                                QualifiedObjectIdentifier qualifiedTypeIdentifier = SecuredSecurityTypes.ROLE_ASSOCIATION
                                        .getQualifiedObjectIdentifier(associationTypeIdentifier);
                                getSecurityService().deleteAllDataForRemovedObject(qualifiedTypeIdentifier);
                                getSecurityService().removeRoleFromUser(user, roleInstance);
                            }
                        }
                    }
                    for (UserGroup group : getSecurityService().getUserGroupList()) {
                        if (group.isRoleAssociated(role)) {
                            getSecurityService().removeRoleDefintionFromUserGroup(group, role);
                        }
                    }
                    getSecurityService().deleteRoleDefinition(role);
                }
            });
        }
    }

    @Override
    public void updateRoleDefinition(RoleDefinitionDTO roleDefinitionWithNewProperties) throws UnauthorizedException {
        SecurityUtils.getSubject().checkPermission(SecuredSecurityTypes.ROLE_DEFINITION.getStringPermissionForObject(
                DefaultActions.UPDATE, roleDefinitionWithNewProperties));
        
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
    public OwnershipDTO setOwnership(String username, UUID userGroupId,
            QualifiedObjectIdentifier idOfOwnedObject,
            String displayNameOfOwnedObject) {

        SecurityUtils.getSubject()
                .checkPermission(idOfOwnedObject.getStringPermission(DefaultActions.CHANGE_OWNERSHIP));

        final User user = getSecurityService().getUserByName(username);
        // no security check if current user can see the user associated with the given username

        final Ownership result = getSecurityService().setOwnership(idOfOwnedObject, user,
                getSecurityService().getUserGroup(userGroupId));
        return securityDTOFactory.createOwnershipDTO(result, new HashMap<>(), new HashMap<>());
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
                getSecurityService().getUserGroupList(),
                ug -> securityDTOFactory.createUserGroupDTOFromUserGroup(ug, fromOriginalToStrippedDownUser,
                        fromOriginalToStrippedDownUserGroup, getSecurityService()));
    }

    @Override
    public UserGroupDTO getUserGroupByName(String userGroupName) throws UnauthorizedException {
        final UserGroup userGroup = getSecurityService().getUserGroupByName(userGroupName);
        if (userGroup == null || SecurityUtils.getSubject().isPermitted(
                SecuredSecurityTypes.USER_GROUP.getStringPermissionForObject(DefaultActions.READ, userGroup))) {
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
                .getStringPermissionForObject(DefaultActions.READ, userGroup))) {
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
        if (user == null
                || SecurityUtils.getSubject()
                        .isPermitted(SecuredSecurityTypes.USER.getStringPermissionForObject(DefaultActions.READ, user))
                || SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.USER
                        .getStringPermissionForObject(SecuredSecurityTypes.PublicReadableActions.READ_PUBLIC, user))) {
            // TODO: pruning when current user only has READ_PUBLIC permission
            return user == null ? null : securityDTOFactory.createUserDTOFromUser(user, getSecurityService());
        } else {
            throw new UnauthorizedException("Not permitted to read user " + username);
        }
    }

    @Override
    public UserGroupDTO createUserGroup(String name) throws UnauthorizedException, UserGroupManagementException {
        UUID newTenantId = UUID.randomUUID();
        final UserGroup userGroupByName = getSecurityService().getUserGroupByName(name);
        if (userGroupByName != null) {
            throw new UserGroupManagementException(
                    String.format("A user group with the name '%s' already exists.", name));
        }

        UserGroup group = getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                SecuredSecurityTypes.USER_GROUP, UserGroupImpl.getTypeRelativeObjectIdentifier(newTenantId), name, () -> {
                    
                    UserGroup userGroup;
                    try {
                        userGroup = getSecurityService().createUserGroup(newTenantId, name);
                    } catch (UserGroupManagementException e) {
                        throw new UserGroupManagementException(e.getMessage());
                    }
                    return userGroup;
                });

        Map<User, StrippedUserDTO> fromOriginalToStrippedDownUser = new HashMap<>();
        Map<UserGroup, StrippedUserGroupDTO> fromOriginalToStrippedDownUserGroup = new HashMap<>();
        return securityDTOFactory.createUserGroupDTOFromUserGroup(group, fromOriginalToStrippedDownUser,
                fromOriginalToStrippedDownUserGroup, getSecurityService());
    }

    @Override
    public SuccessInfo deleteUserGroup(String userGroupIdAsString) throws UnauthorizedException {
        final UUID userGroupId = UUID.fromString(userGroupIdAsString);
        final UserGroup userGroup = getSecurityService().getUserGroup(userGroupId);
        if (userGroup != null) {
            return getSecurityService().checkPermissionAndDeleteOwnershipForObjectRemoval(userGroup, () -> {
                try {
                    getSecurityService().deleteUserGroup(userGroup);
                    return new SuccessInfo(true, "Deleted user group: " + userGroup.getName() + ".",
                            /* redirectURL */ null, null);
                } catch (UserGroupManagementException e) {
                    return new SuccessInfo(false, "Could not delete user group.", /* redirectURL */ null, null);
                }
            });
        } else {
            return new SuccessInfo(false, "Could not delete user group.", /* redirectURL */ null, null);
        }
    }

    @Override
    public void addUserToUserGroup(String userGroupIdAsString, String username)
            throws UnauthorizedException, UserManagementException {
        final UserGroup tenant = getSecurityService().getUserGroup(UUID.fromString(userGroupIdAsString));
        if (SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.USER_GROUP.getStringPermissionForObject(DefaultActions.UPDATE, tenant))) {
            final User userByName = getSecurityService().getUserByName(username);
            if (userByName == null) {
                throw new UserManagementException("user '" + username + "' not found.");
            }
            getSecurityService().addUserToUserGroup(tenant, userByName);
        } else {
            throw new UnauthorizedException("Not permitted to add user to group");
        }
    }

    @Override
    public void removeUserFromUserGroup(String userGroupIdAsString, String username) throws UnauthorizedException {
        final UserGroup userGroup = getSecurityService().getUserGroup(UUID.fromString(userGroupIdAsString));
        if (SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.USER_GROUP.getStringPermissionForObject(DefaultActions.DELETE, userGroup))) {
            getSecurityService().removeUserFromUserGroup(userGroup, getSecurityService().getUserByName(username));
        } else {
            throw new UnauthorizedException("Not permitted to remove user from group");
        }
    }

    @Override
    public void putRoleDefintionToUserGroup(String userGroupIdAsString, String roleDefinitionIdAsString,
            boolean forAll) throws UnauthorizedException {
        final UserGroup userGroup = getSecurityService().getUserGroup(UUID.fromString(userGroupIdAsString));
        if (SecurityUtils.getSubject().isPermitted(
                SecuredSecurityTypes.USER_GROUP.getStringPermissionForObject(DefaultActions.UPDATE, userGroup))) {
            final RoleDefinition roleDefinition = getSecurityService()
                    .getRoleDefinition(UUID.fromString(roleDefinitionIdAsString));
            if (roleDefinition != null) {
                if (!getSecurityService().hasCurrentUserMetaPermissionsOfRoleDefinitionWithQualification(roleDefinition,
                        new Ownership(null, userGroup))) {
                    throw new UnauthorizedException("Not permitted to add role definition to group");
                }
                getSecurityService().putRoleDefinitionToUserGroup(userGroup, roleDefinition, forAll);
            }
        } else {
            throw new UnauthorizedException("Not permitted to add role definition to group");
        }
    }

    @Override
    public void removeRoleDefintionFromUserGroup(String userGroupIdAsString, String roleDefinitionIdAsString)
            throws UnauthorizedException {
        final UserGroup userGroup = getSecurityService().getUserGroup(UUID.fromString(userGroupIdAsString));
        if (SecurityUtils.getSubject().isPermitted(
                SecuredSecurityTypes.USER_GROUP.getStringPermissionForObject(DefaultActions.DELETE, userGroup))) {
            final RoleDefinition roleDefinition = getSecurityService()
                    .getRoleDefinition(UUID.fromString(roleDefinitionIdAsString));
            if (roleDefinition != null) {
                if (!getSecurityService().hasCurrentUserMetaPermissionsOfRoleDefinitionWithQualification(roleDefinition,
                        new Ownership(null, userGroup))) {
                    throw new UnauthorizedException("Not permitted to remove role definition from group");
                }
                getSecurityService().removeRoleDefintionFromUserGroup(userGroup, roleDefinition);
            }
        } else {
            throw new UnauthorizedException("Not permitted to remove role definition from group");
        }
    }

    @Override
    public Collection<UserDTO> getUserList() throws UnauthorizedException {
        List<UserDTO> users = new ArrayList<>();
        for (User u : getSecurityService().getUserList()) {
            if (SecurityUtils.getSubject()
                    .isPermitted(SecuredSecurityTypes.USER.getStringPermissionForObject(DefaultActions.READ, u))
                    || SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.USER
                            .getStringPermissionForObject(SecuredSecurityTypes.PublicReadableActions.READ_PUBLIC, u))) {
                // TODO: pruning if subject only has READ_PUBLIC permission
                final UserDTO userDTO = getUserDTOWithFilteredRolesAndPermissions(u);
                users.add(userDTO);
            }
        }
        return users;
    }

    @Override
    public Triple<UserDTO, UserDTO, ServerInfoDTO> getCurrentUser() throws UnauthorizedException {
        logger.fine("Request: " + getThreadLocalRequest().getRequestURL());
        User user = getSecurityService().getCurrentUser();
        if (user == null) {
            return new Triple<>(null, getAllUser(), getServerInfo());
        }
        getSecurityService().checkCurrentUserReadPermission(user);
        return new Triple<>(securityDTOFactory.createUserDTOFromUser(user, getSecurityService()),
                getAllUser(), getServerInfo());
    }

    @Override
    public SuccessInfo login(String username, String password) {
        try {
            String redirectURL = getSecurityService().login(username, password);
            UserDTO user = securityDTOFactory.createUserDTOFromUser(getSecurityService().getUserByName(username),
                    getSecurityService());
            return new SuccessInfo(true, "Success. Redirecting to " + redirectURL, redirectURL,
                    new Triple<>(user, getAllUser(), getServerInfo()));
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
                new Callable<User>() {
                    @Override
                    public User call() throws Exception {
                        try {
                            User newUser = getSecurityService().createSimpleUser(username, email, password, fullName,
                                    company,
                                    getLocaleFromLocaleName(localeName), validationBaseURL,
                                    getSecurityService().getDefaultTenantForCurrentUser());
                            return newUser;
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
        final User user = getSecurityService().getUserByName(username);
        getSecurityService().checkCurrentUserUpdatePermission(user);
        if (getSecurityService().hasCurrentUserOneOfExplicitPermissions(user, UserActions.FORCE_OVERWRITE_PASSWORD)) {
            // e.g. admin is allowed to update the password without knowing the old password and/or secret
            getSecurityService().updateSimpleUserPassword(username, newPassword);
            sendPasswordChangedMailAsync(username);
        } else if (// someone knew a username and the correct password for that user
        (oldPassword != null && getSecurityService().checkPassword(username, oldPassword))
            // someone provided the correct password reset secret for the correct username
         || (passwordResetSecret != null && getSecurityService().checkPasswordResetSecret(username, passwordResetSecret))) {
            getSecurityService().updateSimpleUserPassword(username, newPassword);
            sendPasswordChangedMailAsync(username);
        } else {
            throw new UserManagementException(UserManagementException.INVALID_CREDENTIALS);
        }
    }

    private void sendPasswordChangedMailAsync(final String username) {
        new Thread("sending updated password to user "+username+" by e-mail") {
            @Override public void run() {
                try {
                    getSecurityService().sendMail(username, "Password Changed", "Somebody changed your password for your user named "+username+".\nIf that wasn't you, please contact sailing_analytics@sap.com via email.");
                } catch (MailException e) {
                    logger.log(Level.SEVERE, "Error sending new password to user "+username+" by e-mail", e);
                }
            }
        }.start();
    }

    @Override
    public UserDTO updateUserProperties(final String username, String fullName, String company, String localeName,
            String defaultTenant) throws UserManagementException {
        getSecurityService().checkCurrentUserUpdatePermission(getSecurityService().getCurrentUser());
        getSecurityService().updateUserProperties(username, fullName, company,
                getLocaleFromLocaleName(localeName));
        getSecurityService().setDefaultTenantForCurrentServerForUser(username, UUID.fromString(defaultTenant));
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
    public SuccessInfo deleteUser(String username) throws UnauthorizedException {
        User user = getSecurityService().getUserByName(username);
        if (user != null) {
            if (!getSecurityService().hasCurrentUserExplicitPermissions(user, DefaultActions.DELETE)) {
                return new SuccessInfo(false, "You are not permitted to delete user " + username,
                        /* redirectURL */ null, null);
            }
            try {
                return getSecurityService().checkPermissionAndDeleteOwnershipForObjectRemoval(user, () -> {
                    try {
                        getSecurityService().deleteUser(username);
                        return new SuccessInfo(true, "Deleted user: " + username + ".", /* redirectURL */ null, null);
                    } catch (UserManagementException e) {
                        return new SuccessInfo(false, "Could not delete user.", /* redirectURL */ null, null);
                    }
                });
            } catch (AuthorizationException e) {
                return new SuccessInfo(false, "You are not permitted to delete user " + username,
                        /* redirectURL */ null, null);
            }
        } else {
            return new SuccessInfo(false, "Could not delete user.", /* redirectURL */ null, null);
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
    public Triple<UserDTO, UserDTO, ServerInfoDTO> verifySocialUser(CredentialDTO credentialDTO) {
        User user = null;
        try {
            user = getSecurityService().verifySocialUser(createCredentialFromDTO(credentialDTO));
        } catch (UserManagementException e) {
            e.printStackTrace();
        }
        final UserDTO userDTO = securityDTOFactory.createUserDTOFromUser(user, getSecurityService());
        return new Triple<>(userDTO, getAllUser(), getServerInfo());
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
    public String getAccessToken(String username) throws UnauthorizedException {
        getSecurityService().checkCurrentUserReadPermission(getUserByName(username));
        return getSecurityService().getAccessToken(username);
    }

    @Override
    public String getOrCreateAccessToken(String username) throws UnauthorizedException {
        getSecurityService().checkCurrentUserUpdatePermission(getUserByName(username));
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

    @Override
    public SuccessInfo addRoleToUser(String username, String userQualifierName, UUID roleDefinitionId,
            String tenantQualifierName) throws UserManagementException, UnauthorizedException {

        SuccessInfo successInfo;
        try {
            // get user for which to add a role
            final User user = getOrThrowUser(username);

            // get user for which the role is qualified, if one exists
            getOrThrowQualifiedUser(userQualifierName);

            // get the group tenant the role is qualified for if one exists
            final UserGroup tenant = getOrThrowTenant(tenantQualifierName);

            final Role role = getOrThrowRoleFromIDs(roleDefinitionId, tenant == null ? null : tenant.getId(),
                    userQualifierName);

            final TypeRelativeObjectIdentifier associationTypeIdentifier = PermissionAndRoleAssociation.get(role, user);

            final String message = "added role " + role.getName() + " for user " + username;
            getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                    SecuredSecurityTypes.ROLE_ASSOCIATION, associationTypeIdentifier,
                    associationTypeIdentifier.toString(), new Action() {

                        @Override
                        public void run() throws Exception {
                            getSecurityService().addRoleForUser(user, role);
                            logger.info(message);
                        }
                    });

            final UserDTO userDTO = securityDTOFactory.createUserDTOFromUser(user, getSecurityService());
            successInfo = new SuccessInfo(true, message, /* redirectURL */null,
                    new Triple<>(userDTO, getAllUser(), getServerInfo()));
        } catch (UserManagementException e) {
            successInfo = new SuccessInfo(false,
                    "You are not allowed to grant this role for user " + username
                            + " or the username, group name or role name did not exist.",
                    /* redirectURL */ null, /* userDTO */ null);
        }
        return successInfo;
    }

    @Override
    public SuccessInfo removeRoleFromUser(String username, String userQualifierName, UUID roleDefinitionId,
            String tenantQualifierName) throws UserManagementException, UnauthorizedException {

        SuccessInfo successInfo;
        try {
            // get user for which to remove role
            final User user = getOrThrowUser(username);

            // get user for which the role is qualified, if one exists
            getOrThrowQualifiedUser(userQualifierName);

            // get the group tenant the role is qualified for if one exists
            UserGroup tenant = getOrThrowTenant(tenantQualifierName);

            Role role = getOrThrowRoleFromIDs(roleDefinitionId, tenant == null ? null : tenant.getId(),
                    userQualifierName);

            final String message = "removed role " + role.getName() + " for user " + username;
            final TypeRelativeObjectIdentifier associationTypeIdentifier = PermissionAndRoleAssociation.get(role, user);
            final QualifiedObjectIdentifier qualifiedTypeIdentifier = SecuredSecurityTypes.ROLE_ASSOCIATION
                    .getQualifiedObjectIdentifier(associationTypeIdentifier);
            getSecurityService().checkPermissionAndDeleteOwnershipForObjectRemoval(qualifiedTypeIdentifier,
                    new Callable<Void>() {

                        @Override
                        public Void call() throws Exception {
                            getSecurityService().removeRoleFromUser(user, role);
                            logger.info(message);
                            return null;
                        }
                    });

            final UserDTO userDTO = securityDTOFactory.createUserDTOFromUser(user, getSecurityService());
            successInfo = new SuccessInfo(true, message, /* redirectURL */null,
                    new Triple<>(userDTO, getAllUser(), getServerInfo()));
        } catch (UserManagementException e) {
            successInfo = new SuccessInfo(false,
                    "You are not allowed to revoke this role from user " + username
                            + " or the username, grou name or role name did not exist.",
                    /* redirectURL */ null, /* userDTO */ null);
        }
        return successInfo;
    }

    /**
     * @returns the user associated with the userQualifierName or null
     * @throws UserManagementException
     *             if the userQualifierName is not empty or null but no user was found.
     */
    private User getOrThrowQualifiedUser(String userQualifierName) throws UserManagementException {
        User user = getSecurityService().getUserByName(userQualifierName);
        if (userQualifierName != null && !userQualifierName.isEmpty() && user == null) {
            throw new UserManagementException("User " + userQualifierName + " not found.");
        }
        return user;
    }

    /**
     * @return the role associated with the given IDs and qualifiers
     * @throws UserManagementException
     *             if the current user does not have the meta permission to give this specific, qualified role in this
     *             context.
     */
    private Role getOrThrowRoleFromIDs(UUID roleDefinitionId, UUID tenantId, String userQualifierName)
            throws UserManagementException {
        final Role role = createRoleFromIDs(roleDefinitionId, tenantId, userQualifierName);
        if (!getSecurityService().hasCurrentUserMetaPermissionsOfRoleDefinitionWithQualification(
                role.getRoleDefinition(), role.getQualificationAsOwnership())) {
            throw new UserManagementException("You are not allowed to take this role to the user.");
        }
        return role;
    }

    /**
     * @return the user group associated with the tenantQualifierName
     * @throws UserManagementException,
     *             if the tenantQualifierName was not empty or null but did not yield a valid user group
     */
    private UserGroup getOrThrowTenant(String tenantQualifierName) throws UserManagementException {
        final UserGroup tenant;
        if (tenantQualifierName == null || tenantQualifierName.trim().isEmpty()) {
            tenant = null;
        } else {
            tenant = getSecurityService().getUserGroupByName(tenantQualifierName);
            if (tenant == null) {
                throw new UserManagementException("Tenant not found: " + tenantQualifierName);
            }
        }
        return tenant;
    }
    /**
     * @return the User associated with the username or a {@link UserManagementException}, if the user is null
     */
    private User getOrThrowUser(String username) throws UserManagementException {
        final User user = getSecurityService().getUserByName(username);

        if (user == null) {
            throw new UserManagementException("user " + username + " not found.");
        }
        return user;
    }

    @Override
    public SuccessInfo addPermissionForUser(String username, WildcardPermission permission)
            throws UnauthorizedException {

        SuccessInfo successInfo;
        try {
            // check if user exists
            User user = getOrThrowUser(username);

            // check permissions
            if (!getSecurityService().hasCurrentUserMetaPermission(permission, null)) {
                throw new UnauthorizedException(
                        "Not permitted to grant/revoke permission " + permission + " for user " + user.getName());
            }

            // grant permission
            final TypeRelativeObjectIdentifier associationTypeIdentifier = PermissionAndRoleAssociation.get(permission,
                    user);
            final String message = "Added permission " + permission + " for user " + username;
            getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                    SecuredSecurityTypes.PERMISSION_ASSOCIATION, associationTypeIdentifier,
                    associationTypeIdentifier.toString(), new Action() {

                        @Override
                        public void run() throws Exception {
                            getSecurityService().addPermissionForUser(username, permission);
                            logger.info(message);
                        }
                    });

            final UserDTO userDTO = securityDTOFactory.createUserDTOFromUser(user, getSecurityService());
            successInfo = new SuccessInfo(true, message, /* redirectURL */null,
                    new Triple<>(userDTO, getAllUser(), getServerInfo()));
        } catch (UserManagementException | UnauthorizedException e) {
            successInfo = new SuccessInfo(false, "Not permitted to grant permission " + permission + " for user "
                    + username + " or the user or permission did not exist.", /* redirectURL */null, null);
        }
        return successInfo;
    }

    @Override
    public SuccessInfo removePermissionFromUser(String username, WildcardPermissionWithSecurityDTO permission)
            throws UnauthorizedException {

        SuccessInfo successInfo;
        try {
            // check if user exists
            User user = getOrThrowUser(username);

            // check permissions
            if (!getSecurityService().hasCurrentUserMetaPermission(permission, null)) {
                throw new UnauthorizedException(
                        "Not permitted to grant/revoke permission " + permission + " for user " + user.getName());
            }

            // revoke permission
            final String message = "Revoked permission " + permission + " for user " + username;
            final TypeRelativeObjectIdentifier associationTypeIdentifier = PermissionAndRoleAssociation.get(permission,
                    user);
            final QualifiedObjectIdentifier qualifiedTypeIdentifier = SecuredSecurityTypes.PERMISSION_ASSOCIATION
                    .getQualifiedObjectIdentifier(associationTypeIdentifier);

            getSecurityService().checkPermissionAndDeleteOwnershipForObjectRemoval(qualifiedTypeIdentifier,
                    new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            getSecurityService().removePermissionFromUser(username, permission);
                            logger.info(message);
                            return null;
                        }
                    });

            final UserDTO userDTO = securityDTOFactory.createUserDTOFromUser(user, getSecurityService());
            successInfo = new SuccessInfo(true, message, /* redirectURL */null,
                    new Triple<>(userDTO, getAllUser(), getServerInfo()));

        } catch (UserManagementException | UnauthorizedException e) {
            successInfo = new SuccessInfo(false, "Not permitted to revoke permission " + permission + " for user "
                    + username + " or the user or permission did not exist", /* redirectURL */null, null);

        }
        return successInfo;
    }

    @Override
    public TypeRelativeObjectIdentifier serializationDummy(TypeRelativeObjectIdentifier typeRelativeObjectIdentifier) {
        return null;
    }

    @Override
    public Boolean userExists(String username) {
        return getSecurityService().getUserByName(username) != null;
    }

    @Override
    public RolesAndPermissionsForUserDTO getRolesAndPermissionsForUser(String username) throws UserManagementException {
        final User user = getSecurityService().getUserByName(username);
        if (user == null) {
            throw new UserManagementException("User '" + username + "'not found.");
        }
        final UserDTO userDTO = getUserDTOWithFilteredRolesAndPermissions(user);
        Collection<WildcardPermissionWithSecurityDTO> permissions = new ArrayList<>();
        for (WildcardPermission p : userDTO.getPermissions()) {
            if (p instanceof WildcardPermissionWithSecurityDTO) {
                permissions.add((WildcardPermissionWithSecurityDTO) p);
            }
        }
        return new RolesAndPermissionsForUserDTO(userDTO.getRoles(), permissions);
    }

    /**
     * @return The UserDTO for the given {@link User user} with his permissions and roles filtered to those the current
     *         user can actually see.
     */
    private UserDTO getUserDTOWithFilteredRolesAndPermissions(final User user) {
        return securityDTOFactory.createUserDTOFromUser(user, getSecurityService(), permission -> {
            final TypeRelativeObjectIdentifier typeRelativeObjectIdentifier = PermissionAndRoleAssociation
                    .get(permission, user);
            return SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.PERMISSION_ASSOCIATION
                    .getStringPermissionForTypeRelativeIdentifier(DefaultActions.READ, typeRelativeObjectIdentifier));
        }, role -> {
            final TypeRelativeObjectIdentifier typeRelativeObjectIdentifier = PermissionAndRoleAssociation.get(role,
                    user);
            return SecurityUtils.getSubject().isPermitted(SecuredSecurityTypes.ROLE_ASSOCIATION
                    .getStringPermissionForTypeRelativeIdentifier(DefaultActions.READ, typeRelativeObjectIdentifier));
        });
    }
}
