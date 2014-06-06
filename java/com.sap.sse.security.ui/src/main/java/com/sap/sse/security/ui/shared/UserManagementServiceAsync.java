package com.sap.sse.security.ui.shared;

import java.util.Collection;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.security.ui.oauth.client.CredentialDTO;

public interface UserManagementServiceAsync {

    void sayHello(AsyncCallback<String> callback);

    void getUserList(AsyncCallback<Collection<UserDTO>> callback);

    void getCurrentUser(AsyncCallback<UserDTO> callback);

    void login(String username, String password, AsyncCallback<SuccessInfo> callback);

    void logout(AsyncCallback<SuccessInfo> callback);

    void createSimpleUser(String name, String email, String password, AsyncCallback<UserDTO> callback);

    void getFilteredSortedUserList(String filter, AsyncCallback<Collection<UserDTO>> callback);

    void addRoleForUser(String username, String role, AsyncCallback<SuccessInfo> callback);

    void deleteUser(String username, AsyncCallback<SuccessInfo> callback);

    void getSettings(AsyncCallback<Map<String, String>> callback);

    void setSetting(String key, String clazz, String setting, AsyncCallback<Void> callback);

    void getSettingTypes(AsyncCallback<Map<String, String>> callback);
    
  //------------------------------------------------ OAuth Interface ----------------------------------------------------------------------

    void getAuthorizationUrl(CredentialDTO credential, AsyncCallback<String> callback);

    void verifySocialUser(CredentialDTO credential, AsyncCallback<UserDTO> callback);

    void getAccessToken(String sessionId, AsyncCallback<String> callback);

}
