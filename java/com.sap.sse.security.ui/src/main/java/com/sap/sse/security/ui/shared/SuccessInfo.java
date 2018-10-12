package com.sap.sse.security.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Util.Pair;

public class SuccessInfo implements IsSerializable {

    public static final long serialVersionUID = -3044914225885460520L;

    public static final String FAILED_TO_LOGIN = "Failed to login.";
    
    private boolean successful;
    private String message;
    private Pair<UserDTO, UserDTO> userDTO;
    private String redirectURL;
    
    SuccessInfo() {} // for serializtion only
    
    public SuccessInfo(boolean successful, String message, String redirectURL, Pair<UserDTO, UserDTO> userDTO) {
        super();
        this.successful = successful;
        this.message = message;
        this.redirectURL = redirectURL;
        this.userDTO = userDTO;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getMessage() {
        return message;
    }

    public Pair<UserDTO, UserDTO> getUserDTO() {
        return userDTO;
    }

    public String getRedirectURL() {
        return redirectURL;
    }
    
}
