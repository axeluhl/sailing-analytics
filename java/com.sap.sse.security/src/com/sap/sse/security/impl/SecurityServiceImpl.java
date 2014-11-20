package com.sap.sse.security.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sse.common.Util;
import com.sap.sse.replication.OperationExecutionListener;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.security.ClientUtils;
import com.sap.sse.security.Credential;
import com.sap.sse.security.GithubApi;
import com.sap.sse.security.InstagramApi;
import com.sap.sse.security.OAuthRealm;
import com.sap.sse.security.OAuthToken;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.SessionUtils;
import com.sap.sse.security.Social;
import com.sap.sse.security.SocialSettingsKeys;
import com.sap.sse.security.User;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.operations.SecurityOperation;
import com.sap.sse.security.shared.Account.AccountType;
import com.sap.sse.security.shared.DefaultRoles;
import com.sap.sse.security.shared.MailException;
import com.sap.sse.security.shared.SocialUserAccount;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.UsernamePasswordAccount;

public class SecurityServiceImpl extends RemoteServiceServlet implements SecurityService {

    private static final long serialVersionUID = -3490163216601311858L;

    private static final Logger logger = Logger.getLogger(SecurityServiceImpl.class.getName());

    private SecurityManager securityManager;
    private final CacheManager cacheManager = new EhCacheManager();
    private final UserStore store;
    private final Properties mailProperties;
    
    private static Ini shiroConfiguration;
    static {
        shiroConfiguration = new Ini();
        shiroConfiguration.loadFromPath("classpath:shiro.ini");
    }

