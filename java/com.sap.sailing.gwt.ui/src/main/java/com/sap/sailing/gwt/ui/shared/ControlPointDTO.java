package com.sap.sailing.gwt.ui.shared;

/**
 * Equality and hash code are defined by the control point's name.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public abstract class ControlPointDTO extends NamedDTO {
    public ControlPointDTO() {}
    
    public ControlPointDTO(String name) {
        super(name);
    }
    
    @Override
    public int hashCode() {
        return 98174 ^ name.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        return name.equals(((BuoyDTO) o).name);
    }
    
    public abstract Iterable<BuoyDTO> getBuoys();
}
