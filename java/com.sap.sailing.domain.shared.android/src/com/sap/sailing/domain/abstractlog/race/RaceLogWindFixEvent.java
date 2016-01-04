package com.sap.sailing.domain.abstractlog.race;

import com.sap.sailing.domain.common.Wind;

/**
 * Contains a wind fix that the race officers documented in the Race Committee App ashore
 *
 */
public interface RaceLogWindFixEvent extends RaceLogEvent {

    /**
     * Returns the wind fix entered by the race committee
     */
    Wind getWindFix();

    /**
     * Usually, wind readings are expected to specify a true north-based direction. However, in some
     * cases capturing a wind direction happens based on a magnetic compass and is not immediately corrected
     * for the magnetic variation / declination. Should this be the case for the wind fix provided by this
     * event, this method returns <code>true</code>, and a {@link DeclinationService} should be used to infer
     * the true north-based wind direction.
     */
    boolean isMagnetic();
}
