package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.shared.events.impl.BaseRegisterCompetitorEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sse.common.TimePoint;

/**
 * This event can be used for 2 kinds of competitor registrations.
 * If 'canBoatsOfCompetitorsChangePerRace' is true for a regatta the event registers a standalone {@link Competitor} without a boat.     
 * If 'canBoatsOfCompetitorsChangePerRace' is false the event registers a {@link CompetitorWithBoat}.    
 * @author Frank Mittag
 *
 */
public class RegattaLogRegisterCompetitorEventImpl extends BaseRegisterCompetitorEventImpl<RegattaLogEventVisitor>
        implements RegattaLogRegisterCompetitorEvent {
    private static final long serialVersionUID = -5114645637316367845L;

    /**
     * @throws IllegalArgumentException
     *             if {@code competitor} is null
     */
    public RegattaLogRegisterCompetitorEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, Competitor competitor) throws IllegalArgumentException {
        super(createdAt, logicalTimePoint, author, id, competitor);
    }

    /**
     * @throws IllegalArgumentException
     *             if {@code competitor} is null
     */
    public RegattaLogRegisterCompetitorEventImpl(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Competitor competitor) throws IllegalArgumentException {
        super(logicalTimePoint, author, competitor);
    }

    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
