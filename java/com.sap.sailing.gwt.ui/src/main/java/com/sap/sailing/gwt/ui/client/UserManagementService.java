package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.gwt.ui.shared.UserDTO;

public interface UserManagementService extends RemoteService {

    public boolean isUserInRole(String userRole);

    public void logoutUser();

    public UserDTO getUser();
}
