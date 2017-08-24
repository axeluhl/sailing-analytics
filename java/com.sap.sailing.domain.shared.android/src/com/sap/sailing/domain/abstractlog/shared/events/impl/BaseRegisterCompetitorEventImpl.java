package com.sap.sailing.domain.abstractlog.shared.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventImpl;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterCompetitorEvent;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sse.common.TimePoint;

public abstract class BaseRegisterCompetitorEventImpl<VisitorT> extends AbstractLogEventImpl<VisitorT> implements
        RegisterCompetitorEvent<VisitorT> {
    private static final long serialVersionUID = -30864810737555657L;
    private final CompetitorWithBoat competitor;

    /**
     * @throws IllegalArgumentException
     *             if {@code competitor} is null
     */
    public BaseRegisterCompetitorEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable pId, CompetitorWithBoat competitor) throws IllegalArgumentException {
        super(createdAt, logicalTimePoint, author, pId);
        checkCompetitor(competitor);
        this.competitor = competitor;
    }

    /**
     * @throws IllegalArgumentException
     *             if {@code competitor} is null
     */
    public BaseRegisterCompetitorEventImpl(TimePoint logicalTimePoint, AbstractLogEventAuthor author,
            CompetitorWithBoat competitor) throws IllegalArgumentException {
        this(now(), logicalTimePoint, author, randId(), competitor);
    }

    private static void checkCompetitor(CompetitorWithBoat competitor) throws IllegalArgumentException {
        if (competitor == null) {
            throw new IllegalArgumentException("Competitor may not be null");
        }
        if (competitor.getBoat() == null) {
            throw new IllegalArgumentException("BoaCompetitor may not be null");
        }
    }

    @Override
    public CompetitorWithBoat getCompetitor() {
        return competitor;
    }

    @Override
    public String getShortInfo() {
        return "competitor: " + getCompetitor().toString();
    }
}
