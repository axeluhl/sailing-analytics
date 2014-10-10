package com.sap.sse.security.ui.oauth.client.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sse.security.ui.client.StringMessages;
import com.sap.sse.security.ui.oauth.client.CredentialDTO;
import com.sap.sse.security.ui.oauth.client.resources.ImageResources;
import com.sap.sse.security.ui.oauth.client.util.ClientUtils;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class OAuthLoginPanel extends Composite {
    private static final StringMessages stringMessages = GWT.create(StringMessages.class);
    private static final String SAP_SAILING_OAUTH_WINDOW = "sap_sailing_oauth_verification_window";
    private VerticalPanel verticalPanel;
    private FlexTable flexTableLeft;

    private Image image;
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
    
    private PopupPanel popupPanel;
    private Frame popupFrame;
    
    public static ImageResources images = ImageResources.INSTANCE;

    private UserManagementServiceAsync userManagementService;

    public OAuthLoginPanel(UserManagementServiceAsync userManagementService) {
        this.userManagementService = userManagementService;
        verticalPanel = new VerticalPanel();
        verticalPanel.setWidth("100%");
        verticalPanel.setSpacing(4);
        initWidget(verticalPanel);

        HorizontalAlignmentConstant har = HasHorizontalAlignment.ALIGN_RIGHT;

        DecoratorPanel dp = new DecoratorPanel();

        /* left side */
        flexTableLeft = new FlexTable();
        dp.add(flexTableLeft);
        verticalPanel.add(dp);

        Label loginWithLabel = new Label("Login With"); // TODO i18n
        loginWithLabel.setWordWrap(false);
        // loginWithLabel.setStyleName(labelStyle);
        flexTableLeft.setWidget(0, 0, loginWithLabel);

        // row 1
        int r;
        int c;
        r = 1;
        c = 0;
        facebookImage = new Image(images.facebookIconImageData().getSafeUri());
        image = facebookImage;
        // image.setStyleName(imageStyle);
        image.setTitle("Login with Facebook"); // TODO i18n
        flexTableLeft.setWidget(r, c, image);

        r = 1;
        c = 1;
        googleImage = new Image(images.googleIconImageData().getSafeUri());
        image = googleImage;
        // image.setStyleName(imageStyle);
        image.setTitle("Login with Google"); // TODO i18n
//        flexTableLeft.setWidget(r, c, image);
        flexTableLeft.getCellFormatter().setHorizontalAlignment(r, c, har);

        r = 1;
        c = 2;
        twitterImage = new Image(images.twitterIconImageData().getSafeUri());
        image = twitterImage;
        // image.setStyleName(imageStyle);
        image.setTitle("Login with Twitter"); // TODO i18n
//        flexTableLeft.setWidget(r, c, image);
        flexTableLeft.getCellFormatter().setHorizontalAlignment(r, c, har);

        r = 1;
        c = 3;
        yahooImage = new Image(images.yahooIconImageData().getSafeUri());
        image = yahooImage;
        // image.setStyleName(imageStyle);
        image.setTitle("Login with Yahoo!"); // TODO i18n
//        flexTableLeft.setWidget(r, c, image);
        flexTableLeft.getCellFormatter().setHorizontalAlignment(r, c, har);

        // row 2
        r = 2;
        c = 0;
        linkedinImage = new Image(images.linkedinIconImageData().getSafeUri());
        image = linkedinImage;
        // image.setStyleName(imageStyle);
        image.setTitle("Login with LinkedIn"); // TODO i18n
//        flexTableLeft.setWidget(r, c, image);
        flexTableLeft.getCellFormatter().setHorizontalAlignment(r, c, har);

        r = 2;
        c = 1;
        instagramImage = new Image(images.instagramIconImageData().getSafeUri());
        image = instagramImage;
        // image.setStyleName(imageStyle);
        image.setTitle("Login with Instagram"); // TODO i18n
//        flexTableLeft.setWidget(r, c, image);
        flexTableLeft.getCellFormatter().setHorizontalAlignment(r, c, har);

        r = 2;
        c = 2;
        vimeoImage = new Image(images.vimeoIconImageData().getSafeUri());
        image = vimeoImage;
        // image.setStyleName(imageStyle);
        image.setTitle("Login with Vimeo"); // TODO i18n
//        flexTableLeft.setWidget(r, c, image);
        flexTableLeft.getCellFormatter().setHorizontalAlignment(r, c, har);

        r = 2;
        c = 3;
        githubImage = new Image(images.githubIconImageData().getSafeUri());
        image = githubImage;
        // image.setStyleName(imageStyle);
        image.setTitle("Login with github"); // TODO i18n
//        flexTableLeft.setWidget(r, c, image);
        flexTableLeft.getCellFormatter().setHorizontalAlignment(r, c, har);

        // row 3
        r = 3;
        c = 0;
        flickrImage = new Image(images.flickrIconImageData().getSafeUri());
        image = flickrImage;
        // image.setStyleName(imageStyle);
        image.setTitle("Login with flickr"); // TODO i18n
//        flexTableLeft.setWidget(r, c, image);
        flexTableLeft.getCellFormatter().setHorizontalAlignment(r, c, har);

        r = 3;
        c = 1;
        liveImage = new Image(images.liveIconImageData().getSafeUri());
        image = liveImage;
        // image.setStyleName(imageStyle);
        image.setTitle("Login with Microsoft Live Connect"); // TODO i18n
//        flexTableLeft.setWidget(r, c, image);
        flexTableLeft.getCellFormatter().setHorizontalAlignment(r, c, har);
        flexTableLeft.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        flexTableLeft.getFlexCellFormatter().setColSpan(0, 0, 4);
        flexTableLeft.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);

        r = 3;
        c = 2;
        tumblrImage = new Image(images.tumblrIconImageData().getSafeUri());
        image = tumblrImage;
        // image.setStyleName(imageStyle);
        image.setTitle("Login with tumblr."); // TODO i18n
//        flexTableLeft.setWidget(r, c, image);
        flexTableLeft.getCellFormatter().setHorizontalAlignment(r, c, har);
        flexTableLeft.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        flexTableLeft.getFlexCellFormatter().setColSpan(0, 0, 4);
        flexTableLeft.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);

        r = 3;
        c = 3;
        foursquareImage = new Image(images.foursquareIconImageData().getSafeUri());
        image = foursquareImage;
        // image.setStyleName(imageStyle);
        image.setTitle("Login with foursquare"); // TODO i18n
//        flexTableLeft.setWidget(r, c, image);
        flexTableLeft.getCellFormatter().setHorizontalAlignment(r, c, har);
        flexTableLeft.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        flexTableLeft.getFlexCellFormatter().setColSpan(0, 0, 4);
        flexTableLeft.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
        
        popupPanel = new PopupPanel(false);
        VerticalPanel popupContent = new VerticalPanel();
        popupPanel.add(popupContent);
        popupFrame = new Frame();
        popupFrame.setWidth("300px");
        popupFrame.setHeight("300px");
        popupFrame.getElement().getStyle().setBorderWidth(0, Unit.PX);
        popupContent.add(popupFrame);
        Button close = new Button(stringMessages.close(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popupPanel.hide();
            }
        });
        popupContent.add(close);

        setupLoginScreenHandlers();
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
