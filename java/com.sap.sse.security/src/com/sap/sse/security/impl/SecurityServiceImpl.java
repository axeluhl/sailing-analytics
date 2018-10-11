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
import java.util.Collections;
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
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.impl.OperationWithResultWithIdWrapper;
import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.Action;
import com.sap.sse.security.ActionWithResult;
import com.sap.sse.security.BearerAuthenticationToken;
import com.sap.sse.security.ClientUtils;
import com.sap.sse.security.Credential;
import com.sap.sse.security.GithubApi;
import com.sap.sse.security.InstagramApi;
import com.sap.sse.security.OAuthRealm;
import com.sap.sse.security.OAuthToken;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.SessionCacheManager;
import com.sap.sse.security.SessionUtils;
import com.sap.sse.security.Social;
import com.sap.sse.security.SocialSettingsKeys;
import com.sap.sse.security.UserImpl;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.Account.AccountType;
import com.sap.sse.security.shared.AdminRole;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.RoleImpl;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.SocialUserAccount;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.UserRole;
import com.sap.sse.security.shared.UsernamePasswordAccount;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.OwnershipImpl;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.util.ClearStateTestSupport;

public class SecurityServiceImpl implements ReplicableSecurityService, ClearStateTestSupport {

    private static final Logger logger = Logger.getLogger(SecurityServiceImpl.class.getName());

    private static final String ADMIN_USERNAME = "admin";

    private static final String ADMIN_DEFAULT_PASSWORD = "admin";

    private CachingSecurityManager securityManager;
    
    /**
     * A cache manager that the {@link SessionCacheManager} delegates to. This way, multiple Shiro configurations can
     * share the cache manager provided as a singleton within this bundle instance. The cache manager is replicating,
     * forwarding changes to the caches to all replicas registered.
     */
    private final ReplicatingCacheManager cacheManager;
    
    private UserStore userStore;
    private AccessControlStore accessControlStore;
    
    private final ServiceTracker<MailService, MailService> mailServiceTracker;
    
    private final ConcurrentMap<OperationExecutionListener<ReplicableSecurityService>, OperationExecutionListener<ReplicableSecurityService>> operationExecutionListeners;

    /**
     * The master from which this replicable is currently replicating, or <code>null</code> if this replicable is not currently
     * replicated from any master.
     */
    private ReplicationMasterDescriptor replicatingFromMaster;
    
    private Set<OperationWithResultWithIdWrapper<?, ?>> operationsSentToMasterForReplication;
    
    private ThreadLocal<Boolean> currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster = ThreadLocal.withInitial(() -> false);
    
    private static Ini shiroConfiguration;
    static {
        shiroConfiguration = new Ini();
        shiroConfiguration.loadFromPath("classpath:shiro.ini");
    }
    
    public SecurityServiceImpl(UserStore userStore, AccessControlStore accessControlStore) {
        this(/* mail service tracker */ null, userStore, accessControlStore);
    }

