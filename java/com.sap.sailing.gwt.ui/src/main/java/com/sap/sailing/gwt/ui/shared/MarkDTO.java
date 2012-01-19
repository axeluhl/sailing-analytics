package com.sap.sailing.gwt.ui.shared;

public class MarkDTO extends NamedDTO {
    public PositionDTO position;
    
    public MarkDTO() {}
    
    public MarkDTO(String name, double latDeg, double lngDeg) {
        super(name);
        position = new PositionDTO(latDeg, lngDeg);
    }
    
    @Override
    public int hashCode() {
        return 98174 ^ name.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        return name.equals(((MarkDTO) o).name);
    }
}
