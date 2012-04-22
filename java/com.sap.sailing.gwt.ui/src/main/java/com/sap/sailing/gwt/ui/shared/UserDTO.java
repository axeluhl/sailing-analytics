package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class UserDTO implements IsSerializable {
    public String principalName;

    public String firstname;
    
    public String lastname;
    
    public List<String> roles;

    public UserDTO() {
    }

    public UserDTO(String principalName) {
        super();
        this.principalName = principalName;
    }
}
