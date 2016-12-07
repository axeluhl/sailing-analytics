package com.sap.sailing.android.shared.data;

import com.sap.sse.common.Base64Utils;

public class LoginData {

    private String username;
    private String password;

    public LoginData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getCredentials() {
        return Base64Utils.toBase64((username + ":" + password).getBytes());
    }
}
