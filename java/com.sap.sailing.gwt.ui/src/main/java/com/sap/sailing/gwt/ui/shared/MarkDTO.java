package com.sap.sailing.gwt.ui.shared;

import java.util.Collections;

import com.sap.sailing.domain.common.MarkType;

public class MarkDTO extends ControlPointDTO {
    public PositionDTO position;
    public String color;
    public String shape;
    public String pattern;
    public MarkType type;

    MarkDTO() {}
    
    public MarkDTO(String idAsString, String name, double latDeg, double lngDeg) {
        super(idAsString, name);
        this.position = new PositionDTO(latDeg, lngDeg);
    }

    public MarkDTO(String idAsString, String name) {
        super(idAsString, name);
    }
    
    @Override
    public Iterable<MarkDTO> getMarks() {
        return Collections.singleton(this);
    }
}
