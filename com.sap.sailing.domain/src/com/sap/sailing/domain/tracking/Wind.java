package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Timed;

/**
 * Records a wind observation made at a certain position at a given time with an observed speed and a bearing.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface Wind extends Positioned, Timed, SpeedWithBearing {
}
