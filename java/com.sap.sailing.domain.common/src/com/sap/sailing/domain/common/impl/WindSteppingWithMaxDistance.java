package com.sap.sailing.domain.common.impl;


public class WindSteppingWithMaxDistance extends WindSteppingImpl {
    
    private static final long serialVersionUID = -2207840179212727591L;
    private double maxDistance;

    //For GWT Serialization
    WindSteppingWithMaxDistance() {
        super();
    };

    public WindSteppingWithMaxDistance(Integer[] levels, double maxDistance) {
        super(levels);
        this.maxDistance = maxDistance;
    }
    
    @Override
    public int getLevelIndexForValue(double speed) {
        int result = -1;
        for (int i = 0; i < levels.length - 1; i++) {
            double threshold = levels[i] + ((levels[i+1] - levels[i]) / 2);
            if (speed < threshold) {
                if (threshold - speed <= maxDistance * 2) {
                    result = i;
                }
                break;
            }  
        }
        if (result == -1) {
            if (Math.abs(levels[levels.length - 1] - speed) <= maxDistance) {
                result = levels.length - 1;
            }
        }
        return result;
    }
    
    public double getMaxDistance() {
        return maxDistance;
    }
}
