package com.sap.sailing.domain.abstractlog;

import java.io.Serializable;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Timed;
import com.sap.sse.common.WithID;

/**
 * <p>
 * A {@link AbstractLogEvent} is an entry in an AbstractLog.
 * </p>
 * 
 * <p>
 * Such an entry may influence the state of a race (scheduled, started, finished, ...), contain information about the
 * current displayed flags (AP, N, X, P, etc.) of a race or indicate some other change regarding the race. See
 * subclasses of {@link AbstractLogEvent} for a full list of possibilities.
 * </p>
 * 
 * <p>
 * A {@link AbstractLogEvent} may be associated to a certain pass. For a race a new pass is initiated whenever a new
 * start time is proposed (e.g. after the race was aborted). Currently the pass is only used by the RaceLog
 * implementation. It should be refactored out of AbstractLog to a later point in time.
 * </p>
 * 
 * <p>
 * Each {@link AbstractLogEvent} carries two timestamps.
 * 
 * The timestamp received with {@link AbstractLogEvent#getCreatedAt()} denotes the point in time the event was created.
 * 
 * The timestamp received with {@link AbstractLogEvent#getLogicalTimePoint()} denotes the logical point in time the
 * event has occurred (sometimes called 'event time'). This timestamp might be different from
 * {@link AbstractLogEvent#getCreatedAt()}. For example setting the start time of race to a point in time in the past,
 * results in a {@link AbstractLogEvent} returning "now" on {@link AbstractLogEvent#getCreatedAt()} and such a point in
 * time that a complete startphase could have occured on {@link AbstractLogEvent#getTimePoint()}.
 * 
 * To ensure an ordering there should be no two {@link AbstractLogEvent}s returning the same point in time on
 * {@link AbstractLogEvent#getCreatedAt()} <b>per pass</b>.
 * 
 * The {@link AbstractLogEvent}'s {@link Timed} interface redirects to {@link AbstractLogEvent#getCreatedAt()} (was
 * {@link AbstractLogEvent#getLogicalTimePoint()}!).
 * </p>
 * 
 * <p>
 * AbstractLog events have an author that has a name and a priority assigned. This can be used to represent multiple
 * concurrent authors such as a device used on a starting vessel, another device used at the finish line and
 * yet another device used on shore. In case more than one device make a statement about something, such as the start
 * time or the start procedure, the statement from the device with the highest priority needs to take precedence.
 * </p>
 */
public interface AbstractLogEvent<VisitorT> extends Timed, WithID, Serializable {

    /**
     * Gets the {@link TimePoint} this event was created at.
     */
    TimePoint getCreatedAt();

    /**
     * Gets the event's logical timestamp.
     * 
     * @return
     */
    TimePoint getLogicalTimePoint();

    /**
     * Visitor pattern to implement certain {@link AbstractLogEvent} subclass specific behavior.
     */
    void accept(VisitorT visitor);

    /**
     * Gets the {@link AbstractLogEventAuthor} author of this event.
     */
    AbstractLogEventAuthor getAuthor();

    /**
     * Gets a short info about the most relevant data of the event.
     * 
     * @return
     */
    String getShortInfo();
}
