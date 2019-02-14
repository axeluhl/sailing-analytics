package com.sap.sse.security.interfaces;

public enum SocialSettingsKeys {

    OAUTH_FACEBOOK_APP_NAME("Sailing"),OAUTH_FACEBOOK_APP_ID("351552528316184"),OAUTH_FACEBOOK_APP_SECRET("d1a0ac61c5fb36cbd6fda3c32a4af227"), 
    OAUTH_GOOGLE_APP_NAME("Sailing"), OAUTH_GOOGLE_APP_ID("223511326134-mfsusd7f8ud6kde14ru737ve9li0rhet.apps.googleusercontent.com") , OAUTH_GOOGLE_APP_SECRET("MeEsfQpCFWHP9mx0PbWMmB4T") , OAUTH_GOOGLE_SCOPE("https://www.googleapis.com/auth/userinfo.profile"),
    OAUTH_TWITTER_APP_NAME(null) , OAUTH_TWITTER_APP_ID(null) , OAUTH_TWITTER_APP_SECRET(null) ,
    OAUTH_YAHOO_APP_NAME(null) , OAUTH_YAHOO_APP_ID(null) , OAUTH_YAHOO_APP_SECRET(null) ,
    OAUTH_INSTAGRAM_APP_NAME(null) , OAUTH_INSTAGRAM_APP_ID(null) , OAUTH_INSTAGRAM_APP_SECRET(null) ,
    OAUTH_LINKEDIN_APP_NAME(null) , OAUTH_LINKEDIN_APP_ID(null) , OAUTH_LINKEDIN_APP_SECRET(null) , OAUTH_LINKEDIN_USER_TOKEN(null) , OAUTH_LINKEDIN_USER_SECRET(null) ,
    OAUTH_GITHUB_APP_NAME(null) , OAUTH_GITHUB_APP_ID(null) , OAUTH_GITHUB_APP_SECRET(null) ,
    OAUTH_FLICKR_APP_NAME(null) , OAUTH_FLICKR_APP_ID(null) , OAUTH_FLICKR_APP_SECRET(null) , 
    OAUTH_IMGUR_APP_NAME(null) , OAUTH_IMGUR_APP_ID(null) , OAUTH_IMGUR_APP_SECRET(null) , 
    OAUTH_VIMEO_APP_NAME(null) , OAUTH_VIMEO_APP_ID(null) , OAUTH_VIMEO_APP_SECRET(null) ,
    OAUTH_EVERNOTE_APP_NAME(null) , OAUTH_EVERNOTE_APP_ID(null) , OAUTH_EVERNOTE_APP_SECRET(null) ,
    OAUTH_WINDOWS_LIVE_APP_NAME(null) , OAUTH_WINDOWS_LIVE_APP_ID(null) , OAUTH_WINDOWS_LIVE_APP_SECRET(null) ,
    OAUTH_TUMBLR_LIVE_APP_NAME(null) , OAUTH_TUMBLR_LIVE_APP_ID(null) , OAUTH_TUMBLR_LIVE_APP_SECRET(null) ,
    OAUTH_FOURSQUARE_APP_NAME(null) , OAUTH_FOURSQUARE_APP_ID(null) , OAUTH_FOURSQUARE_APP_SECRET(null) ;

    private String value;

    private SocialSettingsKeys(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
    
}