    /**
     * @param mailProperties must not be <code>null</code>
     */
    public SecurityServiceImpl(UserStore store, Properties mailProperties) {
        assert mailProperties != null;
        logger.info("Initializing Security Service with user store " + store+" and mail properties "+mailProperties);
        this.store = store;
        this.mailProperties = mailProperties;
        // Create default users if no users exist yet.
        if (store.getUserCollection().isEmpty()) {
            try {
                logger.info("No users found, creating default user \"admin\" with password \"admin\"");
                createSimpleUser("admin", "nobody@sapsailing.com", "admin", /* validationBaseURL */ null);
                addRoleForUser("admin", DefaultRoles.ADMIN.getRolename());
            } catch (UserManagementException | MailException e) {
                logger.log(Level.SEVERE, "Exception while creating default admin user", e);
            }
        }
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
        SecurityManager securityManager = factory.getInstance();
        logger.info("Created: " + securityManager);
        SecurityUtils.setSecurityManager(securityManager);
        this.securityManager = securityManager;
    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
           String username = mailProperties.getProperty("mail.smtp.user");
           String password = mailProperties.getProperty("mail.smtp.password");
           return new PasswordAuthentication(username, password);
        }
    }

    @Override
    public void sendMail(String username, String subject, String body) throws MailException {
        final User user = getUserByName(username);
        if (user != null) {
            final String toAddress = user.getEmail();
            if (toAddress != null) {
                Session session = Session.getInstance(this.mailProperties, new SMTPAuthenticator());
                MimeMessage msg = new MimeMessage(session);
                try {
                    msg.setFrom(new InternetAddress(mailProperties.getProperty("mail.from", "root@sapsailing.com")));
                    msg.setSubject(subject);
                    msg.setContent(body, "text/plain");
                    msg.addRecipient(RecipientType.TO, new InternetAddress(toAddress.trim()));
                    Transport ts = session.getTransport();
                    ts.connect();
                    ts.sendMessage(msg, msg.getRecipients(RecipientType.TO));
                    ts.close();
                    logger.info("mail sent to user "+username+" with e-mail address "+toAddress+" with subject "+subject);
                } catch (MessagingException e) {
                    logger.log(Level.SEVERE, "Error trying to send mail to user "+username+" with e-mail address "+toAddress, e);
                    throw new MailException(e.getMessage());
                }
            }
        }
    }
    
    @Override
    public void resetPassword(final String username, String baseURL) throws UserManagementException, MailException {
        final User user = store.getUserByName(username);
        if (user == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        if (!user.isEmailValidated()) {
            throw new UserManagementException(UserManagementException.CANNOT_RESET_PASSWORD_WITHOUT_VALIDATED_EMAIL);
        }
        final String passwordResetSecret = user.startPasswordReset();
        store.updateUser(user); // durably storing the password reset secret
        Map<String, String> urlParameters = new HashMap<>();
        try {
            urlParameters.put("u", URLEncoder.encode(user.getName(), "UTF-8"));
            urlParameters.put("e", URLEncoder.encode(user.getEmail(), "UTF-8"));
            urlParameters.put("s", URLEncoder.encode(passwordResetSecret, "UTF-8"));
            final StringBuilder url = buildURL(baseURL, urlParameters);
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
    public SecurityManager getSecurityManager() {
        return this.securityManager;
    }

    @Override
    public Collection<User> getUserList() {
        return store.getUserCollection();
    }

    @Override
    public String login(String username, String password) throws AuthenticationException {
        String redirectUrl;
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        logger.info("Trying to login: " + username);
        Subject subject = SecurityUtils.getSubject();
        subject.login(token);
        SessionUtils.saveUsername(username);
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
    public User getUserByEmail(String email) {
        return store.getUserByEmail(email);
    }

    @Override
    public User createSimpleUser(final String username, final String email, String password,
            final String validationBaseURL) throws UserManagementException, MailException {
        if (store.getUserByName(username) != null) {
            throw new UserManagementException(UserManagementException.USER_ALREADY_EXISTS);
        }
        if (username == null || username.length() < 3) {
            throw new UserManagementException(UserManagementException.USERNAME_DOES_NOT_MEET_REQUIREMENTS);
        } else if (password == null || password.length() < 5) {
            throw new UserManagementException(UserManagementException.PASSWORD_DOES_NOT_MEET_REQUIREMENTS);
        }
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        Object salt = rng.nextBytes();
        String hashedPasswordBase64 = hashPassword(password, salt);
        UsernamePasswordAccount upa = new UsernamePasswordAccount(username, hashedPasswordBase64, salt);
        final User result = store.createUser(username, email, upa);
        store.updateUser(result); // store user with unvalidated e-mail
        if (validationBaseURL != null) {
            new Thread("e-mail validation for user " + username + " with e-mail address " + email) {
                @Override
                public void run() {
                    try {
                        startEmailValidation(result, validationBaseURL);
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
        Object salt = rng.nextBytes();
        String hashedPasswordBase64 = hashPassword(newPassword, salt);
        account.setSalt(salt);
        account.setSaltedPassword(hashedPasswordBase64);
        user.passwordWasReset();
        store.updateUser(user);
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
        user.setEmail(newEmail);
        new Thread("e-mail validation after changing e-mail of user " + username + " to " + newEmail) {
            @Override
            public void run() {
                try {
                    startEmailValidation(user, validationBaseURL);
                } catch (MailException e) {
                    logger.log(Level.SEVERE, "Error sending mail to validate e-mail address change for user "
                            + username + " to address " + newEmail, e);
                }
            }
        }.start();
        store.updateUser(user);
    }
    
    @Override
    public boolean validateEmail(String username, String validationSecret) throws UserManagementException {
        final User user = store.getUserByName(username);
        if (user == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        final boolean result = user.validate(validationSecret);
        store.updateUser(user);
        return result;
    }

    /**
     * {@link User#startEmailValidation() Triggers} e-mail validation for the <code>user</code> object and sends out a
     * URL to the user's e-mail that has the validation secret ready for validation by clicking.
     * 
     * @param baseURL
     *            the URL under which the user can reach the e-mail validation service; this URL is required to assemble a
     *            validation URL that is sent by e-mail to the user, to make the user return the validation secret
     *            to the right server again.
     */
    private void startEmailValidation(User user, String baseURL) throws MailException {
        String secret = user.startEmailValidation();
        try {
            Map<String, String> urlParameters = new HashMap<>();
            urlParameters.put("u", URLEncoder.encode(user.getName(), "UTF-8"));
            urlParameters.put("v", URLEncoder.encode(secret, "UTF-8"));
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
        boolean first = !baseURL.contains("?");
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
    public Set<String> getRolesFromUser(String name) throws UserManagementException {
        return store.getRolesFromUser(name);
    }

    @Override
    public void addRoleForUser(String username, String role) throws UserManagementException {
        store.addRoleForUser(username, role);
    }

    @Override
    public void removeRoleFromUser(String username, String role) throws UserManagementException {
        store.removeRoleFromUser(username, role);
    }

    @Override
    public void deleteUser(String username) throws UserManagementException {
        store.deleteUser(username);
    }

    @Override
    public boolean setSetting(String key, Object setting) {
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
                logger.info("User [" + SessionUtils.loadUsername() + "] logged in successfully.");
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
        String username = SessionUtils.loadUsername();
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
        if (subject == null) {
            result = null;
        } else {
            String username = SessionUtils.loadUsername();
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
        store.addSetting(key, clazz);
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
    public CacheManager getCacheManager() {
        return cacheManager;
    }

    @Override
    public void setPreference(String username, String key, String value) {
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole(DefaultRoles.ADMIN.name()) || username.equals(SessionUtils.loadUsername())) {
            store.setPreference(username, key, value);
        } else {
            throw new SecurityException("User " + SessionUtils.loadUsername()
                    + " does not have permission to set preference for user " + username);
        }
    }
    
    @Override
    public void unsetPreference(String username, String key) {
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole(DefaultRoles.ADMIN.name()) || username.equals(SessionUtils.loadUsername())) {
            store.unsetPreference(username, key);
        } else {
            throw new SecurityException("User " + SessionUtils.loadUsername()
                    + " does not have permission to unset preference for user " + username);
        }
    }

    @Override
    public String getPreference(String username, String key) {
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole(DefaultRoles.ADMIN.name()) || username.equals(SessionUtils.loadUsername())) {
            return store.getPreference(username, key);
        } else {
            throw new SecurityException("User " + SessionUtils.loadUsername()
                    + " does not have permission to read preferences of user " + username);
        }
    }

    // ----------------- Replication -------------
    @Override
    public <T> T apply(OperationWithResult<SecurityService, T> operation) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addOperationExecutionListener(OperationExecutionListener<SecurityService> listener) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeOperationExecutionListener(OperationExecutionListener<SecurityService> listener) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void clearReplicaState() throws MalformedURLException, IOException, InterruptedException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void initiallyFillFrom(InputStream is) throws IOException, ClassNotFoundException, InterruptedException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void serializeForInitialReplication(OutputStream os) throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public SecurityOperation<?> readOperation(InputStream inputStream) throws IOException, ClassNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void writeOperation(OperationWithResult<?, ?> operation, OutputStream outputStream, boolean closeStream)
            throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Serializable getId() {
        return getClass().getName();
    }
    
}
