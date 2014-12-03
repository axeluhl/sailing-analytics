package com.sap.sse.security.ui.client;

import com.sap.sse.security.ui.shared.UserDTO;

public interface UserStatusEventHandler {
    /**
     * Called when something about the currently signed-in user changes. This could be that user now has
     * a different set of {@link UserDTO#getRoles() roles}.
     * 
     * @param user if <code>null</code>, a user is not currently signed-in
     */
    void onUserStatusChange(UserDTO user);
}
