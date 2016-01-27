package com.sap.sse.security.ui.authentication;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.authentication.app.AuthenticationContextImpl;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.WithSecurity;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;

public class AuthenticationManagerImpl implements AuthenticationManager {
    
    private final WithSecurity clientFactory;
    private final EventBus eventBus;

    public AuthenticationManagerImpl(final WithSecurity clientFactory, final EventBus eventBus) {
        this.clientFactory = clientFactory;
        this.eventBus = eventBus;
        clientFactory.getUserService().addUserStatusEventHandler(new UserStatusEventHandler() {
            @Override
            public void onUserStatusChange(UserDTO user) {
                eventBus.fireEvent(new AuthenticationContextEvent(new AuthenticationContextImpl(user)));
            }
        });
        eventBus.addHandler(AuthenticationRequestEvent.TYPE, new AuthenticationRequestEvent.Handler() {
            @Override
            public void onUserManagementRequestEvent(AuthenticationRequestEvent event) {
                if (!event.isLogin()) {
                    logout();
                }
            }
        });
    }

    @Override
    public void createAccount(String name, String email, String password, String fullName, String company,
            String validationBaseURL, AsyncCallback<UserDTO> callback) {
        clientFactory.getUserManagementService().createSimpleUser(name, email, password, fullName, company,
                validationBaseURL, callback);
    }
    
    @Override
    public void reqeustPasswordReset(String username, String eMailAddress, String baseURL, AsyncCallback<Void> callback) {
        clientFactory.getUserManagementService().resetPassword(username, eMailAddress, baseURL, callback);
    }
    
    @Override
    public void login(String username, String password, AsyncCallback<SuccessInfo> callback) {
        clientFactory.getUserService().login(username, password, callback);
    }
    
    @Override
    public void logout() {
        clientFactory.getUserService().logout();
        eventBus.fireEvent(new AuthenticationRequestEvent());
    }
    
    @Override
    public void refreshUserInfo() {
        clientFactory.getUserService().updateUser(true);
    }
    
    @Override
    public AuthenticationContext getAuthenticationContext() {
        return new AuthenticationContextImpl(clientFactory.getUserService().getCurrentUser());
    }
}
