package com.sap.sailing.simulator.windfield;

import java.io.Serializable;
import java.util.logging.Logger;

import com.sap.sailing.simulator.windfield.impl.BlastRandomSeedManagerImpl;


public class WindControlParameters implements Serializable {

    private static final long serialVersionUID = -8512613791406845298L;
    
    public boolean showOmniscient;
    public boolean showOpportunist;    
    
    /**
     * Base wind speed in knots
     * Value range 0 to 30
     */
    public Double baseWindSpeed;
    /**
     * Base average wind direction in degrees
     * Value range 0 to 360 degrees
     */
    public Double windBearing;

    /**
     * Base average wind direction in degrees
     * Value range 0 to 360 degrees
     */
    public Double baseWindBearing;

    /**
     * Base wind speed in knots
     * Value range 0 to 30
     */
    public Double curSpeed;
    /**
     * Base average wind direction in degrees
     * Value range 0 to 360 degrees
     */
    public Double curBearing;
    
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
    
    /**
     * Flag to control if the random stream should be reset  
     */
    public boolean resetBlastRandomStream;
    
    private RandomStreamManager blastRandomStreamManager;
    
    private static Logger logger = Logger.getLogger(WindControlParameters.class.getName());
    
    public WindControlParameters() {
        resetBlastRandomStream = false;
        setDefaults();
    }
    
    public WindControlParameters(double speedInKnots, double bearing) {
        resetBlastRandomStream = false;
        setDefaults();
        baseWindSpeed = speedInKnots;
        baseWindBearing = bearing;
    }
    
    public void setDefaults() {
        
        baseWindSpeed = 12.0;
        
        curSpeed = 0.0;
        curBearing = 0.0;
        
        windBearing = 0.0;
        baseWindBearing = 0.0;
        frequency = 0.0; //30.0;
        amplitude = 15.0;
        leftWindSpeed = 100.0;
        middleWindSpeed = 100.0;
        rightWindSpeed = 100.0;
        
        blastProbability = 25.0;
        maxBlastSize = 1.0;
        blastWindSpeed = 120.0;
        blastWindSpeedVar = 10.0;
        
        if (blastRandomStreamManager == null || !resetBlastRandomStream) {
            setBlastRandomStreamManager(new BlastRandomSeedManagerImpl());
        } else {
            logger.info("Resetting random seeds for wind field.");
            blastRandomStreamManager.reset();
        }
    }

    public RandomStreamManager getBlastRandomStreamManager() {
        return blastRandomStreamManager;
    }

    public void setBlastRandomStreamManager(RandomStreamManager blastRandomStreamManager) {
        this.blastRandomStreamManager = blastRandomStreamManager;
    }
}
