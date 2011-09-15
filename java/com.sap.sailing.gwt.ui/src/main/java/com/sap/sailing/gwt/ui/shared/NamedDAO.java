package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class NamedDAO implements IsSerializable {
    public String name;

    public NamedDAO() {}
    
    public NamedDAO(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}
