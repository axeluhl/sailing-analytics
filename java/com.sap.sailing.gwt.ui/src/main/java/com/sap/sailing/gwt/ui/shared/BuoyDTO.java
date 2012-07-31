package com.sap.sailing.gwt.ui.shared;

import java.util.Collections;

public class BuoyDTO extends ControlPointDTO {
    public PositionDTO position;
    
    public BuoyDTO() {}
    
    public BuoyDTO(String name, double latDeg, double lngDeg) {
        super(name);
        position = new PositionDTO(latDeg, lngDeg);
    }

    @Override
    public Iterable<BuoyDTO> getBuoys() {
        return Collections.singleton(this);
    }
}
