package com.sap.sse.security.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.mgt.CachingSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.apache.shiro.web.config.WebIniSecurityManagerFactory;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.osgi.util.tracker.ServiceTracker;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.builder.api.FlickrApi;
import org.scribe.builder.api.Foursquare2Api;
import org.scribe.builder.api.GoogleApi;
import org.scribe.builder.api.ImgUrApi;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.builder.api.LiveApi;
import org.scribe.builder.api.TumblrApi;
import org.scribe.builder.api.TwitterApi;
import org.scribe.builder.api.VimeoApi;
import org.scribe.builder.api.YahooApi;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import com.sap.sse.common.Util;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.mail.MailService;
import com.sap.sse.replication.OperationExecutionListener;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.OperationWithResultWithIdWrapper;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.security.BearerAuthenticationToken;
import com.sap.sse.security.ClientUtils;
import com.sap.sse.security.Credential;
import com.sap.sse.security.GithubApi;
import com.sap.sse.security.InstagramApi;
import com.sap.sse.security.OAuthRealm;
import com.sap.sse.security.OAuthToken;
import com.sap.sse.security.SessionCacheManager;
import com.sap.sse.security.SessionUtils;
import com.sap.sse.security.Social;
import com.sap.sse.security.SocialSettingsKeys;
import com.sap.sse.security.User;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.persistence.PersistenceFactory;
import com.sap.sse.security.shared.Account.AccountType;
import com.sap.sse.security.shared.DefaultRoles;
import com.sap.sse.security.shared.SocialUserAccount;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.UsernamePasswordAccount;
import com.sap.sse.util.ClearStateTestSupport;

public class SecurityServiceImpl implements ReplicableSecurityService, ClearStateTestSupport {
    private static final Logger logger = Logger.getLogger(SecurityServiceImpl.class.getName());

    private CachingSecurityManager securityManager;
    
    /**
     * A cache manager that the {@link SessionCacheManager} delegates to. This way, multiple Shiro configurations can
     * share the cache manager provided as a singleton within this bundle instance. The cache manager is replicating,
     * forwarding changes to the caches to all replicas registered.
     */
    private final ReplicatingCacheManager cacheManager;
    
    private UserStore store;
    private final ServiceTracker<MailService, MailService> mailServiceTracker;
    private final ConcurrentMap<OperationExecutionListener<ReplicableSecurityService>, OperationExecutionListener<ReplicableSecurityService>> operationExecutionListeners;

    /**
     * The master from which this replicable is currently replicating, or <code>null</code> if this replicable is not currently
     * replicated from any master.
     */
    private ReplicationMasterDescriptor replicatingFromMaster;
    
    private Set<OperationWithResultWithIdWrapper<?, ?>> operationsSentToMasterForReplication;
    
    private ThreadLocal<Boolean> currentlyFillingFromInitialLoad = ThreadLocal.withInitial(() -> false);
    
    private ThreadLocal<Boolean> currentlyApplyingOperationReceivedFromMaster = ThreadLocal.withInitial(() -> false);

    private static Ini shiroConfiguration;
    static {
        shiroConfiguration = new Ini();
        shiroConfiguration.loadFromPath("classpath:shiro.ini");
    }
    
    public SecurityServiceImpl(UserStore store) {
        this(null, store);
    }

    /**
     * @param mailProperties must not be <code>null</code>
     */
    public SecurityServiceImpl(ServiceTracker<MailService, MailService> mailServiceTracker, UserStore store) {
        this(mailServiceTracker, store, /* setAsActivatorTestSecurityService */ false);
    }
    
    /**
     * @param setAsActivatorSecurityService
     *            when <code>true</code>, the {@link Activator#setSecurityService(com.sap.sse.security.SecurityService)}
     *            will be called with this new instance as argument so that the cache manager can already be accessed
     *            when the security manager is created. {@link ReplicatingCacheManager#getCache(String)} fetches the
     *            activator's security service and passes it to the cache entries created. They need it, in turn, for
     *            replication.
     * 
     */
    public SecurityServiceImpl(ServiceTracker<MailService, MailService> mailServiceTracker, UserStore store, boolean setAsActivatorSecurityService) {
        logger.info("Initializing Security Service with user store " + store);
        if (setAsActivatorSecurityService) {
            Activator.setSecurityService(this);
        }
        operationsSentToMasterForReplication = new HashSet<>();
        this.operationExecutionListeners = new ConcurrentHashMap<>();
        this.store = store;
        this.mailServiceTracker = mailServiceTracker;
        // Create default users if no users exist yet.
        initEmptyStore();
        Factory<SecurityManager> factory = new WebIniSecurityManagerFactory(shiroConfiguration);
        cacheManager = loadReplicationCacheManagerContents();
        logger.info("Loaded shiro.ini file from: classpath:shiro.ini");
        StringBuilder logMessage = new StringBuilder("[urls] section from Shiro configuration:");
        final Section urlsSection = shiroConfiguration.getSection("urls");
        if (urlsSection != null) {
            for (Entry<String, String> e : urlsSection.entrySet()) {
                logMessage.append("\n");
                logMessage.append(e.getKey());
                logMessage.append(": ");
                logMessage.append(e.getValue());
            }
        }
        logger.info(logMessage.toString());
        System.setProperty("java.net.useSystemProxies", "true");
        CachingSecurityManager securityManager = (CachingSecurityManager) factory.getInstance();
        logger.info("Created: " + securityManager);
        SecurityUtils.setSecurityManager(securityManager);
        this.securityManager = securityManager;
    }

