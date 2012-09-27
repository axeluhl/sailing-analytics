package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class VenueDTO extends NamedDTO implements IsSerializable {
    public VenueDTO() {
    }

    public VenueDTO(String name) {
        super(name);
    }
}
