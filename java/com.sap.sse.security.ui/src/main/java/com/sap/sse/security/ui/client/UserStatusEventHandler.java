package com.sap.sse.security.ui.client;

import com.sap.sse.security.ui.shared.UserDTO;

public interface UserStatusEventHandler {
    void onUserStatusChange(UserDTO user);
}