    private ReplicatingCacheManager loadReplicationCacheManagerContents() {
        logger.info("Loading session cache manager contents");
        int count = 0;
        final ReplicatingCacheManager result = new ReplicatingCacheManager();
        for (Entry<String, Set<Session>> cacheNameAndSessions : PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory().loadSessionsByCacheName().entrySet()) {
            final String cacheName = cacheNameAndSessions.getKey();
            final ReplicatingCache<Object, Object> cache = (ReplicatingCache<Object, Object>) result.getCache(cacheName);
            for (final Session session : cacheNameAndSessions.getValue()) {
                cache.put(session.getId(), session, /* store */ false);
                count++;
            }
        }
        logger.info("Loaded "+count+" sessions");
        return result;
    }

    @Override
    public boolean isCurrentlyFillingFromInitialLoad() {
        return currentlyFillingFromInitialLoad.get();
    }

    @Override
    public void setCurrentlyFillingFromInitialLoad(boolean currentlyFillingFromInitialLoad) {
        this.currentlyFillingFromInitialLoad.set(currentlyFillingFromInitialLoad);
    }

    @Override
    public boolean isCurrentlyApplyingOperationReceivedFromMaster() {
        return currentlyApplyingOperationReceivedFromMaster.get();
    }

    @Override
    public void setCurrentlyApplyingOperationReceivedFromMaster(boolean currentlyApplyingOperationReceivedFromMaster) {
        this.currentlyApplyingOperationReceivedFromMaster.set(currentlyApplyingOperationReceivedFromMaster);
    }

    /**
     * Creates a default "admin" user with initial password "admin" and initial role "admin" if the user <code>store</code>
     * is empty.
     */
    private void initEmptyStore() {
        if (!store.hasUsers()) {
            try {
                logger.info("No users found, creating default user \"admin\" with password \"admin\"");
                createSimpleUser("admin", "nobody@sapsailing.com", "admin", 
                        /* fullName */ null, /* company */ null, Locale.ENGLISH, /* validationBaseURL */ null);
                addRoleForUser("admin", DefaultRoles.ADMIN.getRolename());
            } catch (UserManagementException | MailException e) {
                logger.log(Level.SEVERE, "Exception while creating default admin user", e);
            }
        }
    }
    
    private MailService getMailService() {
        return mailServiceTracker == null ? null : mailServiceTracker.getService();
    }

    @Override
    public void sendMail(String username, String subject, String body) throws MailException {
        final User user = getUserByName(username);
        if (user != null) {
            final String toAddress = user.getEmail();
            if (toAddress != null) {
                MailService mailService = getMailService();
                if (mailService == null) {
                    logger.warning(String.format("Could not send mail to user %s: no MailService found", username));
                } else {
                    getMailService().sendMail(toAddress, subject, body);
                }
            }
        }
    }
    
    @Override
    public void resetPassword(final String username, String passwordResetBaseURL) throws UserManagementException, MailException {
        final User user = store.getUserByName(username);
        if (user == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        if (!user.isEmailValidated()) {
            throw new UserManagementException(UserManagementException.CANNOT_RESET_PASSWORD_WITHOUT_VALIDATED_EMAIL);
        }
        final String passwordResetSecret = user.startPasswordReset();
        apply(s->s.internalStoreUser(user)); // durably storing the password reset secret
        Map<String, String> urlParameters = new HashMap<>();
        try {
            urlParameters.put("u", URLEncoder.encode(user.getName(), "UTF-8"));
            urlParameters.put("e", URLEncoder.encode(user.getEmail(), "UTF-8"));
            urlParameters.put("s", URLEncoder.encode(passwordResetSecret, "UTF-8"));
            final StringBuilder url = buildURL(passwordResetBaseURL, urlParameters);
            new Thread("sending password reset e-mail to user " + username) {
                @Override
                public void run() {
                    try {
                        sendMail(user.getName(), "Password Reset",
                                "Please click on the link below to reset your password for user " + user.getName()
                                        + ".\n   " + url.toString());
                    } catch (MailException e) {
                        logger.log(Level.SEVERE, "Error sending mail for password reset of user " + user.getName()
                                + " to address " + user.getEmail(), e);
                    }
                }
            }.start();
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.SEVERE,
                    "Internal error: encoding UTF-8 not found. Couldn't send e-mail to user " + user.getName()
                            + " at e-mail address " + user.getEmail(), e);
        }
    }

