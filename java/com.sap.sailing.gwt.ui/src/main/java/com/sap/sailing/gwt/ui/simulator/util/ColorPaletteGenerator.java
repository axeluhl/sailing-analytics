package com.sap.sailing.gwt.ui.simulator.util;

public class ColorPaletteGenerator implements ColorPalette {
    
    //private String[] colors = {"Green","Red","Blue","Yellow","Black"};
    private String[] colors = {"#00ED00","#FFBB00","#FFEA00","#0bf","#00e6ff","#808080","Blue","Blue","Yellow","Black"};
    private int currentIndex = -1;
    
    public ColorPaletteGenerator() {
        
    }
    
    @Override
    public String getNextColor() {
        currentIndex++;
        return colors[currentIndex % colors.length];
    }
    
    @Override
    public String getColor(int idx) {
        return colors[idx];
    }

    @Override
    public void reset() {
        currentIndex = -1;
    }

}
