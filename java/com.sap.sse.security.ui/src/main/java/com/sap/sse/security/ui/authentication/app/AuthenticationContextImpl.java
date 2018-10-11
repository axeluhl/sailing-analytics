package com.sap.sse.security.ui.authentication.app;

import java.util.ArrayList;

import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.shared.AccountDTO;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * Default implementation of {@link AuthenticationContext}.
 */
public class AuthenticationContextImpl implements AuthenticationContext {

    private final UserDTO currentUser;
    private final static UserDTO ANONYMOUS = new UserDTO("Anonymous", "", "", "", null, false, new ArrayList<AccountDTO>(),
            new ArrayList<Role>(), /* default tenant */ null, new ArrayList<WildcardPermission>(), /* groups */ null);
    private final UserService userService;

    /**
     * Creating an {@link AuthenticationContextImpl} containing the given {@link UserDTO} object.
     * 
     * @param currentUser
     *            the current {@link UserDTO user} object
     */
    public AuthenticationContextImpl(UserDTO currentUser, UserService userService) {
        this.userService = userService;
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
    
    public boolean hasPermission(WildcardPermission permission) {
        return userService.hasPermission(permission);
    }
}
