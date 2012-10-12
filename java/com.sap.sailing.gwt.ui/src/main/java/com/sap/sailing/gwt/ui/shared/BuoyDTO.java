package com.sap.sailing.gwt.ui.shared;

import java.util.Collections;

public class BuoyDTO extends ControlPointDTO {
    public PositionDTO position;

    public String displayColor;

    public BuoyDTO() {}
    
    public BuoyDTO(String name, double latDeg, double lngDeg, String displayColor) {
        super(name);
        this.position = new PositionDTO(latDeg, lngDeg);
        this.displayColor = displayColor;
    }

    @Override
    public Iterable<BuoyDTO> getBuoys() {
        return Collections.singleton(this);
    }
}
