package com.sap.sse.security.ui.oauth.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sse.security.ui.oauth.client.component.LoginScreen;
import com.sap.sse.security.ui.oauth.client.model.CredentialDTO;
import com.sap.sse.security.ui.oauth.client.model.SocialUserDTO;
import com.sap.sse.security.ui.oauth.client.util.ClientUtils;
import com.sap.sse.security.ui.oauth.shared.OAuthLoginService;
import com.sap.sse.security.ui.oauth.shared.OAuthLoginServiceAsync;

public class OAuthLoginEntryPoint implements EntryPoint {

//    private static final Logger logger = Logger.getLogger(OAuthLoginEntryPoint.class.getName());

    private final OAuthLoginServiceAsync oauthLoginService = GWT.create(OAuthLoginService.class);

    LoginScreen loginScreen = new LoginScreen();

    public void onModuleLoad() {
        registerASyncService((ServiceDefTarget) oauthLoginService, "service/oauthlogin");
        GWT.log("Loading app..");
        setupLoginScreenHandlers();
        RootPanel.get().add(loginScreen);
        try {
            handleRedirect();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        updateLoginStatus();
    }

    public void updateLoginStatus() {
        // if there is a client side session show, Logout link
        // if (ClientUtils.alreadyLoggedIn())
        // {
        // //log("Already logged in..showing Logout anchor");
        // showLogoutAnchor();
        // }
        // else
        // {
        // //log("Showing Login anchor..");
        // showLoginAchor();
        // }
        updateLoginLabel();
    }

    private void updateLoginLabel() {
        String name = ClientUtils.getUsernameFromCookie();
        // String authProviderName = ClientUtils.getAuthProviderNameFromCookie();
        if (name != null) {
            // String labelStr = "Welcome " + "<font color=\"#006600\">"+ authProviderName + "</font>" + " user " +
            // "<font color=\"#006600\">" + name + "</font>";
            // updateWelcomeLabel(labelStr);

        } else {
            // updateWelcomeLabel(WELCOME_STRING);
        }
    }

    private void handleRedirect() throws Exception {
        if (ClientUtils.redirected()) {
            if (!ClientUtils.alreadyLoggedIn()) {
                verifySocialUser();
            }
        } else {
            // Window.alert("No redirection..");
        }
        updateLoginStatus();
    }

    private void verifySocialUser() throws Exception {
        final String authProviderName = ClientUtils.getAuthProviderNameFromCookie();
        final int authProvider = ClientUtils.getAuthProviderFromCookieAsInt();
        log("Verifying " + authProviderName + " user ...");

        oauthLoginService.verifySocialUser(ClientUtils.getCredential(), new AsyncCallback<SocialUserDTO>() {

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Coult not verify" + authProvider + " user." + caught);
            }

            @Override
            public void onSuccess(SocialUserDTO result) {
                ClientUtils.saveSessionId(result.getSessionId());

                String name = "";
                if (result.getName() != null) {
                    name = result.getName();
                } else if (result.getNickname() != null) // yahoo
                {
                    name = result.getNickname();
                } else if (result.getFirstName() != null) // linkedin
                {
                    name = result.getFirstName();
                    String lastName = result.getLastName();
                    if (lastName != null) {
                        name = name + " " + lastName;
                    }
                }

                log(authProviderName + " user '" + name + "' is verified!\n" + result.getJson());
                ClientUtils.saveUsername(name);
                updateLoginStatus();
            }
        });
    }

    private void setupLoginScreenHandlers() {

        loginScreen.getFacebookImage().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("Facebook");
                getAuthorizationUrl(authProvider);
            }
        });

        loginScreen.getGoogleImage().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("Google");
                getAuthorizationUrl(authProvider);
            }
        });

        loginScreen.getTwitterImage().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("Twitter");
                getAuthorizationUrl(authProvider);
            }
        });

        loginScreen.getYahooImage().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("Yahoo!");
                getAuthorizationUrl(authProvider);
            }
        });

        loginScreen.getLinkedinImage().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("Linkedin");
                getAuthorizationUrl(authProvider);
            }
        });

        loginScreen.getInstagramImage().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("Instagram");
                getAuthorizationUrl(authProvider);
            }
        });

        loginScreen.getVimeoImage().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("Vimeo");
                getAuthorizationUrl(authProvider);
            }
        });

        loginScreen.getGithubImage().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("github");
                getAuthorizationUrl(authProvider);
            }
        });

        loginScreen.getFlickrImage().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("flickr");
                getAuthorizationUrl(authProvider);
            }
        });

        loginScreen.getLiveImage().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("Windows Live");
                getAuthorizationUrl(authProvider);
            }
        });

        loginScreen.getTumblrImage().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("tumblr.");
                getAuthorizationUrl(authProvider);
            }
        });

        loginScreen.getFoursquareImage().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int authProvider = ClientUtils.getAuthProvider("foursquare");
                getAuthorizationUrl(authProvider);
            }
        });

        loginScreen.getBtnLogin().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                // clear all cookes first
                ClientUtils.clearCookies();
                // save the auth provider to cookie
                ClientUtils.saveAuthProvider(ClientUtils.DEFAULT);

                try {
                    verifySocialUser();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });

    }

    private void getAuthorizationUrl(final int authProvider) {
        String authProviderName = ClientUtils.getAuthProviderName(authProvider);
        final String callbackUrl = ClientUtils.getCallbackUrl();
        GWT.log("Getting authorization url");

        final CredentialDTO credential = new CredentialDTO();
        credential.setRedirectUrl(callbackUrl);
        credential.setAuthProvider(authProvider);
        
        oauthLoginService.getAuthorizationUrl(credential, new AsyncCallback<String>() {

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
                ClientUtils.redirect(authorizationUrl);
            }
        });
    }

    public void log(String msg) {
        GWT.log(msg);
    }
    
    protected void registerASyncService(ServiceDefTarget serviceToRegister, String servicePath) {
        String moduleBaseURL = GWT.getModuleBaseURL();
        String baseURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf('/', moduleBaseURL.length()-2)+1);
        
        serviceToRegister.setServiceEntryPoint(baseURL + servicePath);
    }
}
