package com.sap.sailing.domain.abstractlog.shared.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterCompetitorAndBoatEvent;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public abstract class BaseRegisterCompetitorAndBoatEventImpl<VisitorT> extends AbstractLogEventImpl<VisitorT> implements
        RegisterCompetitorAndBoatEvent<VisitorT> {
    private static final long serialVersionUID = 7528972504689931986L;
    private final Competitor competitor;
    private final Boat boat;

    /**
     * @throws IllegalArgumentException
     *             if {@code competitor} is null or if {@code boat} is null
     */
    public BaseRegisterCompetitorAndBoatEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, Competitor competitor, Boat boat) throws IllegalArgumentException {
        super(createdAt, logicalTimePoint, author, pId);
        checkCompetitor(competitor);
        checkBoat(boat);
        this.competitor = competitor;
        this.boat = boat;
    }

    /**
     * @throws IllegalArgumentException
     *             if {@code competitor} is null or if {@code boat} is null
     */
    public BaseRegisterCompetitorAndBoatEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            Competitor competitor, Boat boat) throws IllegalArgumentException {
        this(now(), logicalTimePoint, author, randId(), competitor, boat);
    }

    private static void checkCompetitor(Competitor competitor) throws IllegalArgumentException {
        if (competitor == null) {
            throw new IllegalArgumentException("Competitor may not be null");
        }
    }

    private static void checkBoat(Boat boat) throws IllegalArgumentException {
        if (boat == null) {
            throw new IllegalArgumentException("Boat may not be null");
        }
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }

    @Override
    public Boat getBoat() {
        return boat;
    }

    @Override
    public String getShortInfo() {
        return "competitor: " + getCompetitor().toString() + " with boat " + getBoat().toString();
    }
}
