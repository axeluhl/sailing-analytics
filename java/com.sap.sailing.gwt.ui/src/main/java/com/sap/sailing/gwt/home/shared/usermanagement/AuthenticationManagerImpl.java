package com.sap.sailing.gwt.home.shared.usermanagement;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.shared.usermanagement.app.UserManagementContext;
import com.sap.sailing.gwt.home.shared.usermanagement.app.UserManagementContextImpl;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.WithSecurity;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;

public class AuthenticationManagerImpl implements AuthenticationManager {
    private final WithSecurity clientFactory;
    private UserManagementContext uCtx = new UserManagementContextImpl();
    private final EventBus eventBus;

    public AuthenticationManagerImpl(final WithSecurity clientFactory, final EventBus eventBus) {
        this.clientFactory = clientFactory;
        this.eventBus = eventBus;
        clientFactory.getUserService().addUserStatusEventHandler(new UserStatusEventHandler() {
            @Override
            public void onUserStatusChange(UserDTO user) {
                uCtx = new UserManagementContextImpl(user);
                eventBus.fireEvent(new UserManagementContextEvent(uCtx));
            }
        });
        eventBus.addHandler(UserManagementRequestEvent.TYPE, new UserManagementRequestEvent.Handler() {
            @Override
            public void onUserManagementRequestEvent(UserManagementRequestEvent event) {
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
    public UserManagementContext getAuthenticationContext() {
        return uCtx;
    }
    
    @Override
    public void didLogout() {
        uCtx = new UserManagementContextImpl();
        clientFactory.getUserService().updateUser(true);
        eventBus.fireEvent(new UserManagementRequestEvent());
    }

    @Override
    public void didLogin(UserDTO user) {
        uCtx = new UserManagementContextImpl(user);
        clientFactory.getUserService().updateUser(true);
        eventBus.fireEvent(new UserManagementContextEvent(uCtx));
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
}
