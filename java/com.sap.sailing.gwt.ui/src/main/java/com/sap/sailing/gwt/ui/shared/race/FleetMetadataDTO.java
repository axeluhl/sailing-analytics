package com.sap.sailing.gwt.ui.shared.race;

import com.google.gwt.user.client.rpc.IsSerializable;

public class FleetMetadataDTO implements IsSerializable, Comparable<FleetMetadataDTO> {

    private static final String DEFAULT_FLEET_COLOR = "#f0ab00";
    private String fleetName;
    private String fleetColor;
    private int ordering;

    @SuppressWarnings("unused")
    private FleetMetadataDTO() {
    }

    public FleetMetadataDTO(String fleetName, String fleetColor, int ordering) {
        super();
        this.fleetName = fleetName;
        this.fleetColor = fleetColor;
        this.ordering = ordering;
    }

    public String getFleetName() {
        return fleetName;
    }

    public String getFleetColor() {
        return fleetColor == null || fleetColor.isEmpty() ? DEFAULT_FLEET_COLOR : fleetColor;
    }
    
    public int getOrdering() {
        return ordering;
    }

    @Override
    public int compareTo(FleetMetadataDTO o) {
        if(ordering == o.ordering) {
            return fleetName.compareTo(o.fleetName);
        }
        return Integer.compare(ordering, o.ordering);
    }
}
