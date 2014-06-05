package com.sap.sse.security.userstore.shared;

public enum SocialSettingsKeys {

    FACEBOOK_APP_NAME("Sailing"),FACEBOOK_APP_ID("351552528316184"),FACEBOOK_APP_SECRET("d1a0ac61c5fb36cbd6fda3c32a4af227"), 
    GOOGLE_APP_NAME(null), GOOGLE_APP_ID(null) , GOOGLE_APP_SECRET(null) , GOOGLE_SCOPE("https://www.googleapis.com/auth/userinfo.profile"),
    TWITTER_APP_NAME(null) , TWITTER_APP_ID(null) , TWITTER_APP_SECRET(null) ,
    YAHOO_APP_NAME(null) , YAHOO_APP_ID(null) , YAHOO_APP_SECRET(null) ,
    INSTAGRAM_APP_NAME(null) , INSTAGRAM_APP_ID(null) , INSTAGRAM_APP_SECRET(null) ,
    LINKEDIN_APP_NAME(null) , LINKEDIN_APP_ID(null) , LINKEDIN_APP_SECRET(null) , LINKEDIN_USER_TOKEN(null) , LINKEDIN_USER_SECRET(null) ,
    GITHUB_APP_NAME(null) , GITHUB_APP_ID(null) , GITHUB_APP_SECRET(null) ,
    FLICKR_APP_NAME(null) , FLICKR_APP_ID(null) , FLICKR_APP_SECRET(null) , 
    IMGUR_APP_NAME(null) , IMGUR_APP_ID(null) , IMGUR_APP_SECRET(null) , 
    VIMEO_APP_NAME(null) , VIMEO_APP_ID(null) , VIMEO_APP_SECRET(null) ,
    EVERNOTE_APP_NAME(null) , EVERNOTE_APP_ID(null) , EVERNOTE_APP_SECRET(null) ,
    WINDOWS_LIVE_APP_NAME(null) , WINDOWS_LIVE_APP_ID(null) , WINDOWS_LIVE_APP_SECRET(null) ,
    TUMBLR_LIVE_APP_NAME(null) , TUMBLR_LIVE_APP_ID(null) , TUMBLR_LIVE_APP_SECRET(null) ,
    FOURSQUARE_APP_NAME(null) , FOURSQUARE_APP_ID(null) , FOURSQUARE_APP_SECRET(null) ;

private String value;

    private SocialSettingsKeys(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
    
}
