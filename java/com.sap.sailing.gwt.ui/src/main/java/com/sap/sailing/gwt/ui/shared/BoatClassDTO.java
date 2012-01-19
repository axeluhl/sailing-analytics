package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BoatClassDTO extends NamedDTO implements IsSerializable {
    public BoatClassDTO() {}

    public BoatClassDTO(String name) {
        super(name);
    }
    
}
