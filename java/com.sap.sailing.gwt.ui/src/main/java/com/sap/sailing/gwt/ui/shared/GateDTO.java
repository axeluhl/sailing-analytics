package com.sap.sailing.gwt.ui.shared;

import java.util.Arrays;

public class GateDTO extends ControlPointDTO {
    private static final long serialVersionUID = 3436355049412041735L;
    private MarkDTO left;
    private MarkDTO right;
    
    public GateDTO() {}
    
    public GateDTO(String idAsString, String name, MarkDTO left, MarkDTO right) {
        super(idAsString, name);
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
