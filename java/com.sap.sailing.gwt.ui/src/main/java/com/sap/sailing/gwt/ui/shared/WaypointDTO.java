package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.dto.NamedDTO;

/**
 * Equality and hashcode based on object identity
 *
 */
public class WaypointDTO extends NamedDTO {
    private static final long serialVersionUID = 7439553659782967746L;

    public List<MarkDTO> marks;
    
    public ControlPointDTO controlPoint;
    
    public PassingInstruction passingInstructions;
    
    WaypointDTO() {}
    
    public WaypointDTO(String name, ControlPointDTO controlPoint, List<MarkDTO> marks, PassingInstruction passingInstructions) {
        super(name);
        this.setName(name);
        this.marks = marks;
        this.controlPoint = controlPoint;
        this.passingInstructions = passingInstructions;
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
    
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
