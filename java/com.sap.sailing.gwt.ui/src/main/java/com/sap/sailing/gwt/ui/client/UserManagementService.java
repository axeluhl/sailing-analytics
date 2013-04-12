package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sap.sailing.gwt.ui.shared.UserDTO;

@RemoteServiceRelativePath("usermanagement")
public interface UserManagementService extends RemoteService {

    public boolean isUserInRole(String userRole);

    public void logoutUser();

    public UserDTO getUser();
}
