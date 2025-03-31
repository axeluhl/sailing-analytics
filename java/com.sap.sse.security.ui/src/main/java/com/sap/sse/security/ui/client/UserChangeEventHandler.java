package com.sap.sse.security.ui.client;

import com.sap.sse.security.shared.dto.UserDTO;

public interface UserChangeEventHandler {
    void onUserChange(UserDTO user);
}
