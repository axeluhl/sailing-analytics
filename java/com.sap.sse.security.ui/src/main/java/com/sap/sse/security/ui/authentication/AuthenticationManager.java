package com.sap.sse.security.ui.authentication;

import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;

public interface AuthenticationManager {
    
    void createAccount(String name, String email, String password, String fullName, 
             String company, SuccessCallback<UserDTO> callback);
    
    void reqeustPasswordReset(String username, String eMailAddress, SuccessCallback<Void> callback);
    
    void login(String username, String password, SuccessCallback<SuccessInfo> callback);
    
    void logout();
    
    void refreshUserInfo();
    
    AuthenticationContext getAuthenticationContext();
    
    public interface SuccessCallback<T> {
        
        void onSuccess(T result);
    }
}
