package com.sap.sse.security.ui.client;

import com.sap.sse.security.ui.shared.UserDTO;

public interface UserChangeEventHandler {
    void onUserChange(UserDTO user);
}
