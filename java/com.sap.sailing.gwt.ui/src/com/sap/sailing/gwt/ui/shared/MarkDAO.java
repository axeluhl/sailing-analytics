package com.sap.sailing.gwt.ui.shared;

public class MarkDAO extends NamedDAO {
    public PositionDAO position;
    
    public MarkDAO() {}
    
    public MarkDAO(String name, double latDeg, double lngDeg) {
        super(name);
        position = new PositionDAO(latDeg, lngDeg);
    }
    
    @Override
    public int hashCode() {
        return 98174 ^ name.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        return name.equals(((MarkDAO) o).name);
    }
}
