package com.sap.sailing.gwt.ui.shared;

import java.util.Arrays;

public class GateDTO extends ControlPointDTO {
    private MarkDTO left;
    private MarkDTO right;
    
    public GateDTO() {}
    
    public GateDTO(String name, MarkDTO left, MarkDTO right) {
        super(name);
        this.left = left;
        this.right = right;
    }
    
    public MarkDTO getLeft() {
        return left;
    }

    public MarkDTO getRight() {
        return right;
    }

    @Override
    public Iterable<MarkDTO> getMarks() {
        return Arrays.asList(left, right);
    }
}
