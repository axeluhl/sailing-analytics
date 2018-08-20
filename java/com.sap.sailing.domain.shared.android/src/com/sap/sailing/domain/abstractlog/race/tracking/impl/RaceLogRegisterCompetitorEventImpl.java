package com.sap.sailing.domain.abstractlog.race.tracking.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventData;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventDataImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.shared.events.impl.BaseRegisterCompetitorEventImpl;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sse.common.TimePoint;

/**
 * Links a competitor to the boat it sails with in the race. The {@link #boat} field refers to the
 * {@link CompetitorWithBoat#getBoat() competitor's boat} if the competitor has a fixed boat attached.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RaceLogRegisterCompetitorEventImpl extends BaseRegisterCompetitorEventImpl<RaceLogEventVisitor> implements
        RaceLogRegisterCompetitorEvent {
    private static final long serialVersionUID = -5114645637316367845L;
    private final RaceLogEventData raceLogEventData;
    
    /**
     * The boat the competitor sails with in the race to which the race log containing this event belongs.
     * Never {@code null}.
     */
    private final Boat boat;

    /**
     * @throws IllegalArgumentException
     *             if {@code competitor} is null
     */
    public RaceLogRegisterCompetitorEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, int passId, CompetitorWithBoat competitor)
            throws IllegalArgumentException {
        super(createdAt, logicalTimePoint, author, id, competitor);
        this.raceLogEventData = new RaceLogEventDataImpl(null, passId);
        checkBoat(competitor.getBoat());
        this.boat = competitor.getBoat();
    }

    /**
     * @throws IllegalArgumentException
     *             if {@code competitor} is null or if {@code boat} is null
     */
    public RaceLogRegisterCompetitorEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, int passId, Competitor competitor, Boat boat)
            throws IllegalArgumentException {
        super(createdAt, logicalTimePoint, author, id, competitor);
        this.raceLogEventData = new RaceLogEventDataImpl(null, passId);
        checkBoat(boat);
        this.boat = boat;
    }

    /**
     * @throws IllegalArgumentException
     *             if {@code competitor} is null or if {@code competitor.getBoat()} is null 
     */
    public RaceLogRegisterCompetitorEventImpl(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, int passId, CompetitorWithBoat competitor)
            throws IllegalArgumentException {
        this(now(), logicalTimePoint, author, randId(), passId, competitor);
    }

    /**
     * @throws IllegalArgumentException
     *             if {@code competitor} is null or if {@code boat} is null 
     */
    public RaceLogRegisterCompetitorEventImpl(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, int passId, Competitor competitor, Boat boat)
            throws IllegalArgumentException {
        this(now(), logicalTimePoint, author, randId(), passId, competitor, boat);
    }

    @Override
    public int getPassId() {
        return raceLogEventData.getPassId();
    }

    @Override
    public List<Competitor> getInvolvedCompetitors() {
        return Collections.singletonList((Competitor) getCompetitor());
    }

    @Override
    public String toString() {
        return raceLogEventData.toString();
    }

    @Override
    public void accept(RaceLogEventVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Boat getBoat() {
        return boat;
    }

    private static void checkBoat(Boat boat) throws IllegalArgumentException {
        if (boat == null) {
            throw new IllegalArgumentException("Boat must not be null");
        }
    }

    @Override
    public String getShortInfo() {
        return "competitor: " + getCompetitor().toString() + " with boat " + getBoat().toString();
    }
}
