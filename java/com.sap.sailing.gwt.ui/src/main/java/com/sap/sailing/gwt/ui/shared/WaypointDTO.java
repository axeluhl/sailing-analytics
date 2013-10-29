package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.sap.sailing.domain.common.PassingInstructions;
import com.sap.sailing.domain.common.dto.NamedDTO;

public class WaypointDTO extends NamedDTO {
    private static final long serialVersionUID = 7439553659782967746L;

    public List<MarkDTO> marks;
    
    public ControlPointDTO controlPoint;
    
    public PassingInstructions passingInstructions;
    
    WaypointDTO() {}
    
    public WaypointDTO(String name, ControlPointDTO controlPoint, List<MarkDTO> marks, PassingInstructions passingInstructions) {
        super(name);
        this.setName(name);
        this.marks = marks;
        this.controlPoint = controlPoint;
        this.passingInstructions = passingInstructions;
    }
}
