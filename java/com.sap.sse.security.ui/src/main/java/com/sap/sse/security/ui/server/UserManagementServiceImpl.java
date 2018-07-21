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
import com.sap.sailing.domain.common.security.Permission;
import com.sap.sse.common.Util;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.security.Credential;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.Social;
import com.sap.sse.security.User;
import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.Account.AccountType;
import com.sap.sse.security.shared.DefaultRoles;
import com.sap.sse.security.shared.Permission.DefaultModes;
import com.sap.sse.security.shared.SocialUserAccount;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.UsernamePasswordAccount;
import com.sap.sse.security.ui.client.UserManagementService;
import com.sap.sse.security.ui.oauth.client.CredentialDTO;
import com.sap.sse.security.ui.oauth.client.SocialUserDTO;
import com.sap.sse.security.ui.oauth.shared.OAuthException;
import com.sap.sse.security.ui.shared.AccountDTO;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;
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

    @Override
    public Collection<UserDTO> getUserList() {
        List<UserDTO> users = new ArrayList<>();
        for (User u : getSecurityService().getUserList()) {
            UserDTO userDTO = createUserDTOFromUser(u);
            users.add(userDTO);
        }
        return users;
    }

    @Override
    public UserDTO getCurrentUser() {
        logger.fine("Request: " + getThreadLocalRequest().getRequestURL());
        User user = getSecurityService().getCurrentUser();
        if (user == null) {
            return null;
        }
        return createUserDTOFromUser(user);
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
    public UserDTO createSimpleUser(String name, String email, String password, String fullName, String company, String localeName, String validationBaseURL) throws UserManagementException, MailException {
        User u = null;
        try {
            u = getSecurityService().createSimpleUser(name, email, password, fullName, company, getLocaleFromLocaleName(localeName), validationBaseURL);
        } catch (UserManagementException e) {
            logger.log(Level.SEVERE, "Error creating user "+name, e);
            throw new UserManagementException(e.getMessage());
        }
        if (u == null) {
            return null;
        }
        return createUserDTOFromUser(u);
    }


    @Override
    public void updateSimpleUserPassword(final String username, String oldPassword, String passwordResetSecret, String newPassword) throws UserManagementException {
        final Subject subject = SecurityUtils.getSubject();
        // the signed-in subject has role ADMIN
        if (subject.hasRole(DefaultRoles.ADMIN.getRolename()) 
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
        // the signed-in subject has role ADMIN or is changing own user
        if (!subject.hasRole(DefaultRoles.ADMIN.getRolename()) && (subject.getPrincipal() == null
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
    public Collection<UserDTO> getFilteredSortedUserList(String filter) {
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
    }

    @Override
    public SuccessInfo setRolesForUser(String username, Iterable<String> roles) {
        Subject currentSubject = SecurityUtils.getSubject();
        if (currentSubject.hasRole(DefaultRoles.ADMIN.getRolename())) {
            User u = getSecurityService().getUserByName(username);
            if (u == null) {
                return new SuccessInfo(false, "User does not exist.", /* redirectURL */null, null);
            }
            Set<String> rolesToRemove = new HashSet<>();
            Util.addAll(u.getRoles(), rolesToRemove);
            Util.removeAll(roles, rolesToRemove);
            for (String roleToRemove : rolesToRemove) {
                getSecurityService().removeRoleFromUser(username, roleToRemove);
            }
            Set<String> rolesToAdd = new HashSet<>();
            Util.addAll(roles, rolesToAdd);
            Util.removeAll(u.getRoles(), rolesToAdd);
            for (String roleToAdd : rolesToAdd) {
                getSecurityService().addRoleForUser(username, roleToAdd);
            }
            return new SuccessInfo(true, "Set roles " + roles + " for user " + username, /* redirectURL */null,
                    createUserDTOFromUser(u));
        } else {
            return new SuccessInfo(false, "You don't have the required permissions to add a role.", /* redirectURL */ null, null);
        }
    }

    @Override
    public SuccessInfo setPermissionsForUser(String username, Iterable<String> permissions) {
        Subject currentSubject = SecurityUtils.getSubject();
        if (currentSubject.hasRole(DefaultRoles.ADMIN.getRolename()) || currentSubject.isPermitted(Permission.MANAGE_USERS.getStringPermissionForObjects(DefaultModes.UPDATE, username))) {
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
            return new SuccessInfo(false, "You don't have the required permissions to add a role.", /* redirectURL */ null, null);
        }
    }

    @Override
    public SuccessInfo deleteUser(String username) {
        try {
            getSecurityService().deleteUser(username);
            return new SuccessInfo(true, "Deleted user: " + username + ".", /* redirectURL */ null, null);
        } catch (UserManagementException e) {
            return new SuccessInfo(false, "Could not delete user.", /* redirectURL */ null, null);
        }
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
        userDTO = new UserDTO(user.getName(), user.getEmail(), user.getFullName(), user.getCompany(),
                user.getLocale() != null ? user.getLocale().toLanguageTag() : null, user.isEmailValidated(),
                accountDTOs, user.getRoles(), user.getPermissions());
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
    public void setPreference(String username, String key, String value) throws UserManagementException {
        try {
            getSecurityService().setPreference(username, key, value);
        } catch (AuthorizationException e) {
            throw new UserManagementException(UserManagementException.USER_DOESNT_HAVE_PERMISSION);
        }
    }
    
    @Override
    public void setPreferences(String username, Map<String, String> keyValuePairs) throws UserManagementException {
        try {
            for (Entry<String, String> entry : keyValuePairs.entrySet()) {
                getSecurityService().setPreference(username, entry.getKey(), entry.getValue());
            }
        } catch (AuthorizationException e) {
            throw new UserManagementException(UserManagementException.USER_DOESNT_HAVE_PERMISSION);
        }
    }

    @Override
    public void unsetPreference(String username, String key) throws UserManagementException {
        try {
            getSecurityService().unsetPreference(username, key);
        } catch (AuthorizationException e) {
            throw new UserManagementException(UserManagementException.USER_DOESNT_HAVE_PERMISSION);
        }
    }

    @Override
    public String getPreference(String username, String key) throws UserManagementException {
        try {
            return getSecurityService().getPreference(username, key);
        } catch (AuthorizationException e) {
            throw new UserManagementException(UserManagementException.USER_DOESNT_HAVE_PERMISSION);
        }
    }
    
    @Override
    public Map<String, String> getPreferences(String username, List<String> keys) throws UserManagementException {
        Map<String, String> requestedPreferences = new HashMap<>();
        for (String key : keys) {
            requestedPreferences.put(key, getPreference(username, key));
        }
        return requestedPreferences;
    }
    
    @Override
    public Map<String, String> getAllPreferences(String username) throws UserManagementException {
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
