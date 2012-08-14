package com.sap.sailing.gwt.ui.shared;

import java.util.Arrays;

public class GateDTO extends ControlPointDTO {
    private BuoyDTO left;
    private BuoyDTO right;
    
    public GateDTO() {}
    
    public GateDTO(String name, BuoyDTO left, BuoyDTO right) {
        super(name);
        this.left = left;
        this.right = right;
    }
    
    public BuoyDTO getLeft() {
        return left;
    }

    public BuoyDTO getRight() {
        return right;
    }

    @Override
    public Iterable<BuoyDTO> getBuoys() {
        return Arrays.asList(left, right);
    }
}
