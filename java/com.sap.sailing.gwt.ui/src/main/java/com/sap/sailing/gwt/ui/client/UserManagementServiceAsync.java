package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.UserDTO;

public interface UserManagementServiceAsync {

    void isUserInRole(String userRole, AsyncCallback<Boolean> callback);

    void getUser(AsyncCallback<UserDTO> callback);

    void logoutUser(AsyncCallback<Void> callback);

}
