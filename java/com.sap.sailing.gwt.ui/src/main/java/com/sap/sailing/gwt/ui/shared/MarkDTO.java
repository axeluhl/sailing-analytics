package com.sap.sailing.gwt.ui.shared;

import java.util.Collections;

public class MarkDTO extends ControlPointDTO {
    public PositionDTO position;
    public String color;
    public String shape;
    public String pattern;

    MarkDTO() {}
    
    public MarkDTO(String name, double latDeg, double lngDeg) {
        super(name);
        this.position = new PositionDTO(latDeg, lngDeg);
    }

    @Override
    public Iterable<MarkDTO> getMarks() {
        return Collections.singleton(this);
    }
}
