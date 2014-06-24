package com.sap.sse.security.ui.shared;

import java.util.Collection;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sse.security.ui.oauth.client.CredentialDTO;
import com.sap.sse.security.ui.oauth.shared.OAuthException;

public interface UserManagementService extends RemoteService {

    String sayHello();
    
    Collection<UserDTO> getUserList();
    
    Collection<UserDTO> getFilteredSortedUserList(String filter);
    
    UserDTO getCurrentUser();
    
    SuccessInfo login(String username, String password);
    
    UserDTO createSimpleUser(String name, String email, String password);
    
    SuccessInfo deleteUser(String username);
    
    SuccessInfo logout();
    
    SuccessInfo addRoleForUser(String username, String role);
    
    Map<String, String> getSettings();
    
    Map<String, String> getSettingTypes();
    
    void setSetting(String key, String clazz, String setting);
    void addSetting(String key, String clazz, String setting);
    
    
    //------------------------------------------------ OAuth Interface ----------------------------------------------------------------------
    
    public String     getAuthorizationUrl(CredentialDTO credential) throws OAuthException;
    
    public UserDTO verifySocialUser(CredentialDTO credential) throws OAuthException;
}
