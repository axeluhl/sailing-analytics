package com.sap.sailing.domain.racelog;

import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WithID;

/**
 * <p>
 * A {@link RaceLogEvent} is an entry in a race's log (typically a {@link RaceLog}.
 * </p>
 * 
 * <p>
 * Such an entry may influence the state of a race (scheduled, started, finished, ...), contain information about the
 * current displayed flags (AP, N, X, P, etc.) of a race or indicate some other change regarding the race. See
 * subclasses of {@link RaceLogEvent} for a full list of possibilities.
 * </p>
 * 
 * <p>
 * A {@link RaceLogEvent} may be associated to a certain pass. For a race a new pass is initiated whenever a new start
 * time is proposed (e.g. after the race was aborted).
 * </p>
 * 
 * <p>
 * Each {@link RaceLogEvent} carries two timestamps.
 * 
 * The timestamp received with {@link RaceLogEvent#getCreatedAt()} denotes the point in time the event was created.
 * 
 * The timestamp received with {@link RaceLogEvent#getTimePoint()} denotes the logical point in time the event has
 * occurred (sometimes called 'event time'). This timestamp might be different from {@link RaceLogEvent#getCreatedAt()}.
 * For example setting the start time of race to a point in time in the past, results in a {@link RaceLogEvent}
 * returning "now" on {@link RaceLogEvent#getCreatedAt()} and such a point in time that a complete startphase could have
 * occured on {@link RaceLogEvent#getTimePoint()}.
 * 
 * To ensure an ordering there should be no two {@link RaceLogEvent}s returning the same point in time on
 * {@link RaceLogEvent#getTimePoint()} <b>per pass</b>.
 * </p>
 * 
 */
public interface RaceLogEvent extends Timed, WithID {

    /**
     * Gets the {@link TimePoint} this event was created at.
     */
    TimePoint getCreatedAt();

    /**
     * Gets the {@link TimePoint} denoting the logical event time.
     */
    @Override
    TimePoint getTimePoint();

    /**
     * Gets the event's pass identifier.
     * 
     * Each {@link RaceLogEvent} is associated to a certain pass. A pass is every attempt to start and run a race. A new
     * pass is initiated when a new start time is proposed (e.g. after the race was aborted).
     * 
     */
    int getPassId();

    /**
     * Gets the list of associated {@link Competitor}s.
     * 
     * A {@link RaceLogEvent} might be associated with a list of competitors, which are somehow relevant for this kind
     * of event. An example is a list of competitors who are marked for an individual recall.
     */
    List<Competitor> getInvolvedBoats();

    /**
     * Visitor pattern to implement certain {@link RaceLogEvent} subclass specific behavior.
     */
    void accept(RaceLogEventVisitor visitor);
}
