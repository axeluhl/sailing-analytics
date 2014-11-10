package com.sap.sailing.domain.common.impl;

import java.util.Arrays;

import com.sap.sailing.domain.common.WindStepping;

public class WindSteppingImpl implements WindStepping {
    

    //For GWT Serialization
    protected WindSteppingImpl() {};

    private static final long serialVersionUID = 2215693490331489508L;
    protected Integer[] levels;

    public WindSteppingImpl(Integer[] levels) {
        this.levels = levels;
    }
    
    public int getNumberOfLevels() {
        return levels.length;
    }

    @Override
    public int getLevelIndexForValue(double speed) {
        for (int i = 0; i < levels.length - 1; i++) {
            if (speed < levels[i] + ((levels[i+1] - levels[i]) / 2)) {
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
        int result = - 1;
        if (levelIndex >= 0) {
            result = levels[levelIndex];
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(levels);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WindSteppingImpl other = (WindSteppingImpl) obj;
        if (!Arrays.equals(levels, other.levels))
            return false;
        return true;
    }

    @Override
    public int getLevelIndexFloorForValue(double speed) {
        int result = -1;
        for (int i = 0; i < levels.length - 1; i++) {
            if (speed >= levels[i]) {
                result = i;
            }
        }
        return result;
    }

    @Override
    public double getDistanceToLevelFloor(double speed) {
        double result = 0;
        int floor = getLevelIndexFloorForValue(speed);
        if (floor == -1) {
            result = 0;
        } else {
            result = speed - levels[floor];
        }
        return result;
    }

    @Override
    public int getLevelIndexCeilingForValue(double speed) {
        int floor = getLevelIndexFloorForValue(speed);
        int result;
        if (floor == -1) {
            result = -1;
        }
        if (levels[floor] == speed) {
            result = floor;
        } else {
            result = floor + 1;
        }
        return result;
    }

}
