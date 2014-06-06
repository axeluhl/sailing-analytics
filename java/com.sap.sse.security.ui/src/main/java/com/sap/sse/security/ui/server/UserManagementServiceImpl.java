package com.sap.sse.security.ui.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
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
import com.sap.sse.security.Credential;
import com.sap.sse.security.GithubApi;
import com.sap.sse.security.InstagramApi;
import com.sap.sse.security.OAuthToken;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.SessionUtils;
import com.sap.sse.security.ui.Activator;
import com.sap.sse.security.ui.oauth.client.CredentialDTO;
import com.sap.sse.security.ui.oauth.client.SocialUserDTO;
import com.sap.sse.security.ui.oauth.client.util.ClientUtils;
import com.sap.sse.security.ui.oauth.shared.OAuthException;
import com.sap.sse.security.ui.shared.AccountDTO;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UsernamePasswordAccountDTO;
import com.sap.sse.security.userstore.shared.Account;
import com.sap.sse.security.userstore.shared.Account.AccountType;
import com.sap.sse.security.userstore.shared.SocialSettingsKeys;
import com.sap.sse.security.userstore.shared.SocialUserAccount;
import com.sap.sse.security.userstore.shared.User;
import com.sap.sse.security.userstore.shared.UserManagementException;
import com.sap.sse.security.userstore.shared.UserStore;
import com.sap.sse.security.userstore.shared.UsernamePasswordAccount;

public class UserManagementServiceImpl extends RemoteServiceServlet implements UserManagementService {

    private static final long serialVersionUID = 4458564336368629101L;
    
    private static final Logger logger = Logger.getLogger(UserManagementServiceImpl.class.getName());

    private final BundleContext context;
    private SecurityService securityService;

    public UserManagementServiceImpl() {
        context = Activator.getContext();
        ServiceReference<?> serviceReference = context.getServiceReference(SecurityService.class.getName());
        securityService = (SecurityService) context.getService(serviceReference);
        SecurityUtils.setSecurityManager(securityService.getSecurityManager());
    }

    @Override
    public String sayHello() {
        return "Hello";
    }

    @Override
    public Collection<UserDTO> getUserList() {
        List<UserDTO> users = new ArrayList<>();
        for (User u : securityService.getUserList()) {
            UserDTO userDTO = createUserDTOFromUser(u);
            users.add(userDTO);
        }
        return users;
    }

