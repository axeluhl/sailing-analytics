package com.sap.sse.security.ui.oauth.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;

public interface ImageResources extends ClientBundle {
    
    public static final ImageResources INSTANCE = GWT.create(ImageResources.class);

    @Source("../../images/facebook.png")
    public DataResource facebookIconImageData();

    @Source("../../images/flickr.png")
    public DataResource flickrIconImageData();

    @Source("../../images/github.png")
    public DataResource githubIconImageData();

    @Source("../../images/google.png")
    public DataResource googleIconImageData();

    @Source("../../images/linkedin.png")
    public DataResource linkedinIconImageData();

    @Source("../../images/twitter.png")
    public DataResource twitterIconImageData();

    @Source("../../images/vimeo.png")
    public DataResource vimeoIconImageData();

    @Source("../../images/wordpress.png")
    public DataResource wordpressIconImageData();

    @Source("../../images/yahoo.png")
    public DataResource yahooIconImageData();
    
    @Source("../../images/instagram.png")
    public DataResource instagramIconImageData();
    
    @Source("../../images/imgur.png")
    public DataResource imgurIconImageData();
    
    @Source("../../images/live32.png")
    public DataResource liveIconImageData();
    
    @Source("../../images/tumblr.png")
    public DataResource tumblrIconImageData();
    
    @Source("../../images/foursquare.png")
    public DataResource foursquareIconImageData();
}
