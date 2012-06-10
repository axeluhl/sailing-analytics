package com.sap.sailing.simulator;


public class WindControlParameters {
    /**
     * Base wind speed in knots
     * Value range 0 to 30
     */
    public Double baseWindSpeed;
    /**
     * Base average wind direction in degrees
     * Value range 0 to 360 degrees
     */
    public Double baseWindBearing;
    
    /**
     * Oscillation parameter frequency
     * Value range 0 to 60 per hour
     */
    public Double frequency;
    
    /**
     * Oscillation parameter amplitude
     * Value range 0 to 20 degrees
     */
    public Double amplitude;
    
    /**
     * Oscillation parameter
     * Gradient description for left side of the windfield
     * Value range 0 to 200% of baseSpeed
     */
    public Double leftWindSpeed;
    
    /**
     * Oscillation parameter
     * Gradient description for middle of the windfield
     * Value range 0 to 200% of baseSpeed
     */
    public Double middleWindSpeed;
    
    /**
     * Oscillation parameter
     * Gradient description for right of the windfield
     * Value range 0 to 200% of baseSpeed
     */
    public Double rightWindSpeed;
    
    /**
     * Blast parameter
     * probability of blast at a certain location
     * Value range 0 to 50%
     */
    public Double blastProbability;
    
    /**
     * Blast parameter
     * Maximum size of the blast, represents how many neighbouring cells it will impact
     * Value range 1 to 10
     */
    public Double maxBlastSize;
    
    /**
     * Blast parameter
     * Average blast wind speed
     * Value range 0 to 100% of baseWindSpeed
     */
    public Double blastWindSpeed;
    
    /**
     * Blast parameter
     * Variance of blast wind speed
     * Value range 0 to 100% of baseWindSpeed
     */
    public Double blastWindSpeedVar;
    
    public WindControlParameters() {
        setDefaults();
    }
    
    public WindControlParameters(double speedInKnots, double bearing) {
        setDefaults();
        baseWindSpeed = speedInKnots;
        baseWindBearing = bearing;
    }
    
    public void setDefaults() {
        
        baseWindSpeed = 12.0;
        
        baseWindBearing = 0.0;
        frequency = 30.0;
        amplitude = 15.0;
        leftWindSpeed = 100.0;
        middleWindSpeed = 100.0;
        rightWindSpeed = 100.0;
        
        blastProbability = 25.0;
        maxBlastSize = 1.0;
        blastWindSpeed = 120.0;
        blastWindSpeedVar = 10.0;
    }
}
