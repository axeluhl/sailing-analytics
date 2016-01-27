package com.sap.sse.security.ui.authentication;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;

public interface AuthenticationManager {
    
    void createAccount(String name, String email, String password, String fullName, 
             String company, AsyncCallback<UserDTO> callback);
    
    void reqeustPasswordReset(String username, String eMailAddress, AsyncCallback<Void> callback);
    
    void login(String username, String password, AsyncCallback<SuccessInfo> callback);
    
    void logout();
    
    void refreshUserInfo();
    
    AuthenticationContext getAuthenticationContext();
}
