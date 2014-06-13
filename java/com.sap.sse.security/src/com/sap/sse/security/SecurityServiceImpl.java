package com.sap.sse.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
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
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
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
import com.sap.sse.security.userstore.shared.FieldNames.Social;
import com.sap.sse.security.userstore.shared.SocialSettingsKeys;
import com.sap.sse.security.userstore.shared.SocialUserAccount;
import com.sap.sse.security.userstore.shared.User;
import com.sap.sse.security.userstore.shared.UserManagementException;
import com.sap.sse.security.userstore.shared.UserStore;
import com.sap.sse.security.userstore.shared.UsernamePasswordAccount;

public class SecurityServiceImpl  extends RemoteServiceServlet implements SecurityService {
    
    private static final long serialVersionUID = -3490163216601311858L;
    
    private static final Logger logger = Logger.getLogger(SecurityServiceImpl.class.getName());
    
    private SecurityManager securityManager;
    private UserStore store;
    
    public SecurityServiceImpl() {
        
        BundleContext context = Activator.getContext();
        ServiceReference<?> serviceReference = context.
                getServiceReference(UserStore.class.getName());
        store = (UserStore) context.
                getService(serviceReference);
        
        //Create default users if no users exist yet.
        if (store.getUserCollection().isEmpty()){
            try {
                logger.info("No users found, creating default users!");
                createSimpleUser("Ben", "Ben@sapsailing.com", "ben123");
                addRoleForUser("Ben", "admin");
                addRoleForUser("Ben", "moderator");
                createSimpleUser("Peter", "Peter@sapsailing.com", "peter123");
                addRoleForUser("Peter", "moderator");
                createSimpleUser("Hans", "Hans@sapsailing.com", "hans123");
                createSimpleUser("Hubert", "Hubert@sapsailing.com", "hubert123");
                createSimpleUser("Franz", "Franz@sapsailing.com", "franz123");
            } catch (UserManagementException e) {
                e.printStackTrace();
            }
        }
        
        
        Ini ini = new Ini();
        ini.loadFromPath("classpath:shiro.ini");
        Map<String, Class<?>> allSettingTypes = store.getAllSettingTypes();
        for (Entry<String, Class<?>> e : allSettingTypes.entrySet()){
            String[] classifier = e.getKey().split("_");
            if (classifier[0].equals("URLS") && !classifier[1].equals("AUTH")){
                String key = store.getSetting(e.getKey(), String.class);
                String n = classifier[0] + "_AUTH";
                for (int i = 1; i < classifier.length; i++){
                    n += "_" + classifier[i];
                }
                String value = store.getSetting(n, String.class);
                if (ini.getSection("urls") == null){
                    ini.addSection("urls");
                }
                ini.getSection("urls").put(key, value);
            }
        }
        for (Entry<String, String> e : ini.getSection("urls").entrySet()){
            System.out.println(e.getKey() + ": " + e.getValue());
        }
//        ini.getSection("urls").put("", "");
        Factory<SecurityManager> factory = new IniSecurityManagerFactory(ini);
//        LifecycleUtils.init(ini);
        
        
        logger.info("Loaded shiro.ini file from: classpath:shiro.ini");
        System.setProperty("java.net.useSystemProxies", "true");
        SecurityManager securityManager = factory.getInstance();
        logger.info("Created: " + securityManager);
        SecurityUtils.setSecurityManager(securityManager);
        this.securityManager = securityManager;
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
        if (savedRequest != null){
            redirectUrl = savedRequest.getRequestUrl();
        }
        else {
            redirectUrl = "";
        }
        System.out.println("Redirecturl: " + redirectUrl);
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
    public User createSimpleUser(String name, String email, String password) throws UserManagementException {
        if (store.getUserByName(name) != null){
            throw new UserManagementException(UserManagementException.USER_ALREADY_EXISTS);
        }
        if (name == null || password == null || name.length() < 3 || password.length() < 5){
            throw new UserManagementException(UserManagementException.INVALID_CREDENTIALS);
        }
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        Object salt = rng.nextBytes();
        String hashedPasswordBase64 = new Sha256Hash(password, salt, 1024).toBase64();
        UsernamePasswordAccount upa = new UsernamePasswordAccount(name, hashedPasswordBase64, salt);
        return store.createUser(name, email, upa);
    }

    @Override
    public Set<String> getRolesFromUser(String name) throws UserManagementException {
        return store.getRolesFromUser(name);
    }

    @Override
    public void addRoleForUser(String name, String role) throws UserManagementException {
        store.addRoleForUser(name, role);
    }

    @Override
    public void removeRoleFromUser(String name, String role) throws UserManagementException {
        store.removeRoleFromUser(name, role);
    }

    @Override
    public void deleteUser(String username) throws UserManagementException {
        store.deleteUser(username);
    }

    @Override
    public void setSettings(String key, Object setting) {
        store.setSetting(key, setting);
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
        if (store.getUserByName(name) != null){
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
                logger.log(Level.SEVERE,ae.getLocalizedMessage());
                throw new UserManagementException("An error occured while authenticating the user!");
            }
        }
        String username = SessionUtils.loadUsername();
        if (username == null){
            logger.info("Something went wrong while authneticating, check doGetAuthenticationInfo() in " + OAuthRealm.class.getName() + ".");
            throw new UserManagementException("An error occured while authenticating the user!");
        }
        User user = store.getUserByName(username);
        if (user == null){
            logger.info("Could not find user " + username);
            throw new UserManagementException("An error occured while authenticating the user!");
        }
        return user;
    }

