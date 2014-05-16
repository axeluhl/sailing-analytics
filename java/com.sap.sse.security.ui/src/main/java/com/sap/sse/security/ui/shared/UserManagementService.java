package com.sap.sse.security.ui.shared;

import java.util.Collection;

import com.google.gwt.user.client.rpc.RemoteService;

public interface UserManagementService extends RemoteService {

    String sayHello();
    
    Collection<UserDTO> getUserList();
    
    UserDTO getCurrentUser();
    
    String login(String username, String password);
    
    void logout();
}
