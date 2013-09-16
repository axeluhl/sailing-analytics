package com.sap.sailing.gwt.ui.simulator.util;

public interface ColorPalette {
    
    public String getNextColor();
    
    public String getColor(int idx);
    
    public void reset();
}
