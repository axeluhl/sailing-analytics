package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BoatClassDAO implements IsSerializable {
    public String name;

    public BoatClassDAO() {}

    public BoatClassDAO(String name) {
        super();
        this.name = name;
    }
    
}
