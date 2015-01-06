package com.sap.sailing.domain.common.impl;


public class WindSpeedSteppingWithMaxDistance extends WindSpeedSteppingImpl {
    
    private static final long serialVersionUID = -2207840179212727591L;
    private double maxDistance;

    //For GWT Serialization
    WindSpeedSteppingWithMaxDistance() {
        super();
    };

    public WindSpeedSteppingWithMaxDistance(double[] levels, double maxDistance) {
        super(levels);
        this.maxDistance = maxDistance;
    }
    
    @Override
    public int getLevelIndexForValue(double speed) {
        int result = -1;
        for (int i = 0; i < levels.length - 1; i++) {
            double threshold = levels[i] + ((levels[i+1] - levels[i]) / 2.);
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(maxDistance);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        WindSpeedSteppingWithMaxDistance other = (WindSpeedSteppingWithMaxDistance) obj;
        if (Double.doubleToLongBits(maxDistance) != Double.doubleToLongBits(other.maxDistance))
            return false;
        return true;
    }


}
