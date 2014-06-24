package com.sap.sse.security.ui.oauth.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.security.ui.loginpanel.LoginPanel;
import com.sap.sse.security.ui.oauth.client.component.OAuthLoginPanel;
import com.sap.sse.security.ui.oauth.client.util.ClientUtils;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class OAuthLoginEntryPoint implements EntryPoint {

//    private static final Logger logger = Logger.getLogger(OAuthLoginEntryPoint.class.getName());

    private final UserManagementServiceAsync userManagementService = GWT.create(UserManagementService.class);

    private OAuthLoginPanel loginScreen = new OAuthLoginPanel(userManagementService);
    private FlowPanel content = new FlowPanel();

    public void onModuleLoad() {
        registerASyncService((ServiceDefTarget) userManagementService, "service/usermanagement");
        
        RootPanel.get().add(content);
        setContentMessage("Loading...");
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
                setContentMessage("Trying to verify user...");
                verifySocialUser();
            }
            else {
                setContentMessage("Fetching user information...");
                userManagementService.getCurrentUser(new AsyncCallback<UserDTO>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        setContent(loginScreen);
                    }

                    @Override
                    public void onSuccess(UserDTO result) {
                        if (result == null){
                            setContent(loginScreen);
                        }
                        else {
                            LoginPanel.fireUserUpdateEvent();
                            setLoggedInContent(result);
                            closeWindow();
                        }
                    }
                });
            }
        } else {
            userManagementService.getCurrentUser(new AsyncCallback<UserDTO>() {

                @Override
                public void onFailure(Throwable caught) {
                    setContent(loginScreen);
                }

                @Override
                public void onSuccess(UserDTO result) {
                    if (result == null){
                        setContent(loginScreen);
                    }
                    else {
                        LoginPanel.fireUserUpdateEvent();
                        setLoggedInContent(result);
                        closeWindow();
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
                setContent(loginScreen);
            }

            @Override
            public void onSuccess(UserDTO result) {
                if (result == null){
                    setContent(loginScreen);
                    return;
                }
                String name = result.getName();

                log(authProviderName + " user '" + name + "' is verified!\n");
                ClientUtils.saveUsername(name);
                setLoggedInContent(result);
                LoginPanel.fireUserUpdateEvent();
                closeWindow();
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
    
    private void setContentMessage(String msg){
        content.clear();
        content.add(new Label(msg));
    }
    
    private void setContent(Widget w){
        content.clear();
        content.add(w);
    }
    
    private void setLoggedInContent(UserDTO user){
        content.clear();
        content.add(new Label("Logged in as " + user.getName()));
        content.add(new Button("Close", new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                closeWindow();
            }
        }));
    }
    
    public static native void closeWindow()/*-{
        $doc.windowCloser();
    }-*/;
}
