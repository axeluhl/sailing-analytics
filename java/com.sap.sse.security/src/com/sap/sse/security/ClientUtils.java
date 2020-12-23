package com.sap.sse.security;

public class ClientUtils {
    // Callback URL is registered with each OAuth Provider
    // private static final String APP_CALLBACK_URL = "http://oauthdemo2012.com:8888/GWTOAuthLoginDemo.html";
    // http://127.0.0.1:8888/GWTOAuthLoginDemo.html?gwt.codesvr=127.0.0.1:9997&
    private static final String APP_CALLBACK_URL = "http://local.sapsailing.com:8888/security/ui/redirect?redirectTo=oauthlogin";

    private static final String FACEBOOK_PROTECTED_RESOURCE_URL = "https://graph.facebook.com/me";

    private static final String GOOGLE_PROTECTED_RESOURSE_URL = "https://www.googleapis.com/oauth2/v1/userinfo";

    private static final String TWITTER_PROTECTED_RESOURCE_URL = "https://api.twitter.com/1/account/verify_credentials.json";

    // %s is guid and the caller must replace
    private static final String YAHOO_PROTECTED_RESOURCE_URL = "http://social.yahooapis.com/v1/user/%s/profile?format=json";

    private static final String LINKEDIN_PROTECTED_RESOURCE_URL = "http://api.linkedin.com/v1/people/~?format=json";

    // %s is userId and acess token and the caller must replace
    private static final String INSTAGRAM_PROTECTED_RESOURCE_URL = "https://api.instagram.com/v1/users/%s/?access_token=%s";

    private static final String IMGUR_PROTECTED_RESOURCE_URL = "http://api.imgur.com/2/account.json";

    private static final String GITHUB_PROTECTED_RESOURCE_URL = "https://api.github.com/user?access_token=%s";

    private static final String LIVE_PROTECTED_RESOURCE_URL = "https://apis.live.net/v5.0/me";

    private static final String FLICKR_PROTECTED_RESOURCE_URL = "http://api.flickr.com/services/rest?format=json&nojsoncallback=1&method=flickr.test.login";

    private static final String VIMEO_PROTECTED_RESOURCE_URL = "http://vimeo.com/api/rest/v2?format=json&method=vimeo.people.getInfo";

    private static final String TUMBLR_PROTECTED_RESOURCE_URL = "https://api.tumblr.com/v2/user/info";

    // caller must replace %s with access token
    private static final String FOURSQUARE_PROTECTED_RESOURCE_URL = "https://api.foursquare.com/v2/users/self?oauth_token=%s";

    private static String[] authProviders = { "Facebook", "Google", "Twitter", "Yahoo!", "Flickr", "ImaGur",
            "LinkedIn", "Windows Live", "Instagram", "github", "Vimeo", "Evernote", "tumblr.", "foursquare", };

    public final static int UNKNOWN = -1;
    public final static int DEFAULT = 0;
    public final static int FACEBOOK = 1;
    public final static int GOOGLE = 2;
    public final static int TWITTER = 3;
    public final static int YAHOO = 4;
    public final static int FLICKR = 5;
    public final static int IMGUR = 6;
    public final static int LINKEDIN = 7;
    public final static int WINDOWS_LIVE = 8;
    public final static int INSTAGRAM = 9;
    public final static int GITHUB = 10;
    public final static int VIMEO = 11;
    public final static int EVERNOTE = 12;
    public final static int TUMBLR = 13;
    public final static int FOURSQUARE = 14;

    public static String[] getAuthProviders() {
        return authProviders;
    }

    public static int getAuthProvider(String authProviderName) {
        int authProvider = DEFAULT;
        authProviderName = authProviderName.toLowerCase();
        if (authProviderName.equals("facebook"))
            return FACEBOOK;
        else if (authProviderName.equals("google"))
            return GOOGLE;
        else if (authProviderName.equals("twitter"))
            return TWITTER;
        else if (authProviderName.equals("yahoo!"))
            return YAHOO;
        else if (authProviderName.equals("yahoo"))
            return YAHOO;
        else if (authProviderName.equals("flickr"))
            return FLICKR;
        else if (authProviderName.equals("imgur"))
            return IMGUR;
        else if (authProviderName.equals("linkedin"))
            return LINKEDIN;
        else if (authProviderName.equals("windows live"))
            return WINDOWS_LIVE;
        else if (authProviderName.equals("instagram"))
            return INSTAGRAM;
        else if (authProviderName.equals("github"))
            return GITHUB;
        else if (authProviderName.equals("vimeo"))
            return VIMEO;
        else if (authProviderName.equals("evernote"))
            return EVERNOTE;
        else if (authProviderName.equals("tumblr."))
            return TUMBLR;
        else if (authProviderName.equals("foursquare"))
            return FOURSQUARE;
        return authProvider;
    }

    public static String getAuthProviderName(int authProvider) {
        if (authProvider == FACEBOOK)
            return "Facebook";
        else if (authProvider == GOOGLE)
            return "Google";
        else if (authProvider == TWITTER)
            return "Twitter";
        else if (authProvider == YAHOO)
            return "Yahoo!";
        else if (authProvider == FLICKR)
            return "Flicker";
        else if (authProvider == IMGUR)
            return "ImGur";
        else if (authProvider == LINKEDIN)
            return "LinkedIn";
        else if (authProvider == WINDOWS_LIVE)
            return "Windows Live";
        else if (authProvider == INSTAGRAM)
            return "Instagram";
        else if (authProvider == GITHUB)
            return "github";
        else if (authProvider == VIMEO)
            return "vimeo";
        else if (authProvider == EVERNOTE)
            return "Evernote";
        else if (authProvider == TUMBLR)
            return "tumblr.";
        else if (authProvider == FOURSQUARE)
            return "foursquare";
        return "Default";
    }

    public static String getCallbackUrl() {
        return APP_CALLBACK_URL;
    }

    public static String getProctedResourceUrl(int authProvider) {
        switch (authProvider) {
        case FACEBOOK: {
            return FACEBOOK_PROTECTED_RESOURCE_URL;
        }
        case GOOGLE: {
            return GOOGLE_PROTECTED_RESOURSE_URL;
        }
        case TWITTER: {
            return TWITTER_PROTECTED_RESOURCE_URL;
        }
        case YAHOO: {
            return YAHOO_PROTECTED_RESOURCE_URL;
        }
        case LINKEDIN: {
            return LINKEDIN_PROTECTED_RESOURCE_URL;
        }
        case INSTAGRAM: {
            return INSTAGRAM_PROTECTED_RESOURCE_URL;
        }
        case IMGUR: {
            return IMGUR_PROTECTED_RESOURCE_URL;
        }
        case GITHUB: {
            return GITHUB_PROTECTED_RESOURCE_URL;
        }
        case FLICKR: {
            return FLICKR_PROTECTED_RESOURCE_URL;
        }
        case VIMEO: {
            return VIMEO_PROTECTED_RESOURCE_URL;
        }
        case WINDOWS_LIVE: {
            return LIVE_PROTECTED_RESOURCE_URL;
        }
        case TUMBLR: {
            return TUMBLR_PROTECTED_RESOURCE_URL;
        }
        case FOURSQUARE: {
            return FOURSQUARE_PROTECTED_RESOURCE_URL;
        }
        default: {
            return null;
        }
        }
    }
}
