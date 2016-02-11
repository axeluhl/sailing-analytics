package com.sap.sailing.gwt.home.shared.partials.userdetails;

import com.sap.sse.security.ui.shared.UserDTO;

public interface UserDetailsView {
    
    void setPresenter(Presenter presenter);
    
    void setUser(UserDTO user);
    
    void clearPasswordFields();
    
    public interface Presenter {
        void handleSaveChangesRequest(String fullName, String company);
        void handleEmailChangeRequest(String email);
        void handlePasswordChangeRequest(String oldPassword, String newPassword, String newPasswordConfirmation);
    }
}
