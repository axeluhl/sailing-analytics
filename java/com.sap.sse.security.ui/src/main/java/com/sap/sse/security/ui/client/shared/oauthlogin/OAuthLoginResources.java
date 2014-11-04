package com.sap.sse.security.ui.client.shared.oauthlogin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface OAuthLoginResources extends ClientBundle {
    public static final OAuthLoginResources INSTANCE = GWT.create(OAuthLoginResources.class);

    @Source("com/sap/sse/security/ui/client/shared/oauthlogin/OAuthLoginResources.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String oauthLogo();
        String oauthPanel();
    }
    
    @Source("com/sap/sse/security/ui/oauth/images/facebook.png")
    public ImageResource facebookIcon();

    @Source("com/sap/sse/security/ui/oauth/images/flickr.png")
    public ImageResource flickrIcon();

    @Source("com/sap/sse/security/ui/oauth/images/github.png")
    public ImageResource githubIcon();

    @Source("com/sap/sse/security/ui/oauth/images/google.png")
    public ImageResource googleIcon();

    @Source("com/sap/sse/security/ui/oauth/images/linkedin.png")
    public ImageResource linkedinIcon();

    @Source("com/sap/sse/security/ui/oauth/images/twitter.png")
    public ImageResource twitterIcon();

    @Source("com/sap/sse/security/ui/oauth/images/vimeo.png")
    public ImageResource vimeoIcon();

    @Source("com/sap/sse/security/ui/oauth/images/wordpress.png")
    public ImageResource wordpressIcon();

    @Source("com/sap/sse/security/ui/oauth/images/yahoo.png")
    public ImageResource yahooIcon();
    
    @Source("com/sap/sse/security/ui/oauth/images/instagram.png")
    public ImageResource instagramIcon();
    
    @Source("com/sap/sse/security/ui/oauth/images/imgur.png")
    public ImageResource imgurIcon();
    
    @Source("com/sap/sse/security/ui/oauth/images/live32.png")
    public ImageResource liveIcon();
    
    @Source("com/sap/sse/security/ui/oauth/images/tumblr.png")
    public ImageResource tumblrIcon();
    
    @Source("com/sap/sse/security/ui/oauth/images/foursquare.png")
    public ImageResource foursquareIcon();
}
