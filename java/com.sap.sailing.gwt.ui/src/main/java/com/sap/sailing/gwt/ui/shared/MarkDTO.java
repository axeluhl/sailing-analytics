package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.common.MarkType;

public class MarkDTO extends ControlPointDTO {
    public PositionDTO position;
    public String color;
    public String shape;
    public String pattern;
    public MarkType type;

    MarkDTO() {}
    
    public MarkDTO(Serializable id, String name, double latDeg, double lngDeg) {
        super(id, name);
        this.position = new PositionDTO(latDeg, lngDeg);
    }

    @Override
    public Iterable<MarkDTO> getMarks() {
        return Collections.singleton(this);
    }
}
