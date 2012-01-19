package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class NamedDTO implements IsSerializable {
    public String name;

    public NamedDTO() {}
    
    public NamedDTO(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}
