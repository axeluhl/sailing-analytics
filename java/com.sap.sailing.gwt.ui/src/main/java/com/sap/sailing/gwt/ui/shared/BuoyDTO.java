package com.sap.sailing.gwt.ui.shared;

import java.util.Collections;

public class BuoyDTO extends ControlPointDTO {
    public PositionDTO position;
    public String color;
    public String shape;
    public String pattern;

    BuoyDTO() {}
    
    public BuoyDTO(String name, double latDeg, double lngDeg) {
        super(name);
        this.position = new PositionDTO(latDeg, lngDeg);
    }

    @Override
    public Iterable<BuoyDTO> getBuoys() {
        return Collections.singleton(this);
    }
}
