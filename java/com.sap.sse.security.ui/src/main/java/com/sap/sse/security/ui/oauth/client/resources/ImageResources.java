package com.sap.sse.security.ui.oauth.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;

public interface ImageResources extends ClientBundle {
    
    public static final ImageResources INSTANCE = GWT.create(ImageResources.class);

    @Source("com/sap/sse/security/ui/oauth/images/facebook.png")
    public DataResource facebookIconImageData();

    @Source("com/sap/sse/security/ui/oauth/images/flickr.png")
    public DataResource flickrIconImageData();

    @Source("com/sap/sse/security/ui/oauth/images/github.png")
    public DataResource githubIconImageData();

    @Source("com/sap/sse/security/ui/oauth/images/google.png")
    public DataResource googleIconImageData();

    @Source("com/sap/sse/security/ui/oauth/images/linkedin.png")
    public DataResource linkedinIconImageData();

    @Source("com/sap/sse/security/ui/oauth/images/twitter.png")
    public DataResource twitterIconImageData();

    @Source("com/sap/sse/security/ui/oauth/images/vimeo.png")
    public DataResource vimeoIconImageData();

    @Source("com/sap/sse/security/ui/oauth/images/wordpress.png")
    public DataResource wordpressIconImageData();

    @Source("com/sap/sse/security/ui/oauth/images/yahoo.png")
    public DataResource yahooIconImageData();
    
    @Source("com/sap/sse/security/ui/oauth/images/instagram.png")
    public DataResource instagramIconImageData();
    
    @Source("com/sap/sse/security/ui/oauth/images/imgur.png")
    public DataResource imgurIconImageData();
    
    @Source("com/sap/sse/security/ui/oauth/images/live32.png")
    public DataResource liveIconImageData();
    
    @Source("com/sap/sse/security/ui/oauth/images/tumblr.png")
    public DataResource tumblrIconImageData();
    
    @Source("com/sap/sse/security/ui/oauth/images/foursquare.png")
    public DataResource foursquareIconImageData();
}