    @Override
    public CachingSecurityManager getSecurityManager() {
        return this.securityManager;
    }

    @Override
    public Iterable<User> getUserList() {
        return store.getUsers();
    }

    @Override
    public String login(String username, String password) throws AuthenticationException {
        String redirectUrl;
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        logger.info("Trying to login: " + username);
        Subject subject = SecurityUtils.getSubject();
        subject.login(token);
        HttpServletRequest httpRequest = WebUtils.getHttpRequest(subject);
        SavedRequest savedRequest = WebUtils.getSavedRequest(httpRequest);
        if (savedRequest != null) {
            redirectUrl = savedRequest.getRequestUrl();
        } else {
            redirectUrl = "";
        }
        logger.info("Redirecturl: " + redirectUrl);
        return redirectUrl;
    }
    
    @Override
    public User loginByAccessToken(String accessToken) {
        BearerAuthenticationToken token = new BearerAuthenticationToken(accessToken);
        logger.info("Trying to login with access token");
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
            final String username = (String) token.getPrincipal();
            return store.getUserByName(username);
        } catch (AuthenticationException e) {
            logger.log(Level.INFO, "Authentication failed with access token "+accessToken);
            throw e;
        }
    }

    @Override
    public void logout() {
        Subject subject = SecurityUtils.getSubject();
        logger.info("Logging out");
        subject.logout();
    }

    @Override
    public User getUserByName(String name) {
        return store.getUserByName(name);
    }
    
    @Override
    public User getUserByAccessToken(String accessToken) {
        return store.getUserByAccessToken(accessToken);
    }

    @Override
    public User getUserByEmail(String email) {
        return store.getUserByEmail(email);
    }
    
    @Override
    public User createSimpleUser(final String username, final String email, String password, String fullName,
            String company, final String validationBaseURL) throws UserManagementException, MailException {
        return createSimpleUser(username, email, password, fullName, company, /* locale */ null, validationBaseURL);
    }

    @Override
    public User createSimpleUser(final String username, final String email, String password, String fullName,
            String company, Locale locale, final String validationBaseURL) throws UserManagementException, MailException {
        if (store.getUserByName(username) != null) {
            throw new UserManagementException(UserManagementException.USER_ALREADY_EXISTS);
        }
        if (username == null || username.length() < 3) {
            throw new UserManagementException(UserManagementException.USERNAME_DOES_NOT_MEET_REQUIREMENTS);
        } else if (password == null || password.length() < 5) {
            throw new UserManagementException(UserManagementException.PASSWORD_DOES_NOT_MEET_REQUIREMENTS);
        }
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        byte[] salt = rng.nextBytes().getBytes();
        String hashedPasswordBase64 = hashPassword(password, salt);
        UsernamePasswordAccount upa = new UsernamePasswordAccount(username, hashedPasswordBase64, salt);
        final User result = store.createUser(username, email, upa);
        result.setFullName(fullName);
        result.setCompany(company);
        result.setLocale(locale);
        final String emailValidationSecret = result.startEmailValidation();
        // don't replicate exception handling; replicate only the effect on the user store
        apply(s->s.internalStoreUser(result));
        if (validationBaseURL != null) {
            new Thread("e-mail validation for user " + username + " with e-mail address " + email) {
                @Override
                public void run() {
                    try {
                        startEmailValidation(result, emailValidationSecret, validationBaseURL);
                    } catch (MailException e) {
                        logger.log(Level.SEVERE, "Error sending mail for new account validation of user " + username
                                + " to address " + email, e);
                    }
                }
            }.start();
        }
        return result;
    }

    @Override
    public Void internalStoreUser(User user) {
        store.updateUser(user);
        return null;
    }

    @Override
    public void updateSimpleUserPassword(String username, String newPassword) throws UserManagementException {
        final User user = store.getUserByName(username);
        if (user == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        updateSimpleUserPassword(user, newPassword);
    }

    private void updateSimpleUserPassword(final User user, String newPassword) throws UserManagementException {
        if (newPassword == null || newPassword.length() < 5) {
            throw new UserManagementException(UserManagementException.PASSWORD_DOES_NOT_MEET_REQUIREMENTS);
        }
        // for non-admins, check that the old password is correct
        final UsernamePasswordAccount account = (UsernamePasswordAccount) user.getAccount(AccountType.USERNAME_PASSWORD);
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        byte[] salt = rng.nextBytes().getBytes();
        String hashedPasswordBase64 = hashPassword(newPassword, salt);
        account.setSalt(salt);
        account.setSaltedPassword(hashedPasswordBase64);
        user.passwordWasReset();
        apply(s->s.internalStoreUser(user));
    }

    @Override
    public void updateUserProperties(String username, String fullName, String company, Locale locale) throws UserManagementException {
        final User user = store.getUserByName(username);
        if (user == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        updateUserProperties(user, fullName, company, locale);
    }

    private void updateUserProperties(User user, String fullName, String company, Locale locale) {
        user.setFullName(fullName);
        user.setCompany(company);
        user.setLocale(locale);
        apply(s->s.internalStoreUser(user));
    }

    @Override
    public boolean checkPassword(String username, String password) throws UserManagementException {
        final User user = store.getUserByName(username);
        if (user == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        final UsernamePasswordAccount account = (UsernamePasswordAccount) user.getAccount(AccountType.USERNAME_PASSWORD);
        String hashedOldPassword = hashPassword(password, account.getSalt());
        return Util.equalsWithNull(hashedOldPassword, account.getSaltedPassword());
    }
    
    @Override
    public boolean checkPasswordResetSecret(String username, String passwordResetSecret) throws UserManagementException {
        final User user = store.getUserByName(username);
        if (user == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        return Util.equalsWithNull(user.getPasswordResetSecret(), passwordResetSecret);
    }

    @Override
    public void updateSimpleUserEmail(final String username, final String newEmail, final String validationBaseURL) throws UserManagementException {
        final User user = store.getUserByName(username);
        if (user == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        logger.info("Changing e-mail address of user "+username+" to "+newEmail);
        final String validationSecret = user.setEmail(newEmail);
        new Thread("e-mail validation after changing e-mail of user " + username + " to " + newEmail) {
            @Override
            public void run() {
                try {
                    startEmailValidation(user, validationSecret, validationBaseURL);
                } catch (MailException e) {
                    logger.log(Level.SEVERE, "Error sending mail to validate e-mail address change for user "
                            + username + " to address " + newEmail, e);
                }
            }
        }.start();
        apply(s->s.internalStoreUser(user));
    }

    @Override
    public boolean validateEmail(String username, String validationSecret) throws UserManagementException {
        final User user = store.getUserByName(username);
        if (user == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        final boolean result = user.validate(validationSecret);
        apply(s->s.internalStoreUser(user));
        return result;
    }

    /**
     * {@link User#startEmailValidation() Triggers} e-mail validation for the <code>user</code> object and sends out a
     * URL to the user's e-mail that has the validation secret ready for validation by clicking.
     * 
     * @param validationSecret
     *            the result of either {@link User#startEmailValidation()} or {@link User#setEmail(String)}.
     * @param baseURL
     *            the URL under which the user can reach the e-mail validation service; this URL is required to assemble
     *            a validation URL that is sent by e-mail to the user, to make the user return the validation secret to
     *            the right server again.
     */
    private void startEmailValidation(User user, String validationSecret, String baseURL) throws MailException {
        try {
            Map<String, String> urlParameters = new HashMap<>();
            urlParameters.put("u", URLEncoder.encode(user.getName(), "UTF-8"));
            urlParameters.put("v", URLEncoder.encode(validationSecret, "UTF-8"));
            StringBuilder url = buildURL(baseURL, urlParameters);
            sendMail(user.getName(), "e-Mail Validation",
                    "Please click on the link below to validate your e-mail address for user "+user.getName()+".\n   "+url.toString());
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.SEVERE,
                    "Internal error: encoding UTF-8 not found. Couldn't send e-mail to user " + user.getName()
                            + " at e-mail address " + user.getEmail(), e);
        }
    }

    public StringBuilder buildURL(String baseURL, Map<String, String> urlParameters) {
        StringBuilder url = new StringBuilder(baseURL);
        // Potentially contained hash is checked to support place-based mail verification
        boolean first = !baseURL.contains("?") || baseURL.contains("#");
        for (Map.Entry<String, String> e : urlParameters.entrySet()) {
            if (first) {
                url.append('?');
                first = false;
            } else {
                url.append('&');
            }
            url.append(e.getKey());
            url.append('=');
            url.append(e.getValue());
        }
        return url;
    }

    protected String hashPassword(String password, Object salt) {
        return new Sha256Hash(password, salt, 1024).toBase64();
    }

    @Override
    public Iterable<String> getRolesFromUser(String name) throws UserManagementException {
        return store.getRolesFromUser(name);
    }

    @Override
    public void addRoleForUser(String username, String role) {
        apply(s->s.internalAddRoleForUser(username, role));
    }

    @Override
    public Void internalAddRoleForUser(String username, String role) throws UserManagementException {
        store.addRoleForUser(username, role);
        return null;
    }

    @Override
    public void removeRoleFromUser(String username, String role) {
        apply(s->s.internalRemoveRoleFromUser(username, role));
    }

    @Override
    public Void internalRemoveRoleFromUser(String username, String role) throws UserManagementException {
        store.removeRoleFromUser(username, role);
        return null;
    }

    @Override
    public Iterable<String> getPermissionsFromUser(String username) throws UserManagementException {
        return store.getPermissionsFromUser(username);
    }

    @Override
    public void removePermissionFromUser(String username, String permissionToRemove) {
        apply(s->s.internalRemovePermissionForUser(username, permissionToRemove));
    }

    @Override
    public Void internalRemovePermissionForUser(String username, String permissionToRemove) throws UserManagementException {
        store.removePermissionFromUser(username, permissionToRemove);
        return null;
    }

    @Override
    public void addPermissionForUser(String username, String permissionToAdd) {
        apply(s->s.internalAddPermissionForUser(username, permissionToAdd));
    }

    @Override
    public Void internalAddPermissionForUser(String username, String permissionToAdd) throws UserManagementException {
        store.addPermissionForUser(username, permissionToAdd);
        return null;
    }

    @Override
    public void deleteUser(String username) throws UserManagementException {
        apply(s->s.internalDeleteUser(username));
    }

    @Override
    public Void internalDeleteUser(String username) throws UserManagementException {
        store.deleteUser(username);
        return null;
    }

    @Override
    public boolean setSetting(String key, Object setting) {
        return apply(s->s.internalSetSetting(key, setting));
    }

    @Override
    public Boolean internalSetSetting(String key, Object setting) {
        return store.setSetting(key, setting);
    }

    @Override
    public <T> T getSetting(String key, Class<T> clazz) {
        return store.getSetting(key, clazz);
    }

    @Override
    public Map<String, Object> getAllSettings() {
        return store.getAllSettings();
    }

    @Override
    public Map<String, Class<?>> getAllSettingTypes() {
        return store.getAllSettingTypes();
    }

    @Override
    public User createSocialUser(String name, SocialUserAccount socialUserAccount) throws UserManagementException {
        if (store.getUserByName(name) != null) {
            throw new UserManagementException(UserManagementException.USER_ALREADY_EXISTS);
        }
        return store.createUser(name, socialUserAccount.getProperty(Social.EMAIL.name()), socialUserAccount);
    }

    @Override
    public User verifySocialUser(Credential credential) throws UserManagementException {
        OAuthToken otoken = new OAuthToken(credential, credential.getVerifier());
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()) {
            try {
                subject.login(otoken);
                logger.info("User [" + subject.getPrincipal().toString() + "] logged in successfully.");
            } catch (UnknownAccountException uae) {
                logger.info("There is no user with username of " + subject.getPrincipal());
                throw new UserManagementException("Invalid credentials!");
            } catch (IncorrectCredentialsException ice) {
                logger.info("Password for account " + subject.getPrincipal() + " was incorrect!");
                throw new UserManagementException("Invalid credentials!");
            } catch (LockedAccountException lae) {
                logger.info("The account for username " + subject.getPrincipal() + " is locked.  "
                        + "Please contact your administrator to unlock it.");
                throw new UserManagementException("Invalid credentials!");
            } catch (AuthenticationException ae) {
                logger.log(Level.SEVERE, ae.getLocalizedMessage());
                throw new UserManagementException("An error occured while authenticating the user!");
            }
        }
        String username = subject.getPrincipal().toString();
        if (username == null) {
            logger.info("Something went wrong while authneticating, check doGetAuthenticationInfo() in "
                    + OAuthRealm.class.getName() + ".");
            throw new UserManagementException("An error occured while authenticating the user!");
        }
        User user = store.getUserByName(username);
        if (user == null) {
            logger.info("Could not find user " + username);
            throw new UserManagementException("An error occured while authenticating the user!");
        }
        return user;
    }

    @Override
    public User getCurrentUser() {
        final User result;
        Subject subject = SecurityUtils.getSubject();
        if (subject == null || !subject.isAuthenticated()) {
            result = null;
        } else {
            String username = subject.getPrincipal().toString();
            if (username == null || username.length() <= 0) {
                result = null;
            } else {
                result = store.getUserByName(username);
            }
        }
        return result;
    }

    @Override
    public String getAuthenticationUrl(Credential credential) throws UserManagementException {
        Token requestToken = null;
        String authorizationUrl = null;
        int authProvider = credential.getAuthProvider();
        OAuthService service = getOAuthService(authProvider);
        if (service == null) {
            throw new UserManagementException("Could not build OAuthService");
        }
        if (authProvider == ClientUtils.TWITTER || authProvider == ClientUtils.YAHOO
                || authProvider == ClientUtils.LINKEDIN || authProvider == ClientUtils.FLICKR
                || authProvider == ClientUtils.IMGUR || authProvider == ClientUtils.TUMBLR
                || authProvider == ClientUtils.VIMEO || authProvider == ClientUtils.GOOGLE) {
            String authProviderName = ClientUtils.getAuthProviderName(authProvider);
            logger.info(authProviderName + " requires Request token first.. obtaining..");
            try {
                requestToken = service.getRequestToken();
                logger.info("Got request token: " + requestToken);
                // we must save in the session. It will be required to
                // get the access token
                SessionUtils.saveRequestTokenToSession(requestToken);
            } catch (Exception e) {
                throw new UserManagementException("Could not get request token for " + authProvider + " "
                        + e.getMessage());
            }
        }
        logger.info("Getting Authorization url...");
        try {
            authorizationUrl = service.getAuthorizationUrl(requestToken);
            // Facebook has optional state var to protect against CSFR.
            // We'll use it
            if (authProvider == ClientUtils.FACEBOOK || authProvider == ClientUtils.GITHUB
                    || authProvider == ClientUtils.INSTAGRAM) {
                String state = UUID.randomUUID().toString();
                authorizationUrl += "&state=" + state;
                SessionUtils.saveStateToSession(state);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new UserManagementException("Could not get Authorization url: ");
        }

        if (authProvider == ClientUtils.FLICKR) {
            authorizationUrl += "&perms=read";
        }

        if (authProvider == ClientUtils.FACEBOOK) {
            authorizationUrl += "&scope=email";
        }

        logger.info("Authorization url: " + authorizationUrl);
        return authorizationUrl;
    }

    private OAuthService getOAuthService(int authProvider) {
        OAuthService service = null;
        switch (authProvider) {
        case ClientUtils.FACEBOOK: {
            service = new ServiceBuilder().provider(FacebookApi.class)
                    .apiKey(store.getSetting(SocialSettingsKeys.OAUTH_FACEBOOK_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_FACEBOOK_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.GOOGLE: {
            service = new ServiceBuilder().provider(GoogleApi.class)
                    .apiKey(store.getSetting(SocialSettingsKeys.OAUTH_GOOGLE_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_GOOGLE_APP_SECRET.name(), String.class))
                    .scope(store.getSetting(SocialSettingsKeys.OAUTH_GOOGLE_SCOPE.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();

            break;
        }

        case ClientUtils.TWITTER: {
            service = new ServiceBuilder().provider(TwitterApi.class)
                    .apiKey(store.getSetting(SocialSettingsKeys.OAUTH_TWITTER_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_TWITTER_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }
        case ClientUtils.YAHOO: {
            service = new ServiceBuilder().provider(YahooApi.class)
                    .apiKey(store.getSetting(SocialSettingsKeys.OAUTH_YAHOO_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_YAHOO_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.LINKEDIN: {
            service = new ServiceBuilder().provider(LinkedInApi.class)
                    .apiKey(store.getSetting(SocialSettingsKeys.OAUTH_LINKEDIN_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_LINKEDIN_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.INSTAGRAM: {
            service = new ServiceBuilder().provider(InstagramApi.class)
                    .apiKey(store.getSetting(SocialSettingsKeys.OAUTH_INSTAGRAM_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_INSTAGRAM_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.GITHUB: {
            service = new ServiceBuilder().provider(GithubApi.class)
                    .apiKey(store.getSetting(SocialSettingsKeys.OAUTH_GITHUB_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_GITHUB_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;

        }

        case ClientUtils.IMGUR: {
            service = new ServiceBuilder().provider(ImgUrApi.class)
                    .apiKey(store.getSetting(SocialSettingsKeys.OAUTH_IMGUR_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_IMGUR_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.FLICKR: {
            service = new ServiceBuilder().provider(FlickrApi.class)
                    .apiKey(store.getSetting(SocialSettingsKeys.OAUTH_FLICKR_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_FLICKR_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.VIMEO: {
            service = new ServiceBuilder().provider(VimeoApi.class)
                    .apiKey(store.getSetting(SocialSettingsKeys.OAUTH_VIMEO_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_VIMEO_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.WINDOWS_LIVE: {
            // a Scope must be specified
            service = new ServiceBuilder().provider(LiveApi.class)
                    .apiKey(store.getSetting(SocialSettingsKeys.OAUTH_WINDOWS_LIVE_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_WINDOWS_LIVE_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).scope("wl.basic").build();
            break;
        }

        case ClientUtils.TUMBLR: {
            service = new ServiceBuilder().provider(TumblrApi.class)
                    .apiKey(store.getSetting(SocialSettingsKeys.OAUTH_TUMBLR_LIVE_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_TUMBLR_LIVE_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.FOURSQUARE: {
            service = new ServiceBuilder().provider(Foursquare2Api.class)
                    .apiKey(store.getSetting(SocialSettingsKeys.OAUTH_FOURSQUARE_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_FOURSQUARE_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        default: {
            return null;
        }

        }
        return service;
    }

    @Override
    public void addSetting(String key, Class<?> clazz) throws UserManagementException {
        if (!isValidSettingsKey(key)) {
            throw new UserManagementException("Invalid key!");
        }
        apply(s->s.internalAddSetting(key, clazz));
    }

    @Override
    public Void internalAddSetting(String key, Class<?> clazz) {
        store.addSetting(key, clazz);
        return null;
    }

    public static boolean isValidSettingsKey(String key) {
        char[] characters = key.toCharArray();
        for (char c : characters) {
            if (!Character.isLetter(c) && c != '_') {
                return false;
            }
        }
        return true;
    }

    @Override
    public void refreshSecurityConfig(ServletContext context) {
        logger.info("Refreshing security configuration!");
        IniWebEnvironment env = (IniWebEnvironment) WebUtils.getRequiredWebEnvironment(context);
        System.out.println("Env: " + env);
        FilterChainResolver resolver = env.getFilterChainResolver();
        System.out.println("Resolver: " + resolver);
        if (resolver instanceof PathMatchingFilterChainResolver) {
            PathMatchingFilterChainResolver pmfcr = (PathMatchingFilterChainResolver) resolver;
            FilterChainManager filterChainManager = pmfcr.getFilterChainManager();
            System.out.println(filterChainManager);

            Set<String> chainNames = filterChainManager.getChainNames();

            System.out.println("Chains:");
            for (String s : chainNames) {
                System.out.println(s + ": " + Arrays.toString(filterChainManager.getChain(s).toArray(new Filter[0])));
            }
        }
    }

    public static Ini getShiroConfiguration() {
        return shiroConfiguration;
    }

    @Override
    public ReplicatingCacheManager getCacheManager() {
        return cacheManager;
    }
    
    private void ensureThatUserInQuestionIsLoggedInOrCurrentUserIsAdmin(String username) {
        final Subject subject = SecurityUtils.getSubject();
        if (!subject.hasRole(DefaultRoles.ADMIN.getRolename()) && (subject.getPrincipal() == null
                || !username.equals(subject.getPrincipal().toString()))) {
            final String currentUserName = subject.getPrincipal() == null ? "<anonymous>"
                    : subject.getPrincipal().toString();
            throw new AuthorizationException(
                    "User " + currentUserName + " does not have the permission required to access data of user " + username);
        }
    }

    @Override
    public void setPreference(final String username, final String key, final String value) {
        ensureThatUserInQuestionIsLoggedInOrCurrentUserIsAdmin(username);
        apply(s->s.internalSetPreference(username, key, value));
    }

    @Override
    public void setPreferenceObject(final String username, final String key, final Object value) {
        ensureThatUserInQuestionIsLoggedInOrCurrentUserIsAdmin(username);
        final String preferenceObjectAsString = internalSetPreferenceObject(username, key, value);
        apply(s->s.internalSetPreference(username, key, preferenceObjectAsString));
    }

    @Override
    public Void internalSetPreference(final String username, final String key, final String value) {
        store.setPreference(username, key, value);
        return null;
    }
    
    @Override
    public String internalSetPreferenceObject(final String username, final String key, final Object value) {
        return store.setPreferenceObject(username, key, value);
    }
    
    @Override
    public void unsetPreference(String username, String key) {
        ensureThatUserInQuestionIsLoggedInOrCurrentUserIsAdmin(username);
        apply(s->s.internalUnsetPreference(username, key));
    }

    @Override
    public Void internalUnsetPreference(String username, String key) {
        store.unsetPreference(username, key);
        return null;
    }

    @Override
    public Void internalSetAccessToken(String username, String accessToken) {
        store.setAccessToken(username, accessToken);
        return null;
    }
    
    @Override
    public String getAccessToken(String username) {
        return store.getAccessToken(username);
    }

    @Override
    public String getOrCreateAccessToken(String username) {
        String result = store.getAccessToken(username);
        if (result == null) {
            result = createAccessToken(username);
        }
        return result;
    }

    @Override
    public Void internalRemoveAccessToken(String username) {
        store.removeAccessToken(username);
        return null;
    }

    @Override
    public String getPreference(String username, String key) {
        ensureThatUserInQuestionIsLoggedInOrCurrentUserIsAdmin(username);
        return store.getPreference(username, key);
    }
    
    @Override
    public Map<String, String> getAllPreferences(String username) {
        ensureThatUserInQuestionIsLoggedInOrCurrentUserIsAdmin(username);
        return store.getAllPreferences(username);
    }
    
    @Override
    public String createAccessToken(String username) {
        User user = getUserByName(username);
        final String token;
        if (user != null) {
            RandomNumberGenerator rng = new SecureRandomNumberGenerator();
            byte[] salt = rng.nextBytes().getBytes();
            token = hashPassword(new String(rng.nextBytes().getBytes()), salt);
            apply(s -> s.internalSetAccessToken(user.getName(), token));
        } else {
            token = null;
        }
        return token;
    }
    
    @Override
    public void removeAccessToken(String username) {
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole(DefaultRoles.ADMIN.getRolename()) || username.equals(subject.getPrincipal().toString())) {
            apply(s -> s.internalRemoveAccessToken(username));
        } else {
            throw new org.apache.shiro.authz.AuthorizationException("User " + subject.getPrincipal().toString()
                    + " does not have permission to remove access token of user " + username);
        }
    }

    // ----------------- Replication -------------
    @Override
    public void clearReplicaState() throws MalformedURLException, IOException, InterruptedException {
        store.clear();
    }

    @Override
    public Serializable getId() {
        return getClass().getName();
    }
    
    @Override
    public ObjectInputStream createObjectInputStreamResolvingAgainstCache(InputStream is) throws IOException {
        return new ObjectInputStreamResolvingAgainstSecurityCache(is, store);
    }

    @Override
    public void initiallyFillFromInternal(ObjectInputStream is) throws IOException, ClassNotFoundException,
            InterruptedException {
        ReplicatingCacheManager newCacheManager = (ReplicatingCacheManager) is.readObject();
        cacheManager.replaceContentsFrom(newCacheManager);
        // overriding thread context class loader because the user store may be provided by a different bundle;
        // We're assuming here that the user store service is provided by the same bundle in the replica as on the master.
        ClassLoader oldCCL = Thread.currentThread().getContextClassLoader();
        if (store != null) {
            Thread.currentThread().setContextClassLoader(store.getClass().getClassLoader());
        }
        try {
            UserStore newUserStore = (UserStore) is.readObject();
            store.replaceContentsFrom(newUserStore);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCCL);
        }
    }

    @Override
    public void serializeForInitialReplicationInternal(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.writeObject(cacheManager);
        objectOutputStream.writeObject(store);
    }

    @Override
    public Iterable<OperationExecutionListener<ReplicableSecurityService>> getOperationExecutionListeners() {
        return operationExecutionListeners.keySet();
    }

    @Override
    public void addOperationExecutionListener(OperationExecutionListener<ReplicableSecurityService> listener) {
        operationExecutionListeners.put(listener, listener);
    }

    @Override
    public void removeOperationExecutionListener(OperationExecutionListener<ReplicableSecurityService> listener) {
        operationExecutionListeners.remove(listener);
    }

    @Override
    public ReplicationMasterDescriptor getMasterDescriptor() {
        return replicatingFromMaster;
    }

    @Override
    public void startedReplicatingFrom(ReplicationMasterDescriptor master) {
        this.replicatingFromMaster = master;
    }

    @Override
    public void stoppedReplicatingFrom(ReplicationMasterDescriptor master) {
        this.replicatingFromMaster = null;
    }

    @Override
    public void addOperationSentToMasterForReplication(
            OperationWithResultWithIdWrapper<ReplicableSecurityService, ?> operationWithResultWithIdWrapper) {
        this.operationsSentToMasterForReplication.add(operationWithResultWithIdWrapper);
    }

    @Override
    public boolean hasSentOperationToMaster(OperationWithResult<ReplicableSecurityService, ?> operation) {
        return this.operationsSentToMasterForReplication.remove(operation);
    }

    @Override
    public void clearState() throws Exception {
        store.clear();
        initEmptyStore();
        CacheManager cm = getSecurityManager().getCacheManager();
        if (cm instanceof ReplicatingCacheManager) {
            ((ReplicatingCacheManager) cm).clear();
        }
    }

    @Override
    public <T> T getPreferenceObject(String username, String key) {
        ensureThatUserInQuestionIsLoggedInOrCurrentUserIsAdmin(username);
        return store.getPreferenceObject(username, key);
    }

    @Override
    public void storeSession(String cacheName, Session session) {
        PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory().storeSession(cacheName, session);
    }

    @Override
    public void removeSession(String cacheName, Session session) {
        PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory().removeSession(cacheName, session);
    }

    @Override
    public void removeAllSessions(String cacheName) {
        PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory().removeAllSessions(cacheName);
    }
}
