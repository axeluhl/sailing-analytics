package com.sap.sailing.domain.abstractlog.regatta.events.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterCompetitorAndBoatEvent;
import com.sap.sailing.domain.abstractlog.shared.events.impl.BaseRegisterCompetitorAndBoatEventImpl;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.common.TimePoint;

public class RegattaLogRegisterCompetitorAndBoatEventImpl extends BaseRegisterCompetitorAndBoatEventImpl<RegattaLogEventVisitor>
        implements RegattaLogRegisterCompetitorAndBoatEvent {
    private static final long serialVersionUID = 3577775910921730978L;

    /**
     * @throws IllegalArgumentException
     *             if {@code competitor} is null
     */
    public RegattaLogRegisterCompetitorAndBoatEventImpl(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, Competitor competitor, Boat boat) throws IllegalArgumentException {
        super(createdAt, logicalTimePoint, author, id, competitor, boat);
    }

    /**
     * @throws IllegalArgumentException
     *             if {@code competitor} is null
     */
    public RegattaLogRegisterCompetitorAndBoatEventImpl(TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Competitor competitor, Boat boat) throws IllegalArgumentException {
        super(logicalTimePoint, author, competitor, boat);
    }

    @Override
    public void accept(RegattaLogEventVisitor visitor) {
        visitor.visit(this);
    }
}
