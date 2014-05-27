package com.sap.sse.security.ui.shared;

import java.util.Collection;

import com.google.gwt.user.client.rpc.RemoteService;

public interface UserManagementService extends RemoteService {

    String sayHello();
    
    Collection<UserDTO> getUserList();
    
    Collection<UserDTO> getFilteredSortedUserList(String filter);
    
    UserDTO getCurrentUser();
    
    SuccessInfo login(String username, String password);
    
    UserDTO createSimpleUser(String name, String password);
    
    SuccessInfo deleteUser(String username);
    
    SuccessInfo logout();
    
    SuccessInfo addRoleForUser(String username, String role);
}
