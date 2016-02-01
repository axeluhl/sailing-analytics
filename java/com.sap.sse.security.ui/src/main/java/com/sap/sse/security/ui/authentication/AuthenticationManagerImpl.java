package com.sap.sse.security.ui.authentication;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.authentication.app.AuthenticationContextImpl;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.WithSecurity;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;

public class AuthenticationManagerImpl implements AuthenticationManager {
    
    private final UserManagementServiceAsync userManagementService;
    private final UserService userService;
    private final EventBus eventBus;
    private final String emailConfirmationUrl;
    private final String passwordResetUrl;

    public AuthenticationManagerImpl(WithSecurity clientFactory, EventBus eventBus,
            String emailConfirmationUrl, String passwordResetUrl) {
        this(clientFactory.getUserManagementService(), clientFactory.getUserService(), eventBus, emailConfirmationUrl,
                passwordResetUrl);
    }
    
    public AuthenticationManagerImpl(UserService userService, EventBus eventBus, String emailConfirmationUrl,
            String passwordResetUrl) {
        this(userService.getUserManagementService(), userService, eventBus, emailConfirmationUrl, passwordResetUrl);
    }
    
    private AuthenticationManagerImpl(UserManagementServiceAsync userManagementService, UserService userService,
            final EventBus eventBus, String emailConfirmationUrl, String passwordResetUrl) {
        this.userManagementService = userManagementService;
        this.userService = userService;
        this.eventBus = eventBus;
        this.emailConfirmationUrl = emailConfirmationUrl;
        this.passwordResetUrl = passwordResetUrl;
        userService.addUserStatusEventHandler(new UserStatusEventHandler() {
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
        userManagementService
                .createSimpleUser(name, email, password, fullName, company, emailConfirmationUrl, callback);
    }
    
    @Override
    public void reqeustPasswordReset(String username, String eMailAddress, AsyncCallback<Void> callback) {
        userManagementService.resetPassword(username, eMailAddress, passwordResetUrl, callback);
    }
    
    @Override
    public void login(String username, String password, AsyncCallback<SuccessInfo> callback) {
        userService.login(username, password, callback);
    }
    
    @Override
    public void logout() {
        userService.logout();
        eventBus.fireEvent(new AuthenticationRequestEvent());
    }
    
    @Override
    public void refreshUserInfo() {
        userService.updateUser(true);
    }
    
    @Override
    public AuthenticationContext getAuthenticationContext() {
        return new AuthenticationContextImpl(userService.getCurrentUser());
    }
}