    @Override
    public UserDTO getCurrentUser() {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null) {
            return null;
        }
        Object principal = subject.getPrincipal();
        UserDTO userDTO;
        if (principal != null) {
            String username = SessionUtils.loadUsername();
            if (username == null || username.length() < 1){
                return null;
            }
            User u = securityService.getUserByName(username);
            userDTO = createUserDTOFromUser(u);
        } else {
            userDTO = null;
        }
        return userDTO;
    }

    @Override
    public SuccessInfo login(String username, String password) {
        try {
            return new SuccessInfo(true, securityService.login(username, password));
        } catch (AuthenticationException e) {
            return new SuccessInfo(false, "Failed to login.");
        }
    }

    @Override
    public SuccessInfo logout() {
        securityService.logout();
        getHttpSession().invalidate();
        logger.info("Invalidated HTTP session");
        return new SuccessInfo(true, "Logged out.");
    }

    @Override
    public UserDTO createSimpleUser(String name, String email, String password) {
        User u = null;
        try {
            u = securityService.createSimpleUser(name, email, password);
        } catch (UserManagementException e) {
            e.printStackTrace();
        }
        if (u == null) {
            return null;
        }
        return createUserDTOFromUser(u);
    }

    @Override
    public Collection<UserDTO> getFilteredSortedUserList(String filter) {
        List<UserDTO> users = new ArrayList<>();
        for (User u : securityService.getUserList()) {
            if (filter != null && !"".equals(filter)) {
                if (u.getName().contains(filter)) {
                    users.add(createUserDTOFromUser(u));
                }
            } else {
                users.add(createUserDTOFromUser(u));
            }
        }

        Collections.sort(users, new Comparator<UserDTO>() {

            @Override
            public int compare(UserDTO u1, UserDTO u2) {
                return u1.getName().compareTo(u2.getName());
            }
        });
        return users;
    }

    @Override
    public SuccessInfo addRoleForUser(String username, String role) {
        Subject currentUser = SecurityUtils.getSubject();

        if (currentUser.hasRole(UserStore.DefaultRoles.ADMIN.getName())) {
            User u = securityService.getUserByName(username);
            if (u == null) {
                return new SuccessInfo(false, "User does not exist.");
            }
            try {
                securityService.addRoleForUser(username, role);
                return new SuccessInfo(true, "Added role: " + role + ".");
            } catch (UserManagementException e) {
                return new SuccessInfo(false, e.getMessage());
            }
        } else {
            return new SuccessInfo(false, "You don't have the required permissions to add a role.");
        }
    }

    @Override
    public SuccessInfo deleteUser(String username) {
        try {
            securityService.deleteUser(username);
            return new SuccessInfo(true, "Deleted user: " + username + ".");
        } catch (UserManagementException e) {
            return new SuccessInfo(false, "Could not delete user.");
        }
    }

    private UserDTO createUserDTOFromUser(User user){
        UserDTO userDTO;
        Map<AccountType, Account> accounts = user.getAllAccounts();
        List<AccountDTO> accountDTOs = new ArrayList<>();
        int i = 0;
        for (Account a : accounts.values()){
            switch (a.getAccountType()) {
            case SOCIAL_USER:
                accountDTOs.add(createSocialUserDTO((SocialUserAccount) a));
                break;

            default:
                UsernamePasswordAccount upa = (UsernamePasswordAccount) a;
                accountDTOs.add(new UsernamePasswordAccountDTO(upa.getName(), upa.getSaltedPassword(), ((ByteSource) upa.getSalt()).getBytes()));
                break;
            }
            i++;
        }
        userDTO = new UserDTO(user.getName(), accountDTOs);
        userDTO.addRoles(user.getRoles());
        return userDTO;
    }

    @Override
    public Map<String, String> getSettings() {
        Map<String, String> settings = new TreeMap<String, String>();
        for (Entry<String, Object> e : securityService.getAllSettings().entrySet()){
            settings.put(e.getKey(), e.getValue().toString());
        }
        return settings;
    }

    @Override
    public void setSetting(String key, String clazz, String setting) {
        if (clazz.equals(Boolean.class.getName())){
            securityService.setSettings(key, Boolean.parseBoolean(setting));
        }
        else if (clazz.equals(Integer.class.getName())){
            securityService.setSettings(key, Integer.parseInt(setting));
        }
        else {
            securityService.setSettings(key, setting);
        }
    }

    @Override
    public Map<String, String> getSettingTypes() {
        Map<String, String> settingTypes = new TreeMap<String, String>();
        for (Entry<String, Class<?>> e : securityService.getAllSettingTypes().entrySet()){
            settingTypes.put(e.getKey(), e.getValue().getName());
        }
        return settingTypes;
    }
    
    
    
    
    
    
    
    //--------------------------------------------------------- OAuth Implementations -------------------------------------------------------------------------
    private OAuthService getOAuthService(int authProvider) {
        OAuthService service = null;
        switch (authProvider) {
        case ClientUtils.FACEBOOK: {
            service = new ServiceBuilder().provider(FacebookApi.class).apiKey(securityService.getSetting(SocialSettingsKeys.FACEBOOK_APP_ID.name(), String.class))
                    .apiSecret(securityService.getSetting(SocialSettingsKeys.FACEBOOK_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.GOOGLE: {
            service = new ServiceBuilder().provider(GoogleApi.class).apiKey(securityService.getSetting(SocialSettingsKeys.GOOGLE_APP_ID.name(), String.class))
                    .apiSecret(securityService.getSetting(SocialSettingsKeys.GOOGLE_APP_SECRET.name(), String.class)).scope(securityService.getSetting(SocialSettingsKeys.GOOGLE_SCOPE.name(), String.class))
                    .callback(ClientUtils.getCallbackUrl()).build();

            break;
        }

        case ClientUtils.TWITTER: {
            service = new ServiceBuilder().provider(TwitterApi.class).apiKey(securityService.getSetting(SocialSettingsKeys.TWITTER_APP_ID.name(), String.class))
                    .apiSecret(securityService.getSetting(SocialSettingsKeys.TWITTER_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }
        case ClientUtils.YAHOO: {
            service = new ServiceBuilder().provider(YahooApi.class).apiKey(securityService.getSetting(SocialSettingsKeys.YAHOO_APP_ID.name(), String.class))
                    .apiSecret(securityService.getSetting(SocialSettingsKeys.YAHOO_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.LINKEDIN: {
            service = new ServiceBuilder().provider(LinkedInApi.class).apiKey(securityService.getSetting(SocialSettingsKeys.LINKEDIN_APP_ID.name(), String.class))
                    .apiSecret(securityService.getSetting(SocialSettingsKeys.LINKEDIN_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.INSTAGRAM: {
            service = new ServiceBuilder().provider(InstagramApi.class).apiKey(securityService.getSetting(SocialSettingsKeys.INSTAGRAM_APP_ID.name(), String.class))
                    .apiSecret(securityService.getSetting(SocialSettingsKeys.INSTAGRAM_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.GITHUB: {
            service = new ServiceBuilder().provider(GithubApi.class).apiKey(securityService.getSetting(SocialSettingsKeys.GITHUB_APP_ID.name(), String.class))
                    .apiSecret(securityService.getSetting(SocialSettingsKeys.GITHUB_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;

        }

        case ClientUtils.IMGUR: {
            service = new ServiceBuilder().provider(ImgUrApi.class).apiKey(securityService.getSetting(SocialSettingsKeys.IMGUR_APP_ID.name(), String.class))
                    .apiSecret(securityService.getSetting(SocialSettingsKeys.IMGUR_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.FLICKR: {
            service = new ServiceBuilder().provider(FlickrApi.class).apiKey(securityService.getSetting(SocialSettingsKeys.FLICKR_APP_ID.name(), String.class))
                    .apiSecret(securityService.getSetting(SocialSettingsKeys.FLICKR_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.VIMEO: {
            service = new ServiceBuilder().provider(VimeoApi.class).apiKey(securityService.getSetting(SocialSettingsKeys.VIMEO_APP_ID.name(), String.class))
                    .apiSecret(securityService.getSetting(SocialSettingsKeys.VIMEO_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.WINDOWS_LIVE: {
            // a Scope must be specified
            service = new ServiceBuilder().provider(LiveApi.class).apiKey(securityService.getSetting(SocialSettingsKeys.WINDOWS_LIVE_APP_ID.name(), String.class))
                    .apiSecret(securityService.getSetting(SocialSettingsKeys.WINDOWS_LIVE_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl())
                    .scope("wl.basic").build();
            break;
        }

        case ClientUtils.TUMBLR: {
            service = new ServiceBuilder().provider(TumblrApi.class).apiKey(securityService.getSetting(SocialSettingsKeys.TUMBLR_LIVE_APP_ID.name(), String.class))
                    .apiSecret(securityService.getSetting(SocialSettingsKeys.TUMBLR_LIVE_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        case ClientUtils.FOURSQUARE: {
            service = new ServiceBuilder().provider(Foursquare2Api.class).apiKey(securityService.getSetting(SocialSettingsKeys.FOURSQUARE_APP_ID.name(), String.class))
                    .apiSecret(securityService.getSetting(SocialSettingsKeys.FOURSQUARE_APP_SECRET.name(), String.class)).callback(ClientUtils.getCallbackUrl()).build();
            break;
        }

        default: {
            return null;
        }

        }
        return service;
    }

    @Override
    public String getAuthorizationUrl(CredentialDTO credential) throws OAuthException {
        logger.info("callback url: " + credential.getRedirectUrl());
        String authorizationUrl = null;
        Token requestToken = null;

        int authProvider = credential.getAuthProvider();

        OAuthService service = getOAuthService(authProvider);
        if (service == null) {
            throw new OAuthException("Could not build OAuthService");
        }

        if (authProvider == ClientUtils.TWITTER || authProvider == ClientUtils.YAHOO
                || authProvider == ClientUtils.LINKEDIN || authProvider == ClientUtils.FLICKR
                || authProvider == ClientUtils.IMGUR || authProvider == ClientUtils.TUMBLR
                || authProvider == ClientUtils.VIMEO) {
            String authProviderName = ClientUtils.getAuthProviderName(authProvider);
            logger.info(authProviderName + " requires Request token first.. obtaining..");
            try {
                requestToken = service.getRequestToken();
                logger.info("Got request token: " + requestToken);
                // we must save in the session. It will be required to
                // get the access token
                SessionUtils.saveRequestTokenToSession(requestToken);
            } catch (Exception e) {
                String stackTrace = stackTraceToString(e);
                throw new OAuthException("Could not get request token for " + authProvider + " " + stackTrace);
            }

        }

        logger.info("Getting Authorization url...");
        try {
            authorizationUrl = service.getAuthorizationUrl(requestToken);

            // Facebook has optional state var to protect against CSFR.
            // We'll use it
            if (authProvider == ClientUtils.FACEBOOK || authProvider == ClientUtils.GITHUB
                    || authProvider == ClientUtils.INSTAGRAM) {
                String state = makeRandomString();
                authorizationUrl += "&state=" + state;
                SessionUtils.saveStateToSession(state);
            }
        } catch (Exception e) {
//            String st = LogUtil.stackTraceToString(e);
            throw new OAuthException("Could not get Authorization url: ");
        }

        if (authProvider == ClientUtils.FLICKR) {
            authorizationUrl += "&perms=read";
        }

        logger.info("Authorization url: " + authorizationUrl);

        return authorizationUrl;
    }

    public static String stackTraceToString(Throwable caught) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement e : caught.getStackTrace()) {
            sb.append(e.toString()).append("\n");
        }
        return sb.toString();
    }

    

    private String makeRandomString() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

//    private SocialUserDTO getSocialUser(Token accessToken, int authProvider) throws OAuthException {
//        logger.info("Token: " + accessToken + " Provider: " + authProvider);
//        OAuthService service = getOAuthService(authProvider);
//
//        String url = SessionUtils.getProtectedResourceUrlFromSession();
//        OAuthRequest request = new OAuthRequest(Verb.GET, url);
//        // sign the request
//        service.signRequest(accessToken, request);
//        Response response = request.send();
//        String json = response.getBody();
//        SocialUserDTO socialUser = getSocialUserFromJson(json, authProvider);
//        return socialUser;
//    }

    @Override
    public UserDTO verifySocialUser(CredentialDTO credentialDTO) {
        
        OAuthToken otoken = new OAuthToken(createCredentialFromDTO(credentialDTO), credentialDTO.getVerifier());
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()) {
            try {
                subject.login(otoken);
                logger.info("User [" + subject.getPrincipal().toString() + "] logged in successfully.");
            } catch (UnknownAccountException uae) {
                logger.info("There is no user with username of " + subject.getPrincipal());
            } catch (IncorrectCredentialsException ice) {
                logger.info("Password for account " + subject.getPrincipal() + " was incorrect!");
            } catch (LockedAccountException lae) {
                logger.info("The account for username " + subject.getPrincipal() + " is locked.  "
                        + "Please contact your administrator to unlock it.");
            } catch (AuthenticationException ae) {
                logger.log(Level.SEVERE,ae.getLocalizedMessage());
            }
        }
        return createUserDTOFromUser( securityService.getUserByName(SessionUtils.loadUsername()));
    }

    

//    private SocialUserDTO getSocialUserFromJson(String json, int authProvider) throws OAuthException {
//        String authProviderName = ClientUtils.getAuthProviderName(authProvider);
//        logger.info("Auth provider: " + authProviderName);
//
//        JSONParser jsonParser = new JSONParser();
//        Object obj = null;
//        SocialUserDTO socialUser = new SocialUserDTO();
//        switch (authProvider) {
//        case ClientUtils.FACEBOOK: {
//            /*
//             * --Facebook-- { "id":"537157209", "name":"Muhammad Muquit", "first_name":"Muhammad", "last_name":"Muquit",
//             * "link":"http:\/\/www.facebook.com\/muhammad.muquit", "username":"muhammad.muquit", "gender":"male",
//             * "timezone":-5,"locale":"en_US", "verified":true, "updated_time":"2012-11-10T23:13:04+0000"} }
//             */
//            try {
//                obj = jsonParser.parse(json);
//                JSONObject jsonObj = (JSONObject) obj;
//
//                socialUser.setName((String) jsonObj.get("name"));
//                socialUser.setFirstName((String) jsonObj.get("first_name"));
//                socialUser.setLastName((String) jsonObj.get("last_name"));
//                socialUser.setGender((String) jsonObj.get("gender"));
//
//                socialUser.setJson(json);
//
//                return socialUser;
//            } catch (ParseException e) {
//                throw new OAuthException("Could not parse JSON data from " + authProviderName + ":" + e.getMessage());
//            }
//        }
//
//        case ClientUtils.YAHOO: {
//            /*
//             * --YAHOO-- http://developer.yahoo.com/social/rest_api_guide/extended-profile-resource.html# { "profile": {
//             * "uri": "http:\/\/social.yahooapis.com\/v1\/user\/ECUFIYO7BLY5FOV54XAPEQDC3Y\/profile", "guid":
//             * "ECUFIYO7BLY5FOAPEQDC3Y", "birthYear": 1969, "created": "2010-01-23T13:07:10Z", "displayAge": 89,
//             * "gender": "M", "image": { "height": 192, "imageUrl":
//             * "http:\/\/l.yimg.com\/a\/i\/identity2\/profile_192c.png", "size": "192x192", "width": 192 }, "location":
//             * "Philadelphia, Pennsylvania", "memberSince": "2006-08-04T13:27:58Z", "nickname": "jdoe", "profileUrl":
//             * "http:\/\/profile.yahoo.com\/ECUFIYO7BLY5FOV54XAPEQDC3Y", "searchable": false, "updated":
//             * "2011-04-16T07:28:00Z", "isConnected": false } }
//             */
//            try {
//                obj = jsonParser.parse(json);
//                JSONObject jsonObj = (JSONObject) obj;
//                // get profile object
//                JSONObject jsonObjPeople = (JSONObject) jsonObj.get("profile");
//
//                socialUser.setJson(json);
//
//                socialUser.setNickname((String) jsonObjPeople.get("nickname"));
//                socialUser.setGender((String) jsonObjPeople.get("gender"));
//                socialUser.setFirstName((String) jsonObjPeople.get("givenName"));
//                socialUser.setLastName((String) jsonObjPeople.get("familyName"));
//
//                return socialUser;
//            } catch (ParseException e) {
//                throw new OAuthException("Could not parse JSON data from " + authProviderName + ":" + e.getMessage());
//            }
//        }
//
//        case ClientUtils.GOOGLE: {
//            /*
//             * --Google-- { "id": "116397076041912827850", "name": "Muhammad Muquit", "given_name": "Muhammad",
//             * "family_name": "Muquit", "link": "https://plus.google.com/116397076041912827850", "gender": "male",
//             * "locale": "en-US" }
//             */
//
//            try {
//                obj = jsonParser.parse(json);
//                JSONObject jsonObj = (JSONObject) obj;
//
//                socialUser.setJson(json);
//
//                socialUser.setName((String) jsonObj.get("name"));
//                socialUser.setFirstName((String) jsonObj.get("given_name"));
//                socialUser.setLastName((String) jsonObj.get("family_name"));
//                socialUser.setGender((String) jsonObj.get("gender"));
//
//                return socialUser;
//            } catch (ParseException e) {
//                throw new OAuthException("Could not parse JSON data from " + authProviderName + ":" + e.getMessage());
//            }
//        }
//
//        case ClientUtils.LINKEDIN: {
//            /*
//             * --Linkedin-- { "firstName": "Muhammad", "headline": "Sr. Software Engineer at British Telecom",
//             * "lastName": "Muquit", }
//             */
//            try {
//                obj = jsonParser.parse(json);
//                JSONObject jsonObj = (JSONObject) obj;
//
//                socialUser.setJson(json);
//
//                socialUser.setFirstName((String) jsonObj.get("firstName"));
//                socialUser.setLastName((String) jsonObj.get("lastName"));
//
//                return socialUser;
//            } catch (ParseException e) {
//                throw new OAuthException("Could not parse JSON data from " + authProviderName + ":" + e.getMessage());
//            }
//        }
//
//        case ClientUtils.TWITTER: {
//            /*
//             * --Twitter -- { "id":955924206, "contributors_enabled":false, "profile_use_background_image":true,
//             * "time_zone":"Eastern Time (US & Canada)", "following":false, "friends_count":3, "profile_text_color":
//             * "333333", "geo_enabled":false, "created_at":"Sun Nov 18 17:54:22 +0000 2012", "utc_offset":-18000,
//             * "follow_request_sent":false, "name":"Muhammad Muquit", "id_str":"955924206",
//             * "default_profile_image":true, "verified":false, "profile_sidebar_border_color":"C0DEED", "url":null,
//             * "favourites_count":0, .. "lang":"en", "profile_background_color":"C0DEED", "screen_name":"mmqt2012", .. }
//             */
//
//            try {
//                obj = jsonParser.parse(json);
//                JSONObject jsonObj = (JSONObject) obj;
//
//                socialUser.setJson(json);
//
//                socialUser.setName((String) jsonObj.get("name"));
//                socialUser.setGender((String) jsonObj.get("gender"));
//
//                return socialUser;
//            } catch (ParseException e) {
//                throw new OAuthException("Could not parse JSON data from " + authProviderName + ":" + e.getMessage());
//            }
//        }
//
//        case ClientUtils.INSTAGRAM: {
//            /*
//             * -- Instragram -- { "data": { "id": "1574083", "username": "snoopdogg", "full_name": "Snoop Dogg",
//             * "profile_picture": "http://distillery.s3.amazonaws.com/profiles/profile_1574083_75sq_1295469061.jpg",
//             * "bio": "This is my bio", "website": "http://snoopdogg.com", "counts": { "media": 1320, "follows": 420,
//             * "followed_by": 3410 } }
//             */
//
//            try {
//                logger.info("Instragram JSON: " + json);
//                obj = jsonParser.parse(json);
//                JSONObject jsonObj = (JSONObject) obj;
//                // get profile object
//                JSONObject jsonObjData = (JSONObject) jsonObj.get("data");
//
//                socialUser.setJson(json);
//                socialUser.setName((String) jsonObjData.get("username"));
//
//                return socialUser;
//
//            } catch (ParseException e) {
//                throw new OAuthException("Could not parse JSON data from " + authProviderName + ":" + e.getMessage());
//            }
//
//        }
//
//        case ClientUtils.GITHUB: {
//            /*
//             * -- github -- { "plan":{ "private_repos":0, "space":307200, "name":"free", "collaborators":0 },
//             * "followers":0, "type":"User", "events_url":"https://api.github.com/users/oauthdemo2012/events{/privacy}",
//             * "owned_private_repos":0, "public_gists":0, "avatar_url":
//             * "https://secure.gravatar.com/avatar/e0cb08c2b353cc1c3022dc65ebd060d1?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-user-420.png"
//             * , "received_events_url":"https://api.github.com/users/oauthdemo2012/received_events", "private_gists":0,
//             * "disk_usage":0, "url":"https://api.github.com/users/oauthdemo2012",
//             * "followers_url":"https://api.github.com/users/oauthdemo2012/followers", "login":"oauthdemo2012",
//             * "created_at":"2012-12-20T01:36:36Z",
//             * "following_url":"https://api.github.com/users/oauthdemo2012/following",
//             * "organizations_url":"https://api.github.com/users/oauthdemo2012/orgs", "following":0,
//             * "starred_url":"https://api.github.com/users/oauthdemo2012/starred{/owner}{/repo}", "collaborators":0,
//             * "public_repos":0, "repos_url":"https://api.github.com/users/oauthdemo2012/repos",
//             * "gists_url":"https://api.github.com/users/oauthdemo2012/gists{/gist_id}", "id":3085592,
//             * "total_private_repos":0, "html_url":"https://github.com/oauthdemo2012",
//             * "subscriptions_url":"https://api.github.com/users/oauthdemo2012/subscriptions",
//             * "gravatar_id":"e0cb08c2b353cc1c3022dc65ebd060d1" }
//             */
//            logger.info("github JSON: " + json);
//            try {
//                obj = jsonParser.parse(json);
//                JSONObject jsonObj = (JSONObject) obj;
//
//                socialUser.setJson(json);
//                socialUser.setName((String) jsonObj.get("login"));
//
//                return socialUser;
//
//            } catch (ParseException e) {
//                throw new OAuthException("Could not parse JSON data from " + authProviderName + ":" + e.getMessage());
//            }
//        }
//
//        case ClientUtils.FLICKR: {
//            /*
//             * -- flickr -- { "user": { "id": "91390211@N06", "username": { "_content": "oauthdemo2012" } }, "stat":
//             * "ok" }
//             */
//            logger.info("Flickr JSON: " + json);
//            try {
//                obj = jsonParser.parse(json);
//                JSONObject jsonObj = (JSONObject) obj;
//                JSONObject jsonObjUser = (JSONObject) jsonObj.get("user");
//                JSONObject jsonObjUsername = (JSONObject) jsonObjUser.get("username");
//                socialUser.setName((String) jsonObjUsername.get("_content"));
//                socialUser.setJson(json);
//
//                return socialUser;
//            } catch (ParseException e) {
//                throw new OAuthException("Could not parse JSON data from " + authProviderName + ":" + e.getMessage());
//            }
//        }
//
//        case ClientUtils.VIMEO: {
//            /*
//             * --Vimeo starts -- { "generated_in": "0.0698", "stat": "ok", "person": { "created_on":
//             * "2012-12-22 23:37:55", "id": "15432968", "is_contact": "0", "is_plus": "0", "is_pro": "0", "is_staff":
//             * "0", "is_subscribed_to": "0", "username": "user15432968", "display_name": "oauthdemo2012", "location":
//             * "", "url": [ "" ], ..... } }
//             */
//            logger.info("Vimeo JSON: " + json);
//            try {
//                obj = jsonParser.parse(json);
//                JSONObject jsonObj = (JSONObject) obj;
//                JSONObject jsonObjPerson = (JSONObject) jsonObj.get("person");
//                String userName = (String) jsonObjPerson.get("username");
//                String displayName = (String) jsonObjPerson.get("display_name");
//
//                if (displayName != null) {
//                    socialUser.setName(displayName);
//                } else if (userName != null) {
//                    socialUser.setName(userName);
//                } else {
//                    socialUser.setName("Unknown");
//                }
//                socialUser.setJson(json);
//
//                return socialUser;
//            } catch (ParseException e) {
//                throw new OAuthException("Could not parse JSON data from " + authProviderName + ":" + e.getMessage());
//            }
//        }
//
//        case ClientUtils.WINDOWS_LIVE: {
//            /*
//             * Windows Live --starts -- { "id" : "contact.c1678ab4000000000000000000000000", "first_name" : "Roberto",
//             * "last_name" : "Tamburello", "name" : "Roberto Tamburello", "gender" : "male", "locale" : "en_US" }
//             */
//            logger.info("Windows Live JSON: " + json);
//            try {
//                obj = jsonParser.parse(json);
//                JSONObject jsonObj = (JSONObject) obj;
//                JSONObject jsonErrorObj = (JSONObject) jsonObj.get("error");
//                if (jsonErrorObj != null) {
//                    /*
//                     * { "error": { "code": "request_token_too_many", "message":
//                     * "The request includes more than one access token. Only one access token is allowed." } }
//                     */
//                    String message = (String) jsonErrorObj.get("message");
//                    throw new OAuthException("Error: " + message);
//                }
//                socialUser.setName((String) jsonObj.get("name"));
//                socialUser.setLastName((String) jsonObj.get("last_name"));
//                socialUser.setFirstName((String) jsonObj.get("first_name"));
//                socialUser.setJson(json);
//
//                return socialUser;
//            } catch (ParseException e) {
//                throw new OAuthException("Could not parse JSON data from " + authProviderName + ":" + e.getMessage());
//            }
//        }
//
//        case ClientUtils.TUMBLR: {
//            /*
//             * tumblr. -- { "meta": { "status": 200, "msg": "OK" }, "response": { "user": { "name": "oauthdemo2012",
//             * "likes": 0, "following": 1, "default_post_format": "html", "blogs": [ { "name": "oauthdemo2012", "url":
//             * "http:\/\/oauthdemo2012.tumblr.com\/", "followers": 0, "primary": true, "title": "Untitled",
//             * "description": "", "admin": true, "updated": 0, "posts": 0, "messages": 0, "queue": 0, "drafts": 0,
//             * "share_likes": true, "ask": false, "tweet": "N", "facebook": "N", "facebook_opengraph_enabled": "N",
//             * "type": "public" } ] } } }
//             */
//            logger.info("tumblr JSON: " + json);
//            try {
//                obj = jsonParser.parse(json);
//                JSONObject jsonObj = (JSONObject) obj;
//                JSONObject jsonObjResponse = (JSONObject) jsonObj.get("response");
//                JSONObject jsonObjUser = (JSONObject) jsonObjResponse.get("user");
//                String userName = (String) jsonObjUser.get("name");
//                socialUser.setName(userName);
//                socialUser.setJson(json);
//
//                return socialUser;
//            } catch (ParseException e) {
//                throw new OAuthException("Could not parse JSON data from " + authProviderName + ":" + e.getMessage());
//            }
//        }
//
//        case ClientUtils.FOURSQUARE: {
//
//            /*
//             * foursquare -- { "meta": { "code": 200, "errorType": "deprecated", "errorDetail":
//             * "Please provide an API version to avoid future errors.See http://bit.ly/vywCav" }, "notifications": [ {
//             * "type": "notificationTray", "item": { "unreadCount": 0 } } ], "response": { "user": { "id": "43999331",
//             * "firstName": "OAuth", "lastName": "Demo", "gender": "none", "relationship": "self", "photo":
//             * "https://foursquare.com/img/blank_boy.png", "friends": { "count": 0, "groups": [ { "type": "friends",
//             * "name": "Mutual friends", "count": 0, "items": [] }, { "type": "others", "name": "Other friends",
//             * "count": 0, "items": [] } ] }, ...... } } }
//             */
//            try {
//                obj = jsonParser.parse(json);
//                JSONObject jsonObj = (JSONObject) obj;
//                JSONObject jsonObjResponse = (JSONObject) jsonObj.get("response");
//                JSONObject jsonObjUser = (JSONObject) jsonObjResponse.get("user");
//                String firstName = (String) jsonObjUser.get("firstName");
//                String lastName = (String) jsonObjUser.get("lastName");
//                if (firstName != null && lastName != null) {
//                    socialUser.setName(firstName + " " + lastName);
//                } else {
//                    socialUser.setName("UNKNOWN");
//                }
//                socialUser.setJson(json);
//
//                return socialUser;
//            } catch (ParseException e) {
//                throw new OAuthException("Could not parse JSON data from " + authProviderName + ":" + e.getMessage());
//            }
//        }
//
//        default: {
//            throw new OAuthException("Unknown Auth Provider: " + authProviderName);
//        }
//        }
//
//        /*
//         * We don't use Gson() anymore as it choked on nested Facebook JSON data Dec-03-2012
//         */
//
//        /*
//         * // map json to SocialUser try { Gson gson = new Gson(); SocialUser user =
//         * gson.fromJson(json,SocialUser.class); // pretty print json //gson = new
//         * GsonBuilder().setPrettyPrinting().create(); //String jsonPretty = gson.toJson(json); user.setJson(json);
//         * return user; } catch (Exception e) { e.printStackTrace(); throw new
//         * OurException("Could not map userinfo JSON to SocialUser class: " + e); }
//         */
//    }

    private HttpSession getHttpSession() {
        return getThreadLocalRequest().getSession();
    }

    private HttpSession validateSession(String sessionId) throws OAuthException {
        if (sessionId == null)
            throw new OAuthException("Session Id can not be empty");
        HttpSession session = getHttpSession();
        if (session == null) {
            throw new OAuthException(ClientUtils.SESSION_EXPIRED_MESSAGE);
        }
        /*
         * ServersideSession ssSession=getServersideSession(); if (ssSession == null) { throw new
         * OurException(ClientUtils.SESSION_EXPIRED_MESSAGE); } if (sessionId.equals(ssSession.getSessionId())) { return
         * session; }
         */
        String savedSessionId = SessionUtils.getSessionIdFromSession();
        if (sessionId.equals(savedSessionId)) {
            return session;
        }
        throw new OAuthException("Session Id mismatch: expected " + "'" + sessionId + "'" + " Found: " + "'"
                + savedSessionId + "'");
    }

    @Override
    public String getAccessToken(String sessionId) throws OAuthException {
        validateSession(sessionId);
        Token accessToken = SessionUtils.getAccessTokenFromSession();
        if (accessToken == null) {
            throw new OAuthException("Could not find Access Token in HTTP Session");
        }
        return accessToken.getRawResponse();
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
        return credential;
    }
    
    private SocialUserDTO createSocialUserDTO(SocialUserAccount socialUser){
        SocialUserDTO socialUserDTO = new SocialUserDTO();
        socialUserDTO.setSessionId(socialUser.getSessionId());
        
        socialUserDTO.setEmail(socialUser.getEmail());
        socialUserDTO.setJson(socialUser.getJson());
        
        /* must be named exactly as JSON google returns -- starts */
        socialUserDTO.setId(socialUser.getId());
        socialUserDTO.setName(socialUser.getName());
        socialUserDTO.setGiven_name(socialUser.getGiven_name());
        socialUserDTO.setFamily_name(socialUser.getFamily_name());
        socialUserDTO.setGender(socialUser.getGender());
        socialUserDTO.setLink(socialUser.getLink());
        socialUserDTO.setLocale(socialUser.getLocale());
        /* must be named exactly as JSON google returns -- ends */
        
        /* Yahoo --starts */
        socialUserDTO.setGuid(socialUser.getGuid()); // is it always the same for the user??
        socialUserDTO.setGivenName(socialUser.getGivenName());
        socialUserDTO.setFamilyName(socialUser.getFamilyName());
        socialUserDTO.setNickname(socialUser.getNickname());
        socialUserDTO.setLocation(socialUser.getLocation());
        socialUserDTO.setBirthdate(socialUser.getBirthdate());
        socialUserDTO.setTimeZone(socialUser.getTimeZone());
        socialUserDTO.setLang(socialUser.getLang());
        socialUserDTO.setRelationShipStatus(socialUser.getRelationShipStatus());
        socialUserDTO.setDisplayAge(socialUser.getDisplayAge());
        /* Yahoo --ends */
        
        /* Linkedin -starts */
        socialUserDTO.setFirstName(socialUser.getFirstName());
        socialUserDTO.setLastName(socialUser.getLastName());
        socialUserDTO.setHeadline(socialUser.getHeadline());
        return socialUserDTO;
    }
}