    /**
     * @param mailProperties
     *            must not be <code>null</code>
     */
    public SecurityServiceImpl(ServiceTracker<MailService, MailService> mailServiceTracker, UserStore userStore, AccessControlStore accessControlStore) {
        this(mailServiceTracker, userStore, accessControlStore, /* setAsActivatorTestSecurityService */ false);
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
    public SecurityServiceImpl(ServiceTracker<MailService, MailService> mailServiceTracker, UserStore userStore, AccessControlStore accessControlStore, boolean setAsActivatorSecurityService) {
        logger.info("Initializing Security Service with user store " + userStore);
        if (setAsActivatorSecurityService) {
            Activator.setSecurityService(this);
        }
        operationsSentToMasterForReplication = new HashSet<>();
        cacheManager = new ReplicatingCacheManager();
        this.operationExecutionListeners = new ConcurrentHashMap<>();
        this.userStore = userStore;
        this.accessControlStore = accessControlStore;
        this.mailServiceTracker = mailServiceTracker;
        // Create default users if no users exist yet.
        initEmptyStore();
        initEmptyAccessControlStore();
        Factory<SecurityManager> factory = new WebIniSecurityManagerFactory(shiroConfiguration);
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

    @Override
    public boolean isCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster() {
        return currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster.get();
    }

    @Override
    public void setCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster(boolean currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster) {
        this.currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster.set(currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster);
    }

    /**
     * Creates a default "admin" user with initial password "admin" and initial role "admin" if the user <code>store</code>
     * is empty.
     */
    private void initEmptyStore() {
        final AdminRole adminRolePrototype = AdminRole.getInstance();
        RoleDefinition adminRoleDefinition = getRoleDefinition(adminRolePrototype.getId());
        if (adminRoleDefinition == null) {
            userStore.createPredefinedRoles();
            adminRoleDefinition = getRoleDefinition(adminRolePrototype.getId());
            assert adminRoleDefinition != null;
        }
        try {
            final SecurityUser adminUser;
            if (!userStore.hasUsers()) {
                logger.info("No users found, creating default user \""+ADMIN_USERNAME+"\" with password \""+ADMIN_DEFAULT_PASSWORD+"\"");
                adminUser = createSimpleUser(ADMIN_USERNAME, "nobody@sapsailing.com", ADMIN_DEFAULT_PASSWORD,
                        /* fullName */ null, /* company */ null, Locale.ENGLISH, /* validationBaseURL */ null);
            } else {
                adminUser = userStore.getUserByName(ADMIN_USERNAME);
            }
            setOwnership(SecuredSecurityTypes.USER.getQualifiedObjectIdentifier(ADMIN_USERNAME), adminUser, /* no admin tenant */ null, ADMIN_USERNAME);
            addRoleForUser(adminUser, new RoleImpl(adminRoleDefinition));
        } catch (UserManagementException | MailException | UserGroupManagementException e) {
            logger.log(Level.SEVERE, "Exception while creating default admin user", e);
        }
    }
    
    private void initEmptyAccessControlStore() {
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
        final User user = userStore.getUserByName(username);
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
    public OwnershipAnnotation getOwnership(QualifiedObjectIdentifier idOfOwnedObjectAsString) {
        return accessControlStore.getOwnership(idOfOwnedObjectAsString);
    }

    @Override
    public OwnershipAnnotation createDefaultOwnershipForNewObject(QualifiedObjectIdentifier idOfNewObject) {
        return new OwnershipAnnotation(new OwnershipImpl(getCurrentUser(), getCurrentUser().getDefaultTenant()),
                idOfNewObject, /* display name */ idOfNewObject.toString());
    }
    
    @Override
    public void setOwnership(OwnershipAnnotation ownershipAnnotation) {
        accessControlStore.setOwnership(ownershipAnnotation.getIdOfAnnotatedObject(), ownershipAnnotation.getAnnotation().getUserOwner(),
                ownershipAnnotation.getAnnotation().getTenantOwner(), ownershipAnnotation.getDisplayNameOfAnnotatedObject());
    }

    @Override
    public void deleteAllDataForRemovedObject(QualifiedObjectIdentifier idOfRemovedObject) {
        deleteAccessControlList(idOfRemovedObject);
        deleteOwnership(idOfRemovedObject);
    }

    @Override
    public RoleDefinition getRoleDefinition(UUID idOfRoleDefinition) {
        return userStore.getRoleDefinition(idOfRoleDefinition);
    }

    /**
     * Returns a list of all existing access control lists. This is possibly not complete in the sense
     * that there is a access control list for every access controlled data object.
     */
    @Override
    public Iterable<AccessControlListAnnotation> getAccessControlLists() {
        return accessControlStore.getAccessControlLists();
    }

    @Override
    public AccessControlListAnnotation getAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObjectAsString) {
        return accessControlStore.getAccessControlList(idOfAccessControlledObjectAsString);
    }

    @Override
    public SecurityService setEmptyAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObjectAsString) {
        return setEmptyAccessControlList(idOfAccessControlledObjectAsString, /* display name of access-controlled object */ null);
    }

    @Override
    public SecurityService setEmptyAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObjectAsString, String displayNameOfAccessControlledObject) {
        apply(s->s.internalSetEmptyAccessControlList(idOfAccessControlledObjectAsString, displayNameOfAccessControlledObject));
        return this;
    }

