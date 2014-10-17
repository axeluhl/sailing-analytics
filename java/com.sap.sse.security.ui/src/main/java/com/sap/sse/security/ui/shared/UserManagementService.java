package com.sap.sse.security.ui.shared;

import java.util.Collection;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.ui.oauth.client.CredentialDTO;
import com.sap.sse.security.ui.oauth.shared.OAuthException;

public interface UserManagementService extends RemoteService {
    Collection<UserDTO> getUserList();

    Collection<UserDTO> getFilteredSortedUserList(String filter);

    UserDTO getCurrentUser();

    SuccessInfo login(String username, String password);

    UserDTO createSimpleUser(String name, String email, String password) throws UserManagementException;
    
    void updateSimpleUserPassword(String name, String oldPassword, String newPassword) throws UserManagementException;

    void updateSimpleUserEmail(String username, String newEmail) throws UserManagementException;

    SuccessInfo deleteUser(String username);

    SuccessInfo logout();

    SuccessInfo setRolesForUser(String username, Iterable<String> roles);

    Map<String, String> getSettings();

    Map<String, String> getSettingTypes();

    void setSetting(String key, String clazz, String setting);

    void addSetting(String key, String clazz, String setting);

    // ------------------------------------------------ OAuth Interface --------------------------------------------------------------

    public String getAuthorizationUrl(CredentialDTO credential) throws OAuthException;

    public UserDTO verifySocialUser(CredentialDTO credential) throws OAuthException;

}
