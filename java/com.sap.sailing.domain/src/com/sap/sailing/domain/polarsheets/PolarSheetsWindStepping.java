package com.sap.sailing.domain.polarsheets;

public class PolarSheetsWindStepping {

    private static final long serialVersionUID = 1L;

    private Integer[] levels;

    public PolarSheetsWindStepping(Integer[] levels) {
        this.levels = levels;
    }
    
    public int getNumberOfLevels() {
        return levels.length;
    }
    
    public int getLevelForValue(double speed) {
        for (int i = 0; i < levels.length - 1; i++) {
            if (speed < levels[i] || speed < levels[i] + ((levels[i+1] - levels[i]) / 2)) {
                return i;
            }  
        }
        if (speed < levels[levels.length - 1] + 5) {
            return levels.length - 1;
        }
        
        //To high over highest level
        return -1;
    }

    public Integer[] getRawStepping() {
        return levels;
    }

}
