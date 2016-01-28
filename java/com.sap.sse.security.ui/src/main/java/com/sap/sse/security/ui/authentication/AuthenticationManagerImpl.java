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
    private final String emailConfirmationUrl;
    private final String passwordResetUrl;

    public AuthenticationManagerImpl(WithSecurity clientFactory, final EventBus eventBus,
            String emailConfirmationUrl, String passwordResetUrl) {
        this.clientFactory = clientFactory;
        this.eventBus = eventBus;
        this.emailConfirmationUrl = emailConfirmationUrl;
        this.passwordResetUrl = passwordResetUrl;
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
    public void createAccount(String name, String email, String password, String fullName, 
            String company, AsyncCallback<UserDTO> callback) {
        clientFactory.getUserManagementService().createSimpleUser(name, email, password, 
                fullName, company, emailConfirmationUrl, callback);
    }
    
    @Override
    public void reqeustPasswordReset(String username, String eMailAddress, AsyncCallback<Void> callback) {
        clientFactory.getUserManagementService().resetPassword(username, eMailAddress, passwordResetUrl, callback);
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
