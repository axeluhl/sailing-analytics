package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Timed;

/**
 * Records a wind observation made at a certain position at a given time with an observed speed and a bearing.
 * Note that while nautically wind is usually specified in the direction it's coming <em>from</em>, for
 * computational purposes internally we measure the wind in the direction <em>to</em> which it blows.
 * Use {@link #getFrom} to get the "inverse" bearing.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface Wind extends Positioned, Timed, SpeedWithBearing {
    /**
     * Computes the inverse bearing telling the direction from where the wind blows.
     */
    Bearing getFrom();
}
