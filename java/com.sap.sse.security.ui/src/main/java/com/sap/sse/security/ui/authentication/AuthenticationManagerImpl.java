package com.sap.sse.security.ui.authentication;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.authentication.app.AuthenticationContextImpl;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.WithSecurity;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;

public class AuthenticationManagerImpl implements AuthenticationManager {
    private final WithSecurity clientFactory;
    private AuthenticationContext uCtx = new AuthenticationContextImpl();
    private final EventBus eventBus;

    public AuthenticationManagerImpl(final WithSecurity clientFactory, final EventBus eventBus) {
        this.clientFactory = clientFactory;
        this.eventBus = eventBus;
        clientFactory.getUserService().addUserStatusEventHandler(new UserStatusEventHandler() {
            @Override
            public void onUserStatusChange(UserDTO user) {
                uCtx = new AuthenticationContextImpl(user);
                eventBus.fireEvent(new AuthenticationContextEvent(uCtx));
            }
        });
        eventBus.addHandler(AuthenticationRequestEvent.TYPE, new AuthenticationRequestEvent.Handler() {
            @Override
            public void onUserManagementRequestEvent(AuthenticationRequestEvent event) {
                if (!event.isLogin()) {
                    clientFactory.getUserManagementService().logout(new AsyncCallback<SuccessInfo>() {
                        @Override
                        public void onSuccess(SuccessInfo result) {
                            didLogout();
                        }
                        
                        @Override
                        public void onFailure(Throwable caught) {
                            didLogout();
                        }
                    });
                }
            }
        });
    }

    @Override
    public AuthenticationContext getAuthenticationContext() {
        return uCtx;
    }
    
    @Override
    public void didLogout() {
        uCtx = new AuthenticationContextImpl();
        clientFactory.getUserService().updateUser(true);
        eventBus.fireEvent(new AuthenticationRequestEvent());
    }

    @Override
    public void didLogin(UserDTO user) {
        uCtx = new AuthenticationContextImpl(user);
        clientFactory.getUserService().updateUser(true);
        eventBus.fireEvent(new AuthenticationContextEvent(uCtx));
    }
    
    @Override
    public void refreshUser() {
        clientFactory.getUserManagementService().getCurrentUser(new AsyncCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO result) {
                didLogin(result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
            }
        });
    }
    
    // TODO Remove after refactor accesst UserManagementServiceAsync
    UserManagementServiceAsync getUserManagementService() {
        return clientFactory.getUserManagementService();
    }
}
