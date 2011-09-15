package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BoatClassDAO extends NamedDAO implements IsSerializable {
    public BoatClassDAO() {}

    public BoatClassDAO(String name) {
        super(name);
    }
    
}
