package com.sap.sailing.gwt.home.shared.usermanagement.app;

import java.util.ArrayList;

import com.sap.sse.security.ui.shared.AccountDTO;
import com.sap.sse.security.ui.shared.UserDTO;

public class UserManagementContextImpl implements UserManagementContext {

    private final UserDTO currentUser;
    private final static UserDTO ANONYMOUS = new UserDTO("Anonymous", "", "", "", false, new ArrayList<AccountDTO>(),
            new ArrayList<String>(), new ArrayList<String>());

    public UserManagementContextImpl() {
        this.currentUser = ANONYMOUS;
    }

    public UserManagementContextImpl(UserDTO currentUser) {
        if (currentUser == null) {
            this.currentUser = ANONYMOUS;
        } else {
            this.currentUser = currentUser;
        }
    }

    @Override
    public boolean isLoggedIn() {
        return (currentUser != ANONYMOUS);
    }

    @Override
    public UserDTO getCurrentUser() {
        return currentUser;
    }
    
    @Override
    public String getUserTitle() {
        final String fullName = currentUser.getFullName();
        if(fullName != null && !fullName.isEmpty()) {
            return fullName;
        }
        return currentUser.getName();
    }

    @Override
    public String getUserSubtitle() {
        final String company = currentUser.getCompany();
        if(company != null && !company.isEmpty()) {
            return company;
        }
        final String email = currentUser.getEmail();
        if(email != null && !email.isEmpty()) {
            return email;
        }
        return currentUser.getName();
    }
}
