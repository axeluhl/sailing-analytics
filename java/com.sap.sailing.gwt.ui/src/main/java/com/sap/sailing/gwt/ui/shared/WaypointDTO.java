package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.sap.sailing.domain.common.NauticalSide;

public class WaypointDTO extends NamedDTO {
    public List<MarkDTO> marks;
    
    public ControlPointDTO controlPoint;
    
    public NauticalSide passingSide;
    
    WaypointDTO() {}
    
    public WaypointDTO(String name, ControlPointDTO controlPoint, List<MarkDTO> marks, NauticalSide passingSide) {
        super(name);
        this.name = name;
        this.marks = marks;
        this.controlPoint = controlPoint;
        this.passingSide = passingSide;
    }
}