    @Override
    public User getCurrentUser() {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null) {
            return null;
        }
        String username = SessionUtils.loadUsername();
        if (username == null || username.length() <= 0){
            return null;
        }
        return store.getUserByName(username);
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
                throw new UserManagementException("Could not get request token for " + authProvider + " " + e.getMessage());
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
            throw new  UserManagementException("Could not get Authorization url: ");
        }

        if (authProvider == ClientUtils.FLICKR) {
            authorizationUrl += "&perms=read";
        }
        
        if (authProvider == ClientUtils.FACEBOOK){
            authorizationUrl += "&scope=email";
        }

        logger.info("Authorization url: " + authorizationUrl);
        return authorizationUrl;
    }

    private OAuthService getOAuthService(int authProvider) {
        OAuthService service = null;
        switch (authProvider) {
        case ClientUtils.FACEBOOK: {
            service = new ServiceBuilder().provider(FacebookApi.class).apiKey(store.getSetting(SocialSettingsKeys.OAUTH_FACEBOOK_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_FACEBOOK_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.GOOGLE: {
            service = new ServiceBuilder().provider(GoogleApi.class).apiKey(store.getSetting(SocialSettingsKeys.OAUTH_GOOGLE_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_GOOGLE_APP_SECRET.name(), String.class)).scope(store.getSetting(SocialSettingsKeys.OAUTH_GOOGLE_SCOPE.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();

            break;
        }

        case ClientUtils.TWITTER: {
            service = new ServiceBuilder().provider(TwitterApi.class).apiKey(store.getSetting(SocialSettingsKeys.OAUTH_TWITTER_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_TWITTER_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }
        case ClientUtils.YAHOO: {
            service = new ServiceBuilder().provider(YahooApi.class).apiKey(store.getSetting(SocialSettingsKeys.OAUTH_YAHOO_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_YAHOO_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.LINKEDIN: {
            service = new ServiceBuilder().provider(LinkedInApi.class).apiKey(store.getSetting(SocialSettingsKeys.OAUTH_LINKEDIN_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_LINKEDIN_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.INSTAGRAM: {
            service = new ServiceBuilder().provider(InstagramApi.class).apiKey(store.getSetting(SocialSettingsKeys.OAUTH_INSTAGRAM_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_INSTAGRAM_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.GITHUB: {
            service = new ServiceBuilder().provider(GithubApi.class).apiKey(store.getSetting(SocialSettingsKeys.OAUTH_GITHUB_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_GITHUB_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;

        }

        case ClientUtils.IMGUR: {
            service = new ServiceBuilder().provider(ImgUrApi.class).apiKey(store.getSetting(SocialSettingsKeys.OAUTH_IMGUR_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_IMGUR_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.FLICKR: {
            service = new ServiceBuilder().provider(FlickrApi.class).apiKey(store.getSetting(SocialSettingsKeys.OAUTH_FLICKR_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_FLICKR_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.VIMEO: {
            service = new ServiceBuilder().provider(VimeoApi.class).apiKey(store.getSetting(SocialSettingsKeys.OAUTH_VIMEO_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_VIMEO_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.WINDOWS_LIVE: {
            // a Scope must be specified
            service = new ServiceBuilder().provider(LiveApi.class).apiKey(store.getSetting(SocialSettingsKeys.OAUTH_WINDOWS_LIVE_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_WINDOWS_LIVE_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl())
                    .scope("wl.basic").build();
            break;
        }

        case ClientUtils.TUMBLR: {
            service = new ServiceBuilder().provider(TumblrApi.class).apiKey(store.getSetting(SocialSettingsKeys.OAUTH_TUMBLR_LIVE_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_TUMBLR_LIVE_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.FOURSQUARE: {
            service = new ServiceBuilder().provider(Foursquare2Api.class).apiKey(store.getSetting(SocialSettingsKeys.OAUTH_FOURSQUARE_APP_ID.name(), String.class))
                    .apiSecret(store.getSetting(SocialSettingsKeys.OAUTH_FOURSQUARE_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        default: {
            return null;
        }

        }
        return service;
    }

    @Override
    public Iterable<String> getUrls(ServletContext context) {
        WebEnvironment env = WebUtils.getRequiredWebEnvironment(context);
        FilterChainResolver resolver = env.getFilterChainResolver();
        if (resolver instanceof PathMatchingFilterChainResolver){
            PathMatchingFilterChainResolver pmfcr = (PathMatchingFilterChainResolver) resolver;
            FilterChainManager filterChainManager = pmfcr.getFilterChainManager();
            System.out.println("Filters:");
            for (Entry<String, Filter> e : filterChainManager.getFilters().entrySet()){
                System.out.println(e.getKey() + ": " + e.getValue().toString());
            }
            System.out.println("Chains");
            filterChainManager.createChain("/Register.html", "authc");
            for (String s : filterChainManager.getChainNames()){
                System.out.println(s);
                System.out.println(Arrays.toString(filterChainManager.getChain(s).toArray(new Filter[0])));
                
            }
        }
        return null;
    }
}
