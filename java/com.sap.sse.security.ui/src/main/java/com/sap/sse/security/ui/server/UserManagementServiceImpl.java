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
import org.apache.shiro.subject.Subject;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sse.common.Util;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.security.Credential;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.Social;
import com.sap.sse.security.Tenant;
import com.sap.sse.security.User;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.Account.AccountType;
import com.sap.sse.security.shared.Owner;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.SocialUserAccount;
import com.sap.sse.security.shared.TenantManagementException;
import com.sap.sse.security.shared.UnauthorizedException;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.UsernamePasswordAccount;
import com.sap.sse.security.ui.client.UserManagementService;
import com.sap.sse.security.ui.oauth.client.CredentialDTO;
import com.sap.sse.security.ui.oauth.client.SocialUserDTO;
import com.sap.sse.security.ui.oauth.shared.OAuthException;
import com.sap.sse.security.ui.shared.AccessControlListDTO;
import com.sap.sse.security.ui.shared.AccountDTO;
import com.sap.sse.security.ui.shared.OwnerDTO;
import com.sap.sse.security.ui.shared.RoleDTO;
import com.sap.sse.security.ui.shared.RolePermissionModelDTO;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.TenantDTO;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserGroupDTO;
import com.sap.sse.security.ui.shared.UsernamePasswordAccountDTO;

public class UserManagementServiceImpl extends RemoteServiceServlet implements UserManagementService {
    private static final long serialVersionUID = 4458564336368629101L;
    
    private static final Logger logger = Logger.getLogger(UserManagementServiceImpl.class.getName());

    private final BundleContext context;
    private final FutureTask<SecurityService> securityService;

