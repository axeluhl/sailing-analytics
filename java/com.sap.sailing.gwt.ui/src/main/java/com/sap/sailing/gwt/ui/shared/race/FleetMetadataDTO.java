package com.sap.sailing.gwt.ui.shared.race;

import com.google.gwt.user.client.rpc.IsSerializable;

public class FleetMetadataDTO implements IsSerializable {

    private String fleetName;
    private String fleetColor;

    @SuppressWarnings("unused")
    private FleetMetadataDTO() {
    }

    public FleetMetadataDTO(String fleetName, String fleetColor) {
        super();
        this.fleetName = fleetName;
        this.fleetColor = fleetColor;
    }

    public String getFleetName() {
        return fleetName;
    }

    public String getFleetColor() {
        return fleetColor;
    }
}
