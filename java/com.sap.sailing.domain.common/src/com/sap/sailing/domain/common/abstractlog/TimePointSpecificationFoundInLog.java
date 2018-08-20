package com.sap.sailing.domain.common.abstractlog;

import java.io.Serializable;

import com.sap.sse.common.TimePoint;

/**
 * Events from {@link RaceLog}s and {@link RegattaLog}s may be used to specify timing aspects for a race.
 * When searching for such events, three types of outcome are possible: a valid time point specification
 * may be found; or an explicit specification of {@code null} may be found (such as forcing a tracking
 * interval to be open at one end); or no specification is found.<p>
 * 
 * If only a {@link TimePoint} were used, the last two cases could not be disambiguated when specifying
 * {@code null} as this {@link TimePoint}. This interface makes the distinction possible. If the reference
 * to an object of this type is {@code null} it means that no information regarding the time point was found
 * in the log. If a valid {@link TimePointSpecificationFoundInLog} object is presented, information was found in the log,
 * but the {@link #getTimePoint} could still be {@code null}, meaning that a log entry explicitly set this
 * time point to {@code null}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface TimePointSpecificationFoundInLog extends Serializable {
    TimePoint getTimePoint();
}
