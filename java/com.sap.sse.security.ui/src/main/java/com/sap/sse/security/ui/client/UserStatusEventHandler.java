package com.sap.sse.security.ui.client;

import com.sap.sse.security.shared.dto.UserDTO;

public interface UserStatusEventHandler {
    /**
     * Called when something about the currently signed-in user changes. This could be that user now has a different set
     * of {@link UserDTO#getRoles() roles}.
     * 
     * @param user
     *            if <code>null</code>, a user is not currently signed-in
     * @param preAuthenticated
     *            <code>true</code>, when the authenticated user is determined on page load. <code>false</code> when
     *            logging in/out while the page is already open.
     */
    void onUserStatusChange(UserDTO user, boolean preAuthenticated);
}
