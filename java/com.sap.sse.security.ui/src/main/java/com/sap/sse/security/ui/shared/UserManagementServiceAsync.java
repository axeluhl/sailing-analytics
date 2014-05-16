package com.sap.sse.security.ui.shared;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UserManagementServiceAsync {

    void sayHello(AsyncCallback<String> callback);

    void getUserList(AsyncCallback<Collection<UserDTO>> callback);

    void getCurrentUser(AsyncCallback<UserDTO> callback);

    void login(String username, String password, AsyncCallback<String> callback);

    void logout(AsyncCallback<Void> callback);

}
