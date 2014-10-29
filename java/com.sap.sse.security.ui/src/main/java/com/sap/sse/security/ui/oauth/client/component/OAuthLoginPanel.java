package com.sap.sse.security.ui.oauth.client.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.sap.sse.security.ui.client.StringMessages;
import com.sap.sse.security.ui.loginpanel.Css;
import com.sap.sse.security.ui.oauth.client.CredentialDTO;
import com.sap.sse.security.ui.oauth.client.resources.ImageResources;
import com.sap.sse.security.ui.oauth.client.util.ClientUtils;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class OAuthLoginPanel extends HorizontalPanel {
    private static final StringMessages stringMessages = GWT.create(StringMessages.class);
    private static final String SAP_SAILING_OAUTH_WINDOW = "sap_sailing_oauth_verification_window";

    private Image facebookImage;
    private Image googleImage;
    private Image twitterImage;
    private Image linkedinImage;
    private Image yahooImage;
    private Image vimeoImage;
    private Image githubImage;
    private Image instagramImage;
    private Image flickrImage;
    private Image liveImage;
    private Image tumblrImage;
    private Image foursquareImage;
    
    public static ImageResources images = ImageResources.INSTANCE;

    private UserManagementServiceAsync userManagementService;

    public OAuthLoginPanel(UserManagementServiceAsync userManagementService, Css css) {
        this.userManagementService = userManagementService;
        Label signInWithLabel = new Label(stringMessages.signInWith());
        signInWithLabel.setWordWrap(false);
        add(signInWithLabel);

        facebookImage = new Image(images.facebookIconImageData().getSafeUri());
        addProviderImage(facebookImage, "Facebook", css);

        googleImage = new Image(images.googleIconImageData().getSafeUri());
        addProviderImage(googleImage, "Google", css);

        twitterImage = new Image(images.twitterIconImageData().getSafeUri());
        addProviderImage(twitterImage, "Twitter", css);

        yahooImage = new Image(images.yahooIconImageData().getSafeUri());
        addProviderImage(yahooImage, "Yahoo", css);

        linkedinImage = new Image(images.linkedinIconImageData().getSafeUri());
        addProviderImage(linkedinImage, "LinkedIn", css);

        instagramImage = new Image(images.instagramIconImageData().getSafeUri());
        addProviderImage(instagramImage, "Instagram", css);

        vimeoImage = new Image(images.vimeoIconImageData().getSafeUri());
        addProviderImage(vimeoImage, "Vimeo", css);

        githubImage = new Image(images.githubIconImageData().getSafeUri());
        addProviderImage(githubImage, "Github", css);

        flickrImage = new Image(images.flickrIconImageData().getSafeUri());
        addProviderImage(flickrImage, "Flickr", css);

        liveImage = new Image(images.liveIconImageData().getSafeUri());
        addProviderImage(liveImage, "Live", css);

        tumblrImage = new Image(images.tumblrIconImageData().getSafeUri());
        addProviderImage(tumblrImage, "Tumblr", css);

        foursquareImage = new Image(images.foursquareIconImageData().getSafeUri());
        addProviderImage(foursquareImage, "Foursquare", css);
        
        setupLoginScreenHandlers();
    }
    
    private void addProviderImage(Image image, String title, Css css) {
        image.setHeight("1.5em");
        image.setTitle(title);
        image.getElement().addClassName(css.providerIcon());
        add(image);
    }

    private void getAuthorizationUrl(final int authProvider) {
        // String authProviderName = ClientUtils.getAuthProviderName(authProvider);
        final String callbackUrl = ClientUtils.getCallbackUrl();
        GWT.log("Getting authorization url");
        final CredentialDTO credential = new CredentialDTO();
        credential.setRedirectUrl(callbackUrl);
        credential.setAuthProvider(authProvider);
        userManagementService.getAuthorizationUrl(credential, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientUtils.handleException(caught);
            }

            @Override
            public void onSuccess(String result) {
                String authorizationUrl = result;
                GWT.log("Authorization url: " + authorizationUrl);

                // clear all cookes first
                ClientUtils.clearCookies();

                // save the auth provider to cookie
                ClientUtils.saveAuthProvider(authProvider);

                // save the redirect url to a cookie as well
                // we need to redirect there after logout
                ClientUtils.saveRediretUrl(callbackUrl);

                // Window.alert("Redirecting to: " + authorizationUrl);
                Window.open(authorizationUrl, SAP_SAILING_OAUTH_WINDOW, "status=no,toolbar=no,location=no,menubar=no,width=640px,height=480px");
//                popupFrame.setUrl(authorizationUrl);
//                popupPanel.show();
//                openInNewTab(authorizationUrl);
            }
        });
    }
    
    public static native String openInNewTab(String url)/*-{
    return $wnd.open(url, 
    'target=_blank')
    }-*/;

    private void setupLoginScreenHandlers() {
        facebookImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("Facebook");
                getAuthorizationUrl(authProvider);
            }
        });

        googleImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("Google");
                getAuthorizationUrl(authProvider);
            }
        });

        twitterImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("Twitter");
                getAuthorizationUrl(authProvider);
            }
        });

        yahooImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("Yahoo!");
                getAuthorizationUrl(authProvider);
            }
        });

        linkedinImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("Linkedin");
                getAuthorizationUrl(authProvider);
            }
        });

        instagramImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("Instagram");
                getAuthorizationUrl(authProvider);
            }
        });

        vimeoImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("Vimeo");
                getAuthorizationUrl(authProvider);
            }
        });

        githubImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("github");
                getAuthorizationUrl(authProvider);
            }
        });

        flickrImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("flickr");
                getAuthorizationUrl(authProvider);
            }
        });

        liveImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("Windows Live");
                getAuthorizationUrl(authProvider);
            }
        });

        tumblrImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("tumblr.");
                getAuthorizationUrl(authProvider);
            }
        });

        foursquareImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("foursquare");
                getAuthorizationUrl(authProvider);
            }
        });

    }
}
