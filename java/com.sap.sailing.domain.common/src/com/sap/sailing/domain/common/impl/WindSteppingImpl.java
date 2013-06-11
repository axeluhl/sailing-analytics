package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.WindStepping;

public class WindSteppingImpl implements WindStepping {
    

    //For GWT Serialization
    protected WindSteppingImpl() {};

    private static final long serialVersionUID = 2215693490331489508L;
    private Integer[] levels;

    public WindSteppingImpl(Integer[] levels) {
        this.levels = levels;
    }
    
    public int getNumberOfLevels() {
        return levels.length;
    }

    @Override
    public int getLevelIndexForValue(double speed) {
        for (int i = 0; i < levels.length - 1; i++) {
            if (speed < levels[i] || speed < levels[i] + ((levels[i+1] - levels[i]) / 2)) {
                return i;
            }  
        }
        return levels.length - 1;
    }

    @Override
    public Integer[] getRawStepping() {
        return levels;
    }

    @Override
    public int getSteppedValueForValue(double speed) {
        int levelIndex = getLevelIndexForValue(speed);
        return levels[levelIndex];
    }

}
