package com.sap.sailing.polars.windestimation;

import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sailing.polars.PolarDataService;

/**
 * Implements a wind estimation based on maneuver classifications.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ManeuverBasedWindEstimationTrackImpl extends WindTrackImpl {
    private static final long serialVersionUID = -764156498143531475L;
    
    /**
     * Using a fairly low base confidence for this estimation wind track. It shall only serve as a
     * default and should be superseded by other wind sources easily.
     */
    private static final double DEFAULT_BASE_CONFIDENCE = 0.01;
    
    private final PolarDataService polarService;

    public ManeuverBasedWindEstimationTrackImpl(PolarDataService polarService, long millisecondsOverWhichToAverage, String nameForReadWriteLock) {
        super(millisecondsOverWhichToAverage, DEFAULT_BASE_CONFIDENCE, /* useSpeed */ true, nameForReadWriteLock);
        this.polarService = polarService;
    }
}
