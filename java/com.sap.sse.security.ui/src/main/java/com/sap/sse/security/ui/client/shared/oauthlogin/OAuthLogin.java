package com.sap.sse.security.ui.client.shared.oauthlogin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.oauth.client.CredentialDTO;
import com.sap.sse.security.ui.oauth.client.util.ClientUtils;

public class OAuthLogin extends Composite {
    private static OAuthLoginUiBinder uiBinder = GWT.create(OAuthLoginUiBinder.class);

    interface OAuthLoginUiBinder extends UiBinder<Widget, OAuthLogin> {
    }
    
    private static final String SAP_SAILING_OAUTH_WINDOW = "sap_sailing_oauth_verification_window";

    @UiField Anchor facebookAnchor;
    @UiField Anchor googleAnchor;
    @UiField Anchor twitterAnchor;
    @UiField Anchor linkedinAnchor;
    @UiField Anchor yahooAnchor;
    @UiField Anchor vimeoAnchor;
    @UiField Anchor githubAnchor;
    @UiField Anchor instagramAnchor;
    @UiField Anchor flickrAnchor;
    @UiField Anchor liveAnchor;
    @UiField Anchor tumblrAnchor;
    @UiField Anchor foursquareAnchor;

    public static OAuthLoginResources images = OAuthLoginResources.INSTANCE;

    private UserManagementServiceAsync userManagementService;

    public OAuthLogin(UserManagementServiceAsync userManagementService) {
        this.userManagementService = userManagementService;
        
        OAuthLoginResources.INSTANCE.css().ensureInjected();

        initWidget(uiBinder.createAndBindUi(this));
    }

    private void retreiveAndStoreAuthorizationUrl(final int authProvider) {
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

                // clear all cookies first
                ClientUtils.clearCookies();

                // save the auth provider to cookie
                ClientUtils.saveAuthProvider(authProvider);

                // save the redirect url to a cookie as well
                // we need to redirect there after logout
                ClientUtils.saveRediretUrl(callbackUrl);

                Window.open(authorizationUrl, SAP_SAILING_OAUTH_WINDOW, "status=no,toolbar=no,location=no,menubar=no,width=640px,height=480px");
//                popupFrame.setUrl(authorizationUrl);
//                popupPanel.show();
            }
        });
    }
    
    @UiHandler("facebookAnchor")
    void facebookAnchorClicked(ClickEvent event) {
        final int authProvider = ClientUtils.getAuthProvider("Facebook");
        retreiveAndStoreAuthorizationUrl(authProvider);
    }

    @UiHandler("googleAnchor")
    void googleAnchorClicked(ClickEvent event) {
        final int authProvider = ClientUtils.getAuthProvider("Google");
        retreiveAndStoreAuthorizationUrl(authProvider);
    }

    @UiHandler("twitterAnchor")
    void twitterAnchorClicked(ClickEvent event) {
        final int authProvider = ClientUtils.getAuthProvider("Twitter");
        retreiveAndStoreAuthorizationUrl(authProvider);
    }

    @UiHandler("yahooAnchor")
    void yahooAnchorClicked(ClickEvent event) {
        final int authProvider = ClientUtils.getAuthProvider("Yahoo!");
        retreiveAndStoreAuthorizationUrl(authProvider);
    }

    @UiHandler("linkedinAnchor")
    void linkedinAnchorClicked(ClickEvent event) {
        final int authProvider = ClientUtils.getAuthProvider("Linkedin");
        retreiveAndStoreAuthorizationUrl(authProvider);
    }

    @UiHandler("instagramAnchor")
    void instagramAnchorClicked(ClickEvent event) {
        final int authProvider = ClientUtils.getAuthProvider("Instagram");
        retreiveAndStoreAuthorizationUrl(authProvider);
    }

    @UiHandler("vimeoAnchor")
    void vimeoAnchorClicked(ClickEvent event) {
        final int authProvider = ClientUtils.getAuthProvider("Vimeo");
        retreiveAndStoreAuthorizationUrl(authProvider);
    }

    @UiHandler("githubAnchor")
    void githubAnchorClicked(ClickEvent event) {
        final int authProvider = ClientUtils.getAuthProvider("github");
        retreiveAndStoreAuthorizationUrl(authProvider);
    }

    @UiHandler("flickrAnchor")
    void flickrAnchorClicked(ClickEvent event) {
        final int authProvider = ClientUtils.getAuthProvider("flickr");
        retreiveAndStoreAuthorizationUrl(authProvider);
    }

    @UiHandler("liveAnchor")
    void liveAnchorClicked(ClickEvent event) {
        final int authProvider = ClientUtils.getAuthProvider("Windows Live");
        retreiveAndStoreAuthorizationUrl(authProvider);
    }

    @UiHandler("tumblrAnchor")
    void tumblrAnchorClicked(ClickEvent event) {
        final int authProvider = ClientUtils.getAuthProvider("tumblr.");
        retreiveAndStoreAuthorizationUrl(authProvider);
    }

    @UiHandler("foursquareAnchor")
    void foursquareAnchorClicked(ClickEvent event) {
        final int authProvider = ClientUtils.getAuthProvider("foursquare");
        retreiveAndStoreAuthorizationUrl(authProvider);
    }
}