    @Override
    public Void internalSetEmptyAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, String displayNameOfAccessControlledObject) {
        accessControlStore.setEmptyAccessControlList(idOfAccessControlledObject, displayNameOfAccessControlledObject);
        return null;
    }

    @Override
    public AccessControlList updateAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, Map<UserGroup, Set<String>> permissionMap) {
        if (getAccessControlList(idOfAccessControlledObject) == null) {
            setEmptyAccessControlList(idOfAccessControlledObject);
        }
        for (Map.Entry<UserGroup, Set<String>> entry : permissionMap.entrySet()) {
            final UUID userGroupId = entry.getKey().getId();
            final Set<String> actions = entry.getValue();
            // avoid the UserGroup object having to be serialized with the operation by using the ID
            apply(s->s.internalAclPutPermissions(idOfAccessControlledObject, userGroupId, actions));
        }
        return accessControlStore.getAccessControlList(idOfAccessControlledObject).getAnnotation();
    }

    @Override
    public Void internalAclPutPermissions(QualifiedObjectIdentifier idOfAccessControlledObject, UUID groupId, Set<String> actions) {
        accessControlStore.setAclPermissions(idOfAccessControlledObject, getUserGroup(groupId), actions);
        return null;
    }

    /*
     * @param name The name of the user group to add
     */
    @Override
    public AccessControlList addToAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, UserGroup group, String action) {
        if (getAccessControlList(idOfAccessControlledObject) == null) {
            setEmptyAccessControlList(idOfAccessControlledObject);
        }
        final UUID groupId = group.getId();
        apply(s->s.internalAclAddPermission(idOfAccessControlledObject, groupId, action));
        return accessControlStore.getAccessControlList(idOfAccessControlledObject).getAnnotation();
    }

    @Override
    public Void internalAclAddPermission(QualifiedObjectIdentifier idOfAccessControlledObjectAsString, UUID groupId, String permission) {
        accessControlStore.addAclPermission(idOfAccessControlledObjectAsString, getUserGroup(groupId), permission);
        return null;
    }

    /*
     * @param name The name of the user group to remove
     */
    @Override
    public AccessControlList removeFromAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObjectAsString, UserGroup group, String permission) {
        final AccessControlList result;
        if (getAccessControlList(idOfAccessControlledObjectAsString) != null) {
            final UUID groupId = group.getId();
            apply(s->s.internalAclRemovePermission(idOfAccessControlledObjectAsString, groupId, permission));
            result = accessControlStore.getAccessControlList(idOfAccessControlledObjectAsString).getAnnotation();
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public Void internalAclRemovePermission(QualifiedObjectIdentifier idOfAccessControlledObjectAsString, UUID groupId, String permission) {
        accessControlStore.removeAclPermission(idOfAccessControlledObjectAsString, getUserGroup(groupId), permission);
        return null;
    }

    @Override
    public void deleteAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObjectAsString) {
        if (getAccessControlList(idOfAccessControlledObjectAsString) != null) {
            apply(s->s.internalDeleteAcl(idOfAccessControlledObjectAsString));
        }
    }

    @Override
    public Void internalDeleteAcl(QualifiedObjectIdentifier idOfAccessControlledObjectAsString) {
        accessControlStore.removeAccessControlList(idOfAccessControlledObjectAsString);
        return null;
    }

    @Override
    public Ownership setOwnership(QualifiedObjectIdentifier idOfOwnedObjectAsString, SecurityUser userOwner, UserGroup tenantOwner) {
        return setOwnership(idOfOwnedObjectAsString, userOwner, tenantOwner, /* displayNameOfOwnedObject */ null);
    }

    @Override
    public Ownership setOwnership(QualifiedObjectIdentifier idOfOwnedObjectAsString, SecurityUser userOwner,
            UserGroup tenantOwner, String displayNameOfOwnedObject) {
        final UUID tenantId;
        if (tenantOwner == null || userOwner != null && !tenantOwner.contains(userOwner)) {
            tenantId = userOwner == null || userOwner.getDefaultTenant() == null ? null
                    : userOwner.getDefaultTenant().getId();
        } else {
            tenantId = tenantOwner.getId();
        }
        final String userOwnerName = userOwner == null ? null : userOwner.getName();
        return apply(s -> s.internalSetOwnership(idOfOwnedObjectAsString, userOwnerName, tenantId,
                displayNameOfOwnedObject));
    }
    
    @Override
    public void setDefaultOwnershipAndRevertOnError(QualifiedObjectIdentifier objectIdentifier, Action action) throws Exception {
        setDefaultOwnershipAndRevertOnError(objectIdentifier, () -> {
            action.run();
            return null;
        });
    }
    
    @Override
    public <T> T setDefaultOwnershipAndRevertOnError(QualifiedObjectIdentifier objectIdentifier,
            ActionWithResult<T> action) throws Exception {
        boolean didSetOwnerShip = false;
        if (getOwnership(objectIdentifier) == null) {
            this.setOwnership(this.createDefaultOwnershipForNewObject(objectIdentifier));
            didSetOwnerShip = true;
        }
        try {
            return action.run();
        } catch (Exception e) {
            if (didSetOwnerShip) {
                this.deleteOwnership(objectIdentifier); // revert preliminary ownership allocation
            }
            throw e;
        }
    }

    @Override
    public Ownership internalSetOwnership(QualifiedObjectIdentifier idAsString, String userOwnerName, UUID tenantOwnerId, String displayName) {
        return accessControlStore.setOwnership(idAsString, getUserByName(userOwnerName), getUserGroup(tenantOwnerId), displayName).getAnnotation();
    }

    @Override
    public void deleteOwnership(QualifiedObjectIdentifier idOfOwnedObjectAsString) {
        if (getOwnership(idOfOwnedObjectAsString) != null) {
            apply(s->s.internalDeleteOwnership(idOfOwnedObjectAsString));
        }
    }

    @Override
    public Void internalDeleteOwnership(QualifiedObjectIdentifier idOfOwnedObjectAsString) {
        accessControlStore.removeOwnership(idOfOwnedObjectAsString);
        return null;
    }

    @Override
    public Iterable<UserGroup> getUserGroupList() {
        return userStore.getUserGroups();
    }

    @Override
    public UserGroup getUserGroup(UUID id) {
        return userStore.getUserGroup(id);
    }

    @Override
    public UserGroup getUserGroupByName(String name) {
        return userStore.getUserGroupByName(name);
    }

    @Override
    public Iterable<UserGroup> getUserGroupsOfUser(SecurityUser user) {
        return userStore.getUserGroupsOfUser(user);
    }

    @Override
    public UserGroup createUserGroup(UUID id, String name) throws UserGroupManagementException {
        logger.info("Creating user group "+name+" with ID "+id);
        apply(s->s.internalCreateUserGroup(id, name));
        return userStore.getUserGroup(id);
    }

    @Override
    public Void internalCreateUserGroup(UUID id, String name) throws UserGroupManagementException {
        userStore.createUserGroup(id, name);
        return null;
    }

    @Override
    public void addUserToUserGroup(UserGroup userGroup, SecurityUser user) {
        logger.info("Adding user "+user.getName()+" to group "+userGroup.getName());
        userGroup.add(user);
        final UUID groupId = userGroup.getId();
        final String username = user.getName();
        apply(s->s.internalAddUserToUserGroup(groupId, username));
    }

    @Override
    public Void internalAddUserToUserGroup(UUID groupId, String username) {
        final UserGroup userGroup = getUserGroup(groupId);
        userGroup.add(getUserByName(username));
        userStore.updateUserGroup(userGroup);
        return null;
    }
    
    @Override
    public Void internalRemoveUserFromUserGroup(UUID groupId, String username) {
        final UserGroup userGroup = getUserGroup(groupId);
        userGroup.remove(getUserByName(username));
        userStore.updateUserGroup(userGroup);
        return null;
    }
    
    @Override
    public void removeUserFromUserGroup(UserGroup userGroup, SecurityUser user) {
        logger.info("Removing user "+user.getName()+" from group "+userGroup.getName());
        userGroup.remove(user);
        final UUID userGroupId = userGroup.getId();
        final String username = user.getName();
        apply(s->s.internalRemoveUserFromUserGroup(userGroupId, username));
    }

    @Override
    public void deleteUserGroup(UserGroup userGroup) throws UserGroupManagementException {
        logger.info("Removing user group "+userGroup.getName());
        final UUID groupId = userGroup.getId();
        apply(s->s.internalDeleteUserGroup(groupId));
    }
    
    @Override
    public Void internalDeleteUserGroup(UUID groupId) throws UserGroupManagementException {
        final UserGroup userGroup = getUserGroup(groupId);
        if (userGroup == null) {
            logger.warning("Strange: the user group with ID "+groupId+" which is about to be deleted couldn't be found");
        } else {
            userStore.deleteUserGroup(userGroup);
        }
        return null;
    }

    @Override
    public Iterable<User> getUserList() {
        return userStore.getUsers();
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
    public SecurityUser loginByAccessToken(String accessToken) {
        BearerAuthenticationToken token = new BearerAuthenticationToken(accessToken);
        logger.info("Trying to login with access token");
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
            final String username = (String) token.getPrincipal();
            return userStore.getUserByName(username);
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
        return userStore.getUserByName(name);
    }
    
    @Override
    public User getUserByAccessToken(String accessToken) {
        return userStore.getUserByAccessToken(accessToken);
    }

    @Override
    public SecurityUser getUserByEmail(String email) {
        return userStore.getUserByEmail(email);
    }

    @Override
    public UserImpl createSimpleUser(final String username, final String email, String password, String fullName,
            String company, final String validationBaseURL) throws UserManagementException, MailException, UserGroupManagementException {
        return createSimpleUser(username, email, password, fullName, company, /* locale */ null, validationBaseURL);
    }

    @Override
    public UserImpl createSimpleUser(final String username, final String email, String password, String fullName,
            String company, Locale locale, final String validationBaseURL) throws UserManagementException, MailException, UserGroupManagementException {
        logger.info("Creating user "+username);
        if (userStore.getUserByName(username) != null) {
            logger.warning("User "+username+" already exists");
            throw new UserManagementException(UserManagementException.USER_ALREADY_EXISTS);
        }
        final String defaultTenantNameForUsername = getDefaultTenantNameForUsername(username);
        final UserGroup tenant;
        if (username == null || username.length() < 3) {
            throw new UserManagementException(UserManagementException.USERNAME_DOES_NOT_MEET_REQUIREMENTS);
        } else if (password == null || password.length() < 5) {
            throw new UserManagementException(UserManagementException.PASSWORD_DOES_NOT_MEET_REQUIREMENTS);
        }
        if (userStore.getUserGroupByName(defaultTenantNameForUsername) != null) {
            logger.info("Found existing tenant "+defaultTenantNameForUsername+" to be used as default tenant for new user "+username);
            tenant = userStore.getUserGroupByName(defaultTenantNameForUsername);
        } else {
            logger.info("Creating user group "+defaultTenantNameForUsername+" as default tenant for new user "+username);
            tenant = createUserGroup(UUID.randomUUID(), defaultTenantNameForUsername);
        }
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        byte[] salt = rng.nextBytes().getBytes();
        String hashedPasswordBase64 = hashPassword(password, salt);
        UsernamePasswordAccount upa = new UsernamePasswordAccount(username, hashedPasswordBase64, salt);
        final UserImpl result = userStore.createUser(username, email, tenant, upa); // TODO: get the principal as owner
        // now the user creation needs to be replicated so that when replicating role addition and group assignment
        // the replica will be able to resolve the user correctly
        apply(s->s.internalStoreUser(result));
        addRoleForUser(result, new RoleImpl(UserRole.getInstance(), /* tenant qualifier */ null, /* user qualifier */ result));
        addUserToUserGroup(tenant, result);
        // the new user becomes its owner to ensure the user role is correctly working
        // the default tenant is the owning tenant to allow users having admin role for a specific server tenant to also be able to delete users
        accessControlStore.setOwnership(SecuredSecurityTypes.USER.getQualifiedObjectIdentifier(username), result, getDefaultTenant(), username);
        // the new user becomes the owning user of its own specific tenant which initially only contains the new user
        accessControlStore.setOwnership(SecuredSecurityTypes.USER_GROUP.getQualifiedObjectIdentifier(tenant.getId().toString()), result, tenant, tenant.getName());
        result.setFullName(fullName);
        result.setCompany(company);
        result.setLocale(locale);
        final String emailValidationSecret = result.startEmailValidation();
        // don't replicate exception handling; replicate only the effect on the user store
        apply(s->s.internalStoreUser(result));
        if (validationBaseURL != null && email != null && !email.trim().isEmpty()) {
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

    private String getDefaultTenantNameForUsername(final String username) {
        return username + "-tenant";
    }

    @Override
    public Void internalStoreUser(User user) {
        userStore.updateUser(user);
        return null;
    }

    @Override
    public void updateSimpleUserPassword(String username, String newPassword) throws UserManagementException {
        final User user = userStore.getUserByName(username);
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
        final User user = userStore.getUserByName(username);
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
        final User user = userStore.getUserByName(username);
        if (user == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        final UsernamePasswordAccount account = (UsernamePasswordAccount) user.getAccount(AccountType.USERNAME_PASSWORD);
        String hashedOldPassword = hashPassword(password, account.getSalt());
        return Util.equalsWithNull(hashedOldPassword, account.getSaltedPassword());
    }
    
    @Override
    public boolean checkPasswordResetSecret(String username, String passwordResetSecret) throws UserManagementException {
        final User user = userStore.getUserByName(username);
        if (user == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        return Util.equalsWithNull(user.getPasswordResetSecret(), passwordResetSecret);
    }

    @Override
    public void updateSimpleUserEmail(final String username, final String newEmail, final String validationBaseURL) throws UserManagementException {
        final User user = userStore.getUserByName(username);
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
        final User user = userStore.getUserByName(username);
        if (user == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        final boolean result = user.validate(validationSecret);
        apply(s->s.internalStoreUser(user));
        return result;
    }

    /**
     * {@link UserImpl#startEmailValidation() Triggers} e-mail validation for the <code>user</code> object and sends out a
     * URL to the user's e-mail that has the validation secret ready for validation by clicking.
     * 
     * @param validationSecret
     *            the result of either {@link UserImpl#startEmailValidation()} or {@link UserImpl#setEmail(String)}.
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
    public RoleDefinition createRoleDefinition(UUID roleId, String name) {
        return apply(s->s.internalCreateRoleDefinition(roleId, name));
    }

    @Override
    public RoleDefinition internalCreateRoleDefinition(UUID roleId, String name) {
        return userStore.createRoleDefinition(roleId, name, Collections.emptySet());
    }
    
    @Override
    public void deleteRoleDefinition(RoleDefinition roleDefinition) {
        final UUID roleId = roleDefinition.getId();
        apply(s->s.internalDeleteRoleDefinition(roleId));
    }

    @Override
    public Void internalDeleteRoleDefinition(UUID roleId) {
        final RoleDefinition role = userStore.getRoleDefinition(roleId);
        userStore.removeRoleDefinition(role);
        return null;
    }

    @Override
    public void updateRoleDefinition(RoleDefinition roleDefinitionWithNewProperties) {
        apply(s->s.internalUpdateRoleDefinition(roleDefinitionWithNewProperties));
    }

    @Override
    public Void internalUpdateRoleDefinition(RoleDefinition roleWithNewProperties) {
        final RoleDefinition role = userStore.getRoleDefinition(roleWithNewProperties.getId());
        role.setName(roleWithNewProperties.getName());
        userStore.setRoleDefinitionDisplayName(roleWithNewProperties.getId(), role.getName());
        role.setPermissions(roleWithNewProperties.getPermissions());
        userStore.setRoleDefinitionPermissions(role.getId(), role.getPermissions());
        return null;
    }

    @Override
    public Iterable<RoleDefinition> getRoleDefinitions() {
        return userStore.getRoleDefinitions();
    }

    @Override
    public void addRoleForUser(SecurityUser user, Role role) {
        addRoleForUser(user.getName(), role);
    }

    @Override
    public void addRoleForUser(String username, Role role) {
        final UUID roleDefinitionId = role.getRoleDefinition().getId();
        final UUID idOfTenantQualifyingRole = role.getQualifiedForTenant() == null ? null : role.getQualifiedForTenant().getId();
        final String nameOfUserQualifyingRole = role.getQualifiedForUser() == null ? null : role.getQualifiedForUser().getName();
        apply(s->s.internalAddRoleForUser(username, roleDefinitionId, idOfTenantQualifyingRole, nameOfUserQualifyingRole));
    }

    @Override
    public Void internalAddRoleForUser(String username, UUID roleDefinitionId, UUID idOfTenantQualifyingRole,
            String nameOfUserQualifyingRole) throws UserManagementException {
        userStore.addRoleForUser(username, new RoleImpl(getRoleDefinition(roleDefinitionId),
                getUserGroup(idOfTenantQualifyingRole), getUserByName(nameOfUserQualifyingRole)));
        return null;
    }

    @Override
    public void removeRoleFromUser(SecurityUser user, Role role) {
        removeRoleFromUser(user.getName(), role);
    }
    
    @Override
    public void removeRoleFromUser(String username, Role role) {
        final UUID roleDefinitionId = role.getRoleDefinition().getId();
        final UUID idOfTenantQualifyingRole = role.getQualifiedForTenant() == null ? null : role.getQualifiedForTenant().getId();
        final String nameOfUserQualifyingRole = role.getQualifiedForUser() == null ? null : role.getQualifiedForUser().getName();
        apply(s->s.internalRemoveRoleFromUser(username, roleDefinitionId, idOfTenantQualifyingRole, nameOfUserQualifyingRole));
    }

    @Override
    public Void internalRemoveRoleFromUser(String username, UUID roleDefinitionId, UUID idOfTenantQualifyingRole,
            String nameOfUserQualifyingRole) throws UserManagementException {
        userStore.removeRoleFromUser(username, new RoleImpl(getRoleDefinition(roleDefinitionId),
                getUserGroup(idOfTenantQualifyingRole), getUserByName(nameOfUserQualifyingRole)));
        return null;
    }

    @Override
    public Iterable<WildcardPermission> getPermissionsFromUser(String username) throws UserManagementException {
        return userStore.getPermissionsFromUser(username);
    }

    @Override
    public void removePermissionFromUser(String username, WildcardPermission permissionToRemove) {
        apply(s->s.internalRemovePermissionForUser(username, permissionToRemove));
    }

    @Override
    public Void internalRemovePermissionForUser(String username, WildcardPermission permissionToRemove) throws UserManagementException {
        userStore.removePermissionFromUser(username, permissionToRemove);
        return null;
    }

    @Override
    public void addPermissionForUser(String username, WildcardPermission permissionToAdd) {
        apply(s->s.internalAddPermissionForUser(username, permissionToAdd));
    }

    @Override
    public Void internalAddPermissionForUser(String username, WildcardPermission permissionToAdd) throws UserManagementException {
        userStore.addPermissionForUser(username, permissionToAdd);
        return null;
    }

    @Override
    public void deleteUser(String username) throws UserManagementException {
        apply(s->s.internalDeleteUser(username));
    }

    @Override
    public Void internalDeleteUser(String username) throws UserManagementException {
        userStore.deleteUser(username);
        return null;
    }

    @Override
    public boolean setSetting(String key, Object setting) {
        return apply(s->s.internalSetSetting(key, setting));
    }

    @Override
    public Boolean internalSetSetting(String key, Object setting) {
        return userStore.setSetting(key, setting);
    }

    @Override
    public <T> T getSetting(String key, Class<T> clazz) {
        return userStore.getSetting(key, clazz);
    }

    @Override
    public Map<String, Object> getAllSettings() {
        return userStore.getAllSettings();
    }

    @Override
    public Map<String, Class<?>> getAllSettingTypes() {
        return userStore.getAllSettingTypes();
    }

    @Override
    public SecurityUser createSocialUser(String name, SocialUserAccount socialUserAccount) throws UserManagementException, UserGroupManagementException {
        if (userStore.getUserByName(name) != null) {
            throw new UserManagementException(UserManagementException.USER_ALREADY_EXISTS);
        }
        UserGroup tenant = createUserGroup(UUID.randomUUID(), getDefaultTenantNameForUsername(name));
        SecurityUser result = userStore.createUser(name, socialUserAccount.getProperty(Social.EMAIL.name()), tenant, socialUserAccount);
        accessControlStore.setOwnership(SecuredSecurityTypes.USER_GROUP.getQualifiedObjectIdentifier(tenant.getId().toString()), result, tenant, tenant.getName());
        addUserToUserGroup(tenant, result);
        return result;
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
        User user = userStore.getUserByName(username);
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
                result = userStore.getUserByName(username);
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
                    .apiKey(userStore.getSetting(SocialSettingsKeys.OAUTH_FACEBOOK_APP_ID.name(), String.class))
                    .apiSecret(userStore.getSetting(SocialSettingsKeys.OAUTH_FACEBOOK_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.GOOGLE: {
            service = new ServiceBuilder().provider(GoogleApi.class)
                    .apiKey(userStore.getSetting(SocialSettingsKeys.OAUTH_GOOGLE_APP_ID.name(), String.class))
                    .apiSecret(userStore.getSetting(SocialSettingsKeys.OAUTH_GOOGLE_APP_SECRET.name(), String.class))
                    .scope(userStore.getSetting(SocialSettingsKeys.OAUTH_GOOGLE_SCOPE.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();

            break;
        }

        case ClientUtils.TWITTER: {
            service = new ServiceBuilder().provider(TwitterApi.class)
                    .apiKey(userStore.getSetting(SocialSettingsKeys.OAUTH_TWITTER_APP_ID.name(), String.class))
                    .apiSecret(userStore.getSetting(SocialSettingsKeys.OAUTH_TWITTER_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }
        case ClientUtils.YAHOO: {
            service = new ServiceBuilder().provider(YahooApi.class)
                    .apiKey(userStore.getSetting(SocialSettingsKeys.OAUTH_YAHOO_APP_ID.name(), String.class))
                    .apiSecret(userStore.getSetting(SocialSettingsKeys.OAUTH_YAHOO_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.LINKEDIN: {
            service = new ServiceBuilder().provider(LinkedInApi.class)
                    .apiKey(userStore.getSetting(SocialSettingsKeys.OAUTH_LINKEDIN_APP_ID.name(), String.class))
                    .apiSecret(userStore.getSetting(SocialSettingsKeys.OAUTH_LINKEDIN_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.INSTAGRAM: {
            service = new ServiceBuilder().provider(InstagramApi.class)
                    .apiKey(userStore.getSetting(SocialSettingsKeys.OAUTH_INSTAGRAM_APP_ID.name(), String.class))
                    .apiSecret(userStore.getSetting(SocialSettingsKeys.OAUTH_INSTAGRAM_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.GITHUB: {
            service = new ServiceBuilder().provider(GithubApi.class)
                    .apiKey(userStore.getSetting(SocialSettingsKeys.OAUTH_GITHUB_APP_ID.name(), String.class))
                    .apiSecret(userStore.getSetting(SocialSettingsKeys.OAUTH_GITHUB_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;

        }

        case ClientUtils.IMGUR: {
            service = new ServiceBuilder().provider(ImgUrApi.class)
                    .apiKey(userStore.getSetting(SocialSettingsKeys.OAUTH_IMGUR_APP_ID.name(), String.class))
                    .apiSecret(userStore.getSetting(SocialSettingsKeys.OAUTH_IMGUR_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.FLICKR: {
            service = new ServiceBuilder().provider(FlickrApi.class)
                    .apiKey(userStore.getSetting(SocialSettingsKeys.OAUTH_FLICKR_APP_ID.name(), String.class))
                    .apiSecret(userStore.getSetting(SocialSettingsKeys.OAUTH_FLICKR_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.VIMEO: {
            service = new ServiceBuilder().provider(VimeoApi.class)
                    .apiKey(userStore.getSetting(SocialSettingsKeys.OAUTH_VIMEO_APP_ID.name(), String.class))
                    .apiSecret(userStore.getSetting(SocialSettingsKeys.OAUTH_VIMEO_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.WINDOWS_LIVE: {
            // a Scope must be specified
            service = new ServiceBuilder().provider(LiveApi.class)
                    .apiKey(userStore.getSetting(SocialSettingsKeys.OAUTH_WINDOWS_LIVE_APP_ID.name(), String.class))
                    .apiSecret(userStore.getSetting(SocialSettingsKeys.OAUTH_WINDOWS_LIVE_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).scope("wl.basic").build();
            break;
        }

        case ClientUtils.TUMBLR: {
            service = new ServiceBuilder().provider(TumblrApi.class)
                    .apiKey(userStore.getSetting(SocialSettingsKeys.OAUTH_TUMBLR_LIVE_APP_ID.name(), String.class))
                    .apiSecret(userStore.getSetting(SocialSettingsKeys.OAUTH_TUMBLR_LIVE_APP_SECRET.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.FOURSQUARE: {
            service = new ServiceBuilder().provider(Foursquare2Api.class)
                    .apiKey(userStore.getSetting(SocialSettingsKeys.OAUTH_FOURSQUARE_APP_ID.name(), String.class))
                    .apiSecret(userStore.getSetting(SocialSettingsKeys.OAUTH_FOURSQUARE_APP_SECRET.name(), String.class))
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
        userStore.addSetting(key, clazz);
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
        if (!subject.hasRole(AdminRole.getInstance().getName()) && (subject.getPrincipal() == null
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
        userStore.setPreference(username, key, value);
        return null;
    }
    
    @Override
    public String internalSetPreferenceObject(final String username, final String key, final Object value) {
        return userStore.setPreferenceObject(username, key, value);
    }
    
    @Override
    public void unsetPreference(String username, String key) {
        ensureThatUserInQuestionIsLoggedInOrCurrentUserIsAdmin(username);
        apply(s->s.internalUnsetPreference(username, key));
    }

    @Override
    public Void internalUnsetPreference(String username, String key) {
        userStore.unsetPreference(username, key);
        return null;
    }

    @Override
    public Void internalSetAccessToken(String username, String accessToken) {
        userStore.setAccessToken(username, accessToken);
        return null;
    }
    
    @Override
    public String getAccessToken(String username) {
        return userStore.getAccessToken(username);
    }

    @Override
    public String getOrCreateAccessToken(String username) {
        String result = userStore.getAccessToken(username);
        if (result == null) {
            result = createAccessToken(username);
        }
        return result;
    }

    @Override
    public Void internalRemoveAccessToken(String username) {
        userStore.removeAccessToken(username);
        return null;
    }

    @Override
    public String getPreference(String username, String key) {
        ensureThatUserInQuestionIsLoggedInOrCurrentUserIsAdmin(username);
        return userStore.getPreference(username, key);
    }

    @Override
    public Map<String, String> getAllPreferences(String username) {
        ensureThatUserInQuestionIsLoggedInOrCurrentUserIsAdmin(username);
        return userStore.getAllPreferences(username);
    }
    
    @Override
    public String createAccessToken(String username) {
        SecurityUser user = getUserByName(username);
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
        if (subject.hasRole(AdminRole.getInstance().getName()) || username.equals(subject.getPrincipal().toString())) {
            apply(s -> s.internalRemoveAccessToken(username));
        } else {
            throw new org.apache.shiro.authz.AuthorizationException("User " + subject.getPrincipal().toString()
                    + " does not have permission to remove access token of user " + username);
        }
    }

    // ----------------- Replication -------------
    @Override
    public void clearReplicaState() throws MalformedURLException, IOException, InterruptedException {
        userStore.clear();
        accessControlStore.clear();
    }

    @Override
    public Serializable getId() {
        return getClass().getName();
    }
    
    @Override
    public ObjectInputStream createObjectInputStreamResolvingAgainstCache(InputStream is) throws IOException {
        return new ObjectInputStreamResolvingAgainstSecurityCache(is, userStore);
    }

    @Override
    public void initiallyFillFromInternal(ObjectInputStream is) throws IOException, ClassNotFoundException,
            InterruptedException {
        ReplicatingCacheManager newCacheManager = (ReplicatingCacheManager) is.readObject();
        cacheManager.replaceContentsFrom(newCacheManager);
        // overriding thread context class loader because the user store may be provided by a different bundle;
        // We're assuming here that the user store service is provided by the same bundle in the replica as on the master.
        ClassLoader oldCCL = Thread.currentThread().getContextClassLoader();
        if (userStore != null) {
            Thread.currentThread().setContextClassLoader(userStore.getClass().getClassLoader());
        }
        try {
            UserStore newUserStore = (UserStore) is.readObject();
            userStore.replaceContentsFrom(newUserStore);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCCL);
        }
        if (accessControlStore != null) {
            Thread.currentThread().setContextClassLoader(accessControlStore.getClass().getClassLoader());
        }
        try {
            AccessControlStore newAccessControlStore = (AccessControlStore) is.readObject();
            accessControlStore.replaceContentsFrom(newAccessControlStore);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCCL);
        }
    }

    @Override
    public void serializeForInitialReplicationInternal(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.writeObject(cacheManager);
        objectOutputStream.writeObject(userStore);
        objectOutputStream.writeObject(accessControlStore);
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
        userStore.clear();
        accessControlStore.clear();
        initEmptyStore();
        initEmptyAccessControlStore();
        CacheManager cm = getSecurityManager().getCacheManager();
        if (cm instanceof ReplicatingCacheManager) {
            ((ReplicatingCacheManager) cm).clear();
        }
    }

    @Override
    public UserGroup getDefaultTenant() {
        return userStore.getDefaultTenant();
    }

    @Override
    public <T> T setOwnershipCheckPermissionAndRevertOnError(String tenantOwnerName, HasPermissions type,
            String typeIdentifier, com.sap.sse.security.shared.HasPermissions.Action action, String securityDisplayName,
            ActionWithResult<T> actionWithResult) {
        final UserGroup group = getUserGroupByName(tenantOwnerName);

        return setOwnershipCheckPermissionAndRevertOnError(group, type, typeIdentifier, action, securityDisplayName,
                actionWithResult);
    }

    @Override
    public <T> T setOwnershipCheckPermissionAndRevertOnError(UserGroup tenantOwner, HasPermissions type,
            String typeIdentifier, com.sap.sse.security.shared.HasPermissions.Action action, String securityDisplayName,
            ActionWithResult<T> actionWithResult) {
        QualifiedObjectIdentifier identifier = type.getQualifiedObjectIdentifier(typeIdentifier);
        T result = null;
        boolean didSetOwnerShip = false;
        try {
            final User user = getUserByName((String) SecurityUtils.getSubject().getPrincipal());
            if (getOwnership(identifier) == null) {
                didSetOwnerShip = true;
                setOwnership(identifier, user, tenantOwner, securityDisplayName);
            }
            SecurityUtils.getSubject().checkPermission(type.getStringPermissionForObjects(action, typeIdentifier));
            result = actionWithResult.run();
        } catch (AuthorizationException e) {
            if (didSetOwnerShip) {
                deleteOwnership(identifier);
            }
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
