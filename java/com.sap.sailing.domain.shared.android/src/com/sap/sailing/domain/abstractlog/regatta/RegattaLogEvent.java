package com.sap.sailing.domain.abstractlog.regatta;

import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.base.Timed;

/**
 * <p>
 * A {@link RegattaLogEvent} is an entry in a race's log (typically a {@link RaceLog}.
 * </p>
 * 
 * <p>
 * Such an entry may influence the state of a race (scheduled, started, finished, ...), contain information about the
 * current displayed flags (AP, N, X, P, etc.) of a race or indicate some other change regarding the race. See
 * subclasses of {@link RegattaLogEvent} for a full list of possibilities.
 * </p>
 * 
 * <p>
 * A {@link RegattaLogEvent} may be associated to a certain pass. For a race a new pass is initiated whenever a new start
 * time is proposed (e.g. after the race was aborted).
 * </p>
 * 
 * <p>
 * Each {@link RegattaLogEvent} carries two timestamps.
 * 
 * The timestamp received with {@link RegattaLogEvent#getCreatedAt()} denotes the point in time the event was created.
 * 
 * The timestamp received with {@link RegattaLogEvent#getLogicalTimePoint()} denotes the logical point in time the event
 * has occurred (sometimes called 'event time'). This timestamp might be different from
 * {@link RegattaLogEvent#getCreatedAt()}. For example setting the start time of race to a point in time in the past,
 * results in a {@link RegattaLogEvent} returning "now" on {@link RegattaLogEvent#getCreatedAt()} and such a point in time
 * that a complete startphase could have occured on {@link RegattaLogEvent#getTimePoint()}.
 * 
 * To ensure an ordering there should be no two {@link RegattaLogEvent}s returning the same point in time on
 * {@link RegattaLogEvent#getCreatedAt()} <b>per pass</b>.
 * 
 * The {@link RegattaLogEvent}'s {@link Timed} interface redirects to {@link RegattaLogEvent#getCreatedAt()} (was
 * {@link RegattaLogEvent#getLogicalTimePoint()}!).
 * </p>
 * 
 * <p>
 * Race log events have an author that has a name and a priority assigned. This can be used to represent multiple
 * concurrent race log authors such as a device used on a starting vessel, another device used at the finish line and
 * yet another device used on shore. In case more than one device make a statement about something, such as the start
 * time or the start procedure, the statement from the device with the highest priority needs to take precedence.
 * </p>
 */
public interface RegattaLogEvent extends AbstractLogEvent {

}
