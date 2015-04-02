package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Positioned;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sse.common.Timed;

/**
 * Records a wind observation made at a certain position at a given time with an observed speed and a bearing.
 * Note that while nautically wind is usually specified in the direction it's coming <em>from</em>, for
 * computational purposes internally we measure the wind in the direction <em>to</em> which it blows.
 * Use {@link #getFrom} to get the "inverse" bearing.<p>
 * 
 * Note that sometimes a wind measurement is transmitted without its position. While this is useless
 * for wind field interpolation, it can still serve good uses when some wind direction is better than
 * no wind direction at all. In this case, {@link #getPosition()} will return <code>null</code>.
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
