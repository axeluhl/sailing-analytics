package com.sap.sse.security.ui.oauth.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sse.security.ui.oauth.client.component.LoginScreen;
import com.sap.sse.security.ui.oauth.client.util.ClientUtils;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class OAuthLoginEntryPoint implements EntryPoint {

//    private static final Logger logger = Logger.getLogger(OAuthLoginEntryPoint.class.getName());

    private final UserManagementServiceAsync userManagementService = GWT.create(UserManagementService.class);

    private LoginScreen loginScreen = new LoginScreen();
    private TextBox status = new TextBox();

    public void onModuleLoad() {
        registerASyncService((ServiceDefTarget) userManagementService, "service/usermanagement");
        GWT.log("Loading app..");
        setupLoginScreenHandlers();
        FlowPanel fp = new FlowPanel();
        fp.add(loginScreen);
        fp.add(status);
        RootPanel.get().add(fp);
        try {
            handleRedirect();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    private void handleRedirect() throws Exception {
        if (ClientUtils.redirected()) {
            if (!ClientUtils.alreadyLoggedIn()) {
                status.setText("Trying to verify social user...");
                verifySocialUser();
            }
            else {
                status.setText("Fetching user information...");
                userManagementService.getCurrentUser(new AsyncCallback<UserDTO>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Could not get user information!");
                        status.setText("Could not get user information!" + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(UserDTO result) {
                        if (result == null){
                            status.setText("Not logged in!");
                        }
                        else {
                            status.setText("Logged in as:" + result.getName());
                        }
                    }
                });
            }
        } else {
            userManagementService.getCurrentUser(new AsyncCallback<UserDTO>() {

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Could not get user information!");
                    status.setText("Could not get user information!" + caught.getMessage());
                }

                @Override
                public void onSuccess(UserDTO result) {
                    if (result == null){
                        status.setText("Not logged in!");
                    }
                    else {
                        status.setText("Logged in as:" + result.getName());
                    }
                }
            });
        }
    }

    private void verifySocialUser() throws Exception {
        final String authProviderName = ClientUtils.getAuthProviderNameFromCookie();
        log("Verifying " + authProviderName + " user ...");

        userManagementService.verifySocialUser(ClientUtils.getCredential(), new AsyncCallback<UserDTO>() {

            @Override
            public void onFailure(Throwable caught) {
                status.setText("Could not log in! Reason:" + caught.getMessage());
            }

            @Override
            public void onSuccess(UserDTO result) {
                if (result == null){
                    status.setText("Could not log in!");
                    return;
                }
                String name = result.getName();

                log(authProviderName + " user '" + name + "' is verified!\n");
                ClientUtils.saveUsername(name);
                status.setText("Logged in as:" + name);
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
//        String authProviderName = ClientUtils.getAuthProviderName(authProvider);
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