    public UserManagementServiceImpl() {
        context = Activator.getContext();
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

    private UserGroupDTO createUserGroupDTOFromUserGroup(UserGroup userGroup) {
        AccessControlList acl = getSecurityService().getAccessControlList(userGroup.getId().toString());
        Owner ownership = getSecurityService().getOwnership(userGroup.getId().toString());
        return new UserGroupDTO((UUID) userGroup.getId(), userGroup.getName(),
                createAclDTOFromAcl(acl), createOwnershipDTOFromOwnership(ownership), userGroup.getUsernames());
    }

    private TenantDTO createTenantDTOFromTenant(Tenant tenant) {
        if (tenant == null) {
            return null;
        } else {
            AccessControlList acl = getSecurityService().getAccessControlList(tenant.getId().toString());
            Owner ownership = getSecurityService().getOwnership(tenant.getId().toString());
            return new TenantDTO((UUID) tenant.getId(), tenant.getName(),
                    createAclDTOFromAcl(acl), createOwnershipDTOFromOwnership(ownership), tenant.getUsernames());
        }
    }

    private AccessControlListDTO createAclDTOFromAcl(AccessControlList acl) {
        if (acl != null) {
            Map<UserGroupDTO, Set<String>> permissionMapDTO = new HashMap<>();
            for (Map.Entry<UUID, Set<String>> entry : acl.getPermissionMap().entrySet()) {
                UserGroup group = getSecurityService().getUserGroup(entry.getKey());
                permissionMapDTO.put(createUserGroupDTOFromUserGroup(group), 
                        entry.getValue());
            }
            return new AccessControlListDTO(acl.getId().toString(), acl.getDisplayName(), permissionMapDTO);
        } else {
            return null;
        }
    }

    private OwnerDTO createOwnershipDTOFromOwnership(Owner ownership) {
        if (ownership != null) {
            return new OwnerDTO(ownership.getId().toString(), ownership.getOwner(), ownership.getTenantOwner(), ownership.getDisplayName());
        } else {
            return null;
        }
    }

    @Override
    public Collection<AccessControlListDTO> getAccessControlListList() throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("manage_access_control")) {
            List<AccessControlListDTO> acls = new ArrayList<>();
            for (AccessControlList acl : getSecurityService().getAccessControlListList()) {
                AccessControlListDTO aclDTO = createAclDTOFromAcl(acl);
                acls.add(aclDTO);
            }
            return acls;
        } else {
            throw new UnauthorizedException("Not permitted to manage access control");
        }
    }

    @Override
    public AccessControlListDTO getAccessControlList(String idAsString) {
        return createAclDTOFromAcl(getSecurityService().getAccessControlList(idAsString));
    }

    @Override
    public AccessControlListDTO updateACL(String idAsString, Map<String, Set<String>> permissionStrings) throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("tenant:grant_permission,revoke_permission")) {
            Map<UserGroup, Set<String>> permissionMap = new HashMap<>();
            for (String group : permissionStrings.keySet()) {
                permissionMap.put(getSecurityService().getUserGroupByName(group), permissionStrings.get(group));
            }
            return createAclDTOFromAcl(getSecurityService().updateACL(idAsString, permissionMap));
        } else {
            throw new UnauthorizedException("Not permitted to grant and revoke permissions for user");
        }
    }

    @Override
    public AccessControlListDTO addToACL(String idAsString, String tenantIdAsString, String permission) throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("tenant:grant_permission:" + tenantIdAsString)) {
            UUID tenantId = UUID.fromString(tenantIdAsString);
            return createAclDTOFromAcl(getSecurityService().addToACL(idAsString, tenantId, permission));
        } else {
            throw new UnauthorizedException("Not permitted to grant permission for user");
        }
    }

    @Override
    public AccessControlListDTO removeFromACL(String idAsString, String tenantIdAsString, String permission) throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("tenant:revoke_permission:" + tenantIdAsString)) {
            UUID tenantId = UUID.fromString(tenantIdAsString);
            return createAclDTOFromAcl(getSecurityService().removeFromACL(idAsString, tenantId, permission));
        } else {
            throw new UnauthorizedException("Not permitted to revoke permission for user");
        }
    }

    @Override
    public Collection<TenantDTO> getTenantList() throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("manage_tenants")) {
            List<TenantDTO> tenants = new ArrayList<>();
            for (Tenant t : getSecurityService().getTenantList()) {
                TenantDTO tenantDTO = createTenantDTOFromTenant(t);
                tenants.add(tenantDTO);
            }
            return tenants;
        } else {
            throw new UnauthorizedException("Not permitted to manage tenants");
        }
    }

    @Override
    public TenantDTO createTenant(String name, String tenantOwner) throws TenantManagementException, UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("tenant:create")) {
            UUID id = UUID.randomUUID();
            Tenant tenant;
            try {
                tenant = getSecurityService().createTenant(id, name);
            } catch (UserGroupManagementException e) {
                throw new TenantManagementException(e.getMessage());
            }
            getSecurityService().createOwnership(id.toString(), (String) SecurityUtils.getSubject().getPrincipal(), (UUID) getSecurityService().getTenantByName(tenantOwner).getId(), name);
            return createTenantDTOFromTenant(tenant);
        } else {
            throw new UnauthorizedException("Not permitted to create tenants");
        }
    }

    @Override
    public UserGroupDTO addUserToTenant(String idAsString, String user) throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("tenant:add_user:" + idAsString)) {
            return createUserGroupDTOFromUserGroup(getSecurityService().addUserToUserGroup(UUID.fromString(idAsString), user));
        } else {
            throw new UnauthorizedException("Not permitted to add user to tenant");
        }
    }

    @Override
    public UserGroupDTO removeUserFromTenant(String idAsString, String user) throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("tenant:remove_user:" + idAsString)) {
            return createUserGroupDTOFromUserGroup(getSecurityService().removeUserFromUserGroup(UUID.fromString(idAsString), user));
        } else {
            throw new UnauthorizedException("Not permitted to remove user from tenant");
        }
    }

    @Override
    public SuccessInfo deleteTenant(String idAsString) throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("tenant:delete:" + idAsString)) {
            try {
                UUID id = UUID.fromString(idAsString);
                getSecurityService().deleteTenant(id);
                getSecurityService().deleteACL(id.toString());
                getSecurityService().deleteOwnership(id.toString());
                return new SuccessInfo(true, "Deleted tenant: " + idAsString + ".", /* redirectURL */ null, null);
            } catch (UserGroupManagementException e) {
                return new SuccessInfo(false, "Could not delete tenant.", /* redirectURL */ null, null);
            }
        } else {
            throw new UnauthorizedException("Not permitted to delete tenant");
        }
    }

    @Override
    public Collection<UserDTO> getUserList() throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("manage_users")) {
            List<UserDTO> users = new ArrayList<>();
            for (User u : getSecurityService().getUserList()) {
                UserDTO userDTO = createUserDTOFromUser(u);
                users.add(userDTO);
            }
            return users;
        } else {
            throw new UnauthorizedException("Not permitted to manage users");
        }
    }

    @Override
    public UserDTO getCurrentUser() throws UnauthorizedException {
        logger.fine("Request: " + getThreadLocalRequest().getRequestURL());
        User user = getSecurityService().getCurrentUser();
        if (user == null) {
            return null;
        }
        if (SecurityUtils.getSubject().isPermitted("user:view:" + user.getName())) {
            return createUserDTOFromUser(user);
        } else {
            throw new UnauthorizedException("Not permitted to view current user");
        }
    }

    @Override
    public SuccessInfo login(String username, String password) {
        try {
            String redirectURL = getSecurityService().login(username, password);
            return new SuccessInfo(true, "Success. Redirecting to "+redirectURL, redirectURL,
                    createUserDTOFromUser(getSecurityService().getUserByName(username)));
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

    @Override
    public UserDTO createSimpleUser(String name, String email, String password, String fullName, String company, String localeName, String validationBaseURL, String tenantOwner) throws UserManagementException, MailException, UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("user:create")) {
            User u = null;
            try {
                u = getSecurityService().createSimpleUser(name, email, password, fullName, company, getLocaleFromLocaleName(localeName), validationBaseURL);
                getSecurityService().createOwnership(name, (String) SecurityUtils.getSubject().getPrincipal(), (UUID) getSecurityService().getTenantByName(tenantOwner).getId());
            } catch (UserManagementException | UserGroupManagementException e) {
                logger.log(Level.SEVERE, "Error creating user "+name, e);
                throw new UserManagementException(e.getMessage());
            }
            if (u == null) {
                return null;
            }
            return createUserDTOFromUser(u);
        } else {
            throw new UnauthorizedException("Not permitted to create user");
        }
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

    private void ensureThatUserInQuestionIsLoggedInOrCurrentUserIsAdmin(String username) throws UserManagementException {
        final Subject subject = SecurityUtils.getSubject();
        // the signed-in subject has all permissions or is changing own user
        if (SecurityUtils.getSubject().isPermitted("*") &&
                (subject.getPrincipal() == null
                || !username.equals(subject.getPrincipal().toString()))) {
            throw new UserManagementException(UserManagementException.INVALID_CREDENTIALS);
        }
    }
    
    @Override
    public void updateUserProperties(final String username, String fullName, String company, String localeName) throws UserManagementException {
        ensureThatUserInQuestionIsLoggedInOrCurrentUserIsAdmin(username);
        getSecurityService().updateUserProperties(username, fullName, company,
                getLocaleFromLocaleName(localeName));
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
        ensureThatUserInQuestionIsLoggedInOrCurrentUserIsAdmin(username);
        getSecurityService().updateSimpleUserEmail(username, newEmail, validationBaseURL);
    }
    
    @Override
    public void resetPassword(String username, String email, String passwordResetBaseURL) throws UserManagementException, MailException {
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
        if (SecurityUtils.getSubject().isPermitted("manage_users")) {
            List<UserDTO> users = new ArrayList<>();
            for (User u : getSecurityService().getUserList()) {
                if (filter != null && !"".equals(filter)) {
                    if (u.getName().contains(filter)) {
                        users.add(createUserDTOFromUser(u));
                    }
                } else {
                    users.add(createUserDTOFromUser(u));
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
    public SuccessInfo setRolesForUser(String username, Iterable<UUID> roles) throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("user:grant_permission,revoke_permission:" + username)) {
            User u = getSecurityService().getUserByName(username);
            if (u == null) {
                return new SuccessInfo(false, "User does not exist.", /* redirectURL */null, null);
            }
            Set<UUID> rolesToRemove = new HashSet<>();
            Util.addAll(u.getRoles(), rolesToRemove);
            Util.removeAll(roles, rolesToRemove);
            for (UUID roleToRemove : rolesToRemove) {
                getSecurityService().removeRoleFromUser(username, roleToRemove);
            }
            Set<UUID> rolesToAdd = new HashSet<>();
            Util.addAll(roles, rolesToAdd);
            Util.removeAll(u.getRoles(), rolesToAdd);
            for (UUID roleToAdd : rolesToAdd) {
                getSecurityService().addRoleForUser(username, roleToAdd);
            }
            return new SuccessInfo(true, "Set roles " + roles + " for user " + username, /* redirectURL */null,
                    createUserDTOFromUser(u));
        } else {
            throw new UnauthorizedException("Not permitted to grant permissions to user");
        }
    }

    @Override
    public SuccessInfo setPermissionsForUser(String username, Iterable<String> permissions) throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("user:grant_permission,revoke_permission:" + username)) {
            User u = getSecurityService().getUserByName(username);
            if (u == null) {
                return new SuccessInfo(false, "User does not exist.", /* redirectURL */null, null);
            }
            Set<String> permissionsToRemove = new HashSet<>();
            Util.addAll(u.getPermissions(), permissionsToRemove);
            Util.removeAll(permissions, permissionsToRemove);
            for (String permissionToRemove : permissionsToRemove) {
                getSecurityService().removePermissionFromUser(username, permissionToRemove);
            }
            Set<String> permissionsToAdd = new HashSet<>();
            Util.addAll(permissions, permissionsToAdd);
            Util.removeAll(u.getPermissions(), permissionsToAdd);
            for (String permissionToAdd : permissionsToAdd) {
                getSecurityService().addPermissionForUser(username, permissionToAdd);
            }
            return new SuccessInfo(true, "Set roles " + permissions + " for user " + username, /* redirectURL */null,
                    createUserDTOFromUser(u));
        } else {
            throw new UnauthorizedException("Not permitted to grant or revoke permissions for user");
        }
    }

    @Override
    public SuccessInfo deleteUser(String username) throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted("user:delete:" + username)) {
        try {
            getSecurityService().deleteUser(username);
            getSecurityService().deleteACL(username);
            getSecurityService().deleteOwnership(username);
            return new SuccessInfo(true, "Deleted user: " + username + ".", /* redirectURL */ null, null);
        } catch (UserManagementException e) {
            return new SuccessInfo(false, "Could not delete user.", /* redirectURL */ null, null);
        }
        } else {
            throw new UnauthorizedException("Not permitted to delete user");
        }
    }

    private RoleDTO createRoleDTOFromRole(Role role) {
        HashSet<String> stringPermissions = new HashSet<>();
        for (com.sap.sse.security.shared.WildcardPermission wildcardPermission : role.getPermissions()) {
            stringPermissions.add(wildcardPermission.toString());
        }
        return new RoleDTO((UUID) role.getId(), role.getName(), stringPermissions);
    }

    private UserDTO createUserDTOFromUser(User user) {
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
        HashMap<UUID, RoleDTO> roleMap = new HashMap<>();
        for (Role role : getSecurityService().getRoles()) {
            roleMap.put((UUID) role.getId(), createRoleDTOFromRole(role));
        }
        TenantDTO defaultTenantDTO = createTenantDTOFromTenant(getSecurityService().getTenant(user.getDefaultTenant()));
        userDTO = new UserDTO(user.getName(), user.getEmail(), user.getFullName(), user.getCompany(),
                user.getLocale() != null ? user.getLocale().toLanguageTag() : null, user.isEmailValidated(),
                accountDTOs, user.getRoles(), new RolePermissionModelDTO(roleMap), defaultTenantDTO, user.getPermissions());
        return userDTO;
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
    public UserDTO verifySocialUser(CredentialDTO credentialDTO) {
        User user = null;
        try {
            user = getSecurityService().verifySocialUser(createCredentialFromDTO(credentialDTO));
        } catch (UserManagementException e) {
            e.printStackTrace();
        }
        return createUserDTOFromUser(user);
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
    
    private SocialUserDTO createSocialUserDTO(SocialUserAccount socialUser){
        SocialUserDTO socialUserDTO = new SocialUserDTO(socialUser.getProperty(Social.PROVIDER.name()));
        socialUserDTO.setSessionId(socialUser.getSessionId());
        
        for (Social s : Social.values()){
            socialUserDTO.setProperty(s.name(), socialUser.getProperty(s.name()));
        }
        return socialUserDTO;
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

    private SecurityService getSecurityService() {
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
