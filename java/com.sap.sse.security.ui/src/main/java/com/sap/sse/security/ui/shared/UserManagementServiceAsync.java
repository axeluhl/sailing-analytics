package com.sap.sse.security.ui.shared;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UserManagementServiceAsync {

    void sayHello(AsyncCallback<String> callback);

    void getUserList(AsyncCallback<Collection<UserDTO>> callback);

    void getCurrentUser(AsyncCallback<UserDTO> callback);

    void login(String username, String password, AsyncCallback<SuccessInfo> callback);

    void logout(AsyncCallback<SuccessInfo> callback);

    void createSimpleUser(String name, String password, AsyncCallback<UserDTO> callback);

    void getFilteredSortedUserList(String filter, AsyncCallback<Collection<UserDTO>> callback);

    void addRoleForUser(String username, String role, AsyncCallback<SuccessInfo> callback);

    void deleteUser(String username, AsyncCallback<SuccessInfo> callback);

}
